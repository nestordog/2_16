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
package ch.algotrader.integration.fxcm;

import java.math.BigDecimal;
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
public class FXCMIntegrationTest extends LocalServiceTest {

    private FixSessionLifecycle fixSessionLifecycle;
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

        System.setProperty("spring.profiles.active", "pooledDataSource, server, noopHistoricalData, fXCMMarketData, fXCMFix");

        ServiceLocator serviceLocator = ServiceLocator.instance();
        serviceLocator.init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);
        fixSessionLifecycle = serviceLocator.getContext().getBean("fXCMSessionLifeCycle", FixSessionLifecycle.class);
        lookupService = serviceLocator.getService("lookupService", LookupService.class);
        socketInitiator = serviceLocator.getContext().getBean(SocketInitiator.class);
        marketDataService = serviceLocator.getService("fXCMFixMarketDataService", FixMarketDataService.class);
        orderService = serviceLocator.getService("fXCMFixOrderService", FixOrderService.class);

        final LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<Object>();
        this.eventQueue = queue;

        this.engine = Mockito.spy(new AbstractEngine() {

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

        EngineLocator.instance().setEngine(StrategyImpl.BASE, this.engine);

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

        if (!fixSessionLifecycle.isLoggedOn()) {

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
    public void testMarketDataAndOrderServiceIntergration() throws Exception {

        Security eurusd = lookupService.getSecurityBySymbol("EUR.USD");
        Assert.assertNotNull(eurusd);
        Account account = lookupService.getAccountByName("FXCM_TEST");
        Assert.assertNotNull(account);
        Strategy base = lookupService.getStrategyByName("BASE");
        Assert.assertNotNull(base);

        marketDataService.subscribe(eurusd);

        double bestBid = Double.MAX_VALUE;
        int bidCount = 0;
        while (bidCount < 5) {

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

        LimitOrder order = new LimitOrderImpl();
        order.setSecurity(eurusd);
        order.setAccount(account);
        order.setStrategy(base);
        order.setQuantity(1000);
        order.setSide(Side.BUY);
        order.setLimit(new BigDecimal(bestBid));
        order.setTif(TIF.GTC);
        order.setDateTime(new Date());

        Pair pair = new Pair<Order, Map<?, ?>>(order, null);
        Mockito.when(engine.executeSingelObjectQuery(Mockito.anyString())).thenReturn(pair);

        orderService.sendOrder(order);

        boolean confirmed = false;
        while (!confirmed) {

            Object event = eventQueue.poll(30, TimeUnit.SECONDS);
            if (event == null) {

                Assert.fail("No event received within specific time limit");
            }

            if (event instanceof OrderStatus) {

                OrderStatus orderStatus = (OrderStatus) event;
                Assert.assertEquals(Status.SUBMITTED, orderStatus.getStatus());
                System.out.println("Limited order placed; internal id: " + orderStatus.getIntId() + "; external id: " + orderStatus.getExtId());
                confirmed = true;
            }

        }
    }

}
