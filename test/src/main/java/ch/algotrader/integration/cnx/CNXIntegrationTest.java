/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.integration.cnx;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.espertech.esper.collection.Pair;

import ch.algotrader.ServiceLocator;
import ch.algotrader.adapter.fix.FixSessionLifecycle;
import ch.algotrader.adapter.fix.NoopSessionStateListener;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.LimitOrderImpl;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.enumeration.TIF;
import ch.algotrader.esper.AbstractEngine;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.service.InitializingServiceI;
import ch.algotrader.service.LocalServiceTest;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.fix.FixMarketDataService;
import ch.algotrader.service.fix.FixOrderService;
import ch.algotrader.vo.BidVO;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SocketInitiator;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class CNXIntegrationTest extends LocalServiceTest {

    private FixSessionLifecycle marketDataSessionLifecycle;
    private LookupService lookupService;
    private SocketInitiator socketInitiator;
    private FixMarketDataService marketDataService;
    private FixOrderService orderService;
    private LinkedBlockingQueue<Object> eventQueue;
    private Engine engine;

    private SessionID getSessionID(String sessionQualifier) {

        for (SessionID sessionId : this.socketInitiator.getSessions()) {
            if (sessionId.getSessionQualifier().equals(sessionQualifier)) {
                return sessionId;
            }
        }
        throw new IllegalStateException("FIX Session does not exist " + sessionQualifier);
    }

    @Before
    public void setup() throws Exception {

        System.setProperty("spring.profiles.active", "pooledDataSource, server, noopHistoricalData, cNXMarketData, cNXFix");

        ServiceLocator serviceLocator = ServiceLocator.instance();
        serviceLocator.init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);
        marketDataSessionLifecycle = serviceLocator.getContext().getBean("cNXMarketDataSessionLifeCycle", FixSessionLifecycle.class);
        lookupService = serviceLocator.getService("lookupService", LookupService.class);
        socketInitiator = serviceLocator.getContext().getBean(SocketInitiator.class);
        marketDataService = serviceLocator.getService("cNXFixMarketDataService", FixMarketDataService.class);
        orderService = serviceLocator.getService("cNXFixOrderService", FixOrderService.class);

        final LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<Object>();
        this.eventQueue = queue;

        this.engine = Mockito.spy(new AbstractEngine(StrategyImpl.SERVER) {

            @Override
            public void sendEvent(Object obj) {
                try {
                    queue.put(obj);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            @Override
            public List executeQuery(String query) {
                return null;
            }

        });

        EngineLocator.instance().setEngine(StrategyImpl.SERVER, this.engine);

        if (marketDataService instanceof InitializingServiceI) {

            ((InitializingServiceI) marketDataService).init();
        }
        if (orderService instanceof InitializingServiceI) {

            ((InitializingServiceI) orderService).init();
        }

        String marketDataSessionQualifier = marketDataService.getSessionQualifier();
        Session marketDataSession = Session.lookupSession(getSessionID(marketDataSessionQualifier));

        final CountDownLatch latch1 = new CountDownLatch(1);

        marketDataSession.addStateListener(new NoopSessionStateListener() {

            @Override
            public void onDisconnect() {
                latch1.countDown();
            }

            @Override
            public void onLogon() {
                latch1.countDown();
            }

        });

        if (!marketDataSession.isLoggedOn()) {
            latch1.await(30, TimeUnit.SECONDS);
        }

        if (!marketDataSession.isLoggedOn()) {
            Assert.fail("Market data session logon failed");
        }

        OrderServiceType orderServiceType = orderService.getOrderServiceType();
        Collection<String> tradingSessionQualifiiers = lookupService.getActiveSessionsByOrderServiceType(orderServiceType);
        Assert.assertEquals(1, tradingSessionQualifiiers.size());

        Session tradingSession = Session.lookupSession(getSessionID(tradingSessionQualifiiers.iterator().next()));

        final CountDownLatch latch2 = new CountDownLatch(1);

        tradingSession.addStateListener(new NoopSessionStateListener() {

            @Override
            public void onDisconnect() {
                latch2.countDown();
            }

            @Override
            public void onLogon() {
                latch2.countDown();
            }

        });

        if (!tradingSession.isLoggedOn()) {
            latch2.await(30, TimeUnit.SECONDS);
        }

        if (!marketDataSessionLifecycle.isLoggedOn()) {

            // Allow UserRequest message to get through
            Thread.sleep(1000);
        }
    }

    @After
    public void cleanup() throws Exception{

        if (socketInitiator != null) {
            socketInitiator.stop();
        }
        ServiceLocator.instance().shutdown();

        Thread.sleep(1000);
    }

    @Test
    public void testMarketDataAndOrderServiceIntegration() throws Exception {

        Security eurusd = lookupService.getSecurityBySymbol("EUR.USD");
        Assert.assertNotNull(eurusd);
        Account account = lookupService.getAccountByName("CNX_TEST");
        Assert.assertNotNull(account);
        Strategy server = lookupService.getStrategyByName("SERVER");
        Assert.assertNotNull(server);

        marketDataService.subscribe(eurusd);

        double bestBid = Double.MAX_VALUE;
        int bidCount = 0;
        while (bidCount < 10) {

            Object event = eventQueue.poll(30, TimeUnit.SECONDS);
            if (event == null) {

                Assert.fail("No event received within specific time limit");
            }

            if (event instanceof BidVO) {

                bidCount++;
                BidVO bid = (BidVO) event;
                System.out.println("EUR.USD bid " + bid.getBid() + " at " + bid.getVolBid() + " volume");
                if (bid.getBid() < bestBid) {

                    bestBid = bid.getBid();
                }
            }
        }

        LimitOrder order1 = new LimitOrderImpl();
        order1.setSecurity(eurusd);
        order1.setAccount(account);
        order1.setStrategy(server);
        order1.setQuantity(1000L);
        order1.setSide(Side.BUY);
        order1.setLimit(new BigDecimal(bestBid));
        order1.setTif(TIF.GTC);
        order1.setDateTime(new Date());

        Pair pair1 = new Pair<Order, Map<?, ?>>(order1, null);
        Mockito.when(engine.executeSingelObjectQuery(Mockito.anyString())).thenReturn(pair1);

        orderService.sendOrder(order1);

        OrderStatus confirmedOrder1 = null;
        for (int i = 0; i < 20; i++) {

            Object event = eventQueue.poll(30, TimeUnit.SECONDS);
            if (event == null) {

                Assert.fail("No event received within specific time limit");
            }

            if (event instanceof OrderStatus) {

                OrderStatus orderStatus = (OrderStatus) event;
                Assert.assertEquals(Status.SUBMITTED, orderStatus.getStatus());
                System.out.println("Limited order placed; internal id: " + orderStatus.getIntId() + "; external id: " + orderStatus.getExtId());
                confirmedOrder1 = orderStatus;
                break;
            }
        }
        Assert.assertNotNull(confirmedOrder1);

        order1.setIntId(confirmedOrder1.getIntId());
        order1.setExtId(confirmedOrder1.getExtId());

        OrderStatus executedOrder1 = null;
        for (int i = 0; i < 20; i++) {

            Object event = eventQueue.poll(30, TimeUnit.SECONDS);
            if (event == null) {

                Assert.fail("No event received within specific time limit");
            }

            if (event instanceof OrderStatus) {

                OrderStatus orderStatus = (OrderStatus) event;
                Assert.assertEquals(Status.EXECUTED, orderStatus.getStatus());
                System.out.println("Limited order executed; internal id: " + orderStatus.getIntId() + "; external id: " + orderStatus.getExtId());
                executedOrder1 = orderStatus;
                break;
            }
        }
        Assert.assertNotNull(executedOrder1);

        LimitOrder order2 = new LimitOrderImpl();
        order2.setSecurity(eurusd);
        order2.setAccount(account);
        order2.setStrategy(server);
        order2.setQuantity(1000L);
        order2.setSide(Side.BUY);
        order2.setLimit(new BigDecimal(bestBid).multiply(new BigDecimal("0.8")).setScale(5, BigDecimal.ROUND_HALF_DOWN));
        order2.setTif(TIF.GTC);
        order2.setDateTime(new Date());

        Mockito.reset(engine);

        Pair pair2 = new Pair<Order, Map<?, ?>>(order2, null);
        Mockito.when(engine.executeSingelObjectQuery(Mockito.anyString())).thenReturn(pair2);

        orderService.sendOrder(order2);

        OrderStatus confirmedOrder2 = null;
        for (int i = 0; i < 20; i++) {

            Object event = eventQueue.poll(30, TimeUnit.SECONDS);
            if (event == null) {

                Assert.fail("No event received within specific time limit");
            }

            if (event instanceof OrderStatus) {

                OrderStatus orderStatus = (OrderStatus) event;
                Assert.assertEquals(Status.SUBMITTED, orderStatus.getStatus());
                System.out.println("Limited order placed; internal id: " + orderStatus.getIntId() + "; external id: " + orderStatus.getExtId());
                confirmedOrder2 = orderStatus;
                break;
            }
        }
        Assert.assertNotNull(confirmedOrder2);

        order2.setIntId(confirmedOrder2.getIntId());
        order2.setExtId(confirmedOrder2.getExtId());

        Thread.sleep(1000);

        order2.setLimit(new BigDecimal(bestBid).multiply(new BigDecimal("0.9")).setScale(5, BigDecimal.ROUND_HALF_DOWN));

        orderService.modifyOrder(order2);

        OrderStatus confirmedOrder3 = null;
        for (int i = 0; i < 20; i++) {

            Object event = eventQueue.poll(30, TimeUnit.SECONDS);
            if (event == null) {

                Assert.fail("No event received within specific time limit");
            }

            if (event instanceof OrderStatus) {

                OrderStatus orderStatus = (OrderStatus) event;
                Assert.assertEquals(Status.SUBMITTED, orderStatus.getStatus());
                System.out.println("Limited order modified; internal id: " + orderStatus.getIntId() + "; external id: " + orderStatus.getExtId());
                confirmedOrder3 = orderStatus;
                break;
            }
        }
        Assert.assertNotNull(confirmedOrder3);

        order2.setIntId(confirmedOrder3.getIntId());
        order2.setExtId(confirmedOrder3.getExtId());

        Thread.sleep(1000);

        orderService.cancelOrder(order2);

        OrderStatus cancelledOrder2 = null;
        for (int i = 0; i < 20; i++) {

            Object event = eventQueue.poll(30, TimeUnit.SECONDS);
            if (event == null) {

                Assert.fail("No event received within specific time limit");
            }

            if (event instanceof OrderStatus) {

                OrderStatus orderStatus = (OrderStatus) event;
                Assert.assertEquals(Status.CANCELED, orderStatus.getStatus());
                System.out.println("Limited order cancelled; internal id: " + orderStatus.getIntId() + "; external id: " + orderStatus.getExtId());
                cancelledOrder2 = orderStatus;
                break;
            }
        }
        Assert.assertNotNull(cancelledOrder2);
    }

}
