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
package ch.algotrader.integration.rt;

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
import ch.algotrader.adapter.fix.ManagedFixAdapter;
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
import ch.algotrader.service.fix.FixOrderService;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SocketInitiator;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class RTIntegrationTest extends LocalServiceTest {

    private LookupService lookupService;
    private SocketInitiator socketInitiator;
    private FixOrderService orderService;
    private ManagedFixAdapter managedFixAdapter;
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

        System.setProperty("spring.profiles.active", "pooledDataSource, server, noopHistoricalData, rTFix");

        ServiceLocator serviceLocator = ServiceLocator.instance();
        serviceLocator.init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);
        lookupService = serviceLocator.getService("lookupService", LookupService.class);
        socketInitiator = serviceLocator.getContext().getBean(SocketInitiator.class);
        orderService = serviceLocator.getService("rTFixOrderService", FixOrderService.class);
        managedFixAdapter = serviceLocator.getService("fixAdapter", ManagedFixAdapter.class);

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

        EngineLocator.instance().setEngine(StrategyImpl.SERVER, this.engine);

        if (orderService instanceof InitializingServiceI) {

            ((InitializingServiceI) orderService).init();
        }

        Collection<String> tradingSessionQualifiiers = lookupService.getActiveSessionsByOrderServiceType(orderService.getOrderServiceType());
        Assert.assertEquals(1, tradingSessionQualifiiers.size());

        String sessionQualifier = tradingSessionQualifiiers.iterator().next();
        Session tradingSession = Session.lookupSession(getSessionID(sessionQualifier));

        final CountDownLatch latch = new CountDownLatch(1);

        tradingSession.addStateListener(new NoopSessionStateListener() {

            @Override
            public void onDisconnect() {
                latch.countDown();
            }

            @Override
            public void onLogon() {
                latch.countDown();
            }

        });

        if (!tradingSession.isLoggedOn()) {
            latch.await(30, TimeUnit.SECONDS);
        }

        if (!tradingSession.isLoggedOn()) {
            System.out.println("Trading session logon failed");
        }

        // Primitive order base
        int orderBase = (int) System.currentTimeMillis() % 0xfff;
        managedFixAdapter.setOrderId(sessionQualifier, orderBase);

        // Purge the queue
        while (eventQueue.poll(5, TimeUnit.SECONDS) != null) {
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
    public void testOrderServiceIntergration() throws Exception {

        Security msft = lookupService.getSecurityBySymbol("MSFT");
        Assert.assertNotNull(msft);
        Account account = lookupService.getAccountByName("RT_TEST");
        Assert.assertNotNull(account);
        Strategy server = lookupService.getStrategyByName("SERVER");
        Assert.assertNotNull(server);

        LimitOrder order = new LimitOrderImpl();
        order.setAccount(account);
        order.setSecurity(msft);
        order.setStrategy(server);
        order.setQuantity(100);
        order.setSide(Side.BUY);
        order.setLimit(new BigDecimal("2000.0"));
        order.setTif(TIF.GTC);
        order.setDateTime(new Date());

        Pair pair = new Pair<Order, Map<?, ?>>(order, null);
        Mockito.when(engine.executeSingelObjectQuery(Mockito.anyString())).thenReturn(pair);

        orderService.sendOrder(order);

        OrderStatus orderAck = null;
        while (orderAck == null) {

            Object event = eventQueue.poll(30, TimeUnit.SECONDS);
            if (event == null) {

                Assert.fail("No event received within specific time limit");
            }

            if (event instanceof OrderStatus) {

                OrderStatus orderStatus = (OrderStatus) event;
                System.out.println("Limited order placed; internal id: " + orderStatus.getIntId() + "; external id: " + orderStatus.getExtId() + ": " + orderStatus);
                Assert.assertEquals(Status.SUBMITTED, orderStatus.getStatus());
                orderAck = orderStatus;
            }

        }

        Assert.assertNotNull(orderAck);

        orderService.cancelOrder(order);

        OrderStatus cancelAck = null;
        while (cancelAck == null) {

            Object event = eventQueue.poll(30, TimeUnit.SECONDS);
            if (event == null) {

                Assert.fail("No event received within specific time limit");
            }

            if (event instanceof OrderStatus) {

                OrderStatus orderStatus = (OrderStatus) event;
                System.out.println("Limited order canceled; internal id: " + orderStatus.getIntId() + "; external id: " + orderStatus.getExtId() + ": " + orderStatus);
                Assert.assertEquals(Status.CANCELED, orderStatus.getStatus());
                cancelAck = orderStatus;
            }

        }
    }

}
