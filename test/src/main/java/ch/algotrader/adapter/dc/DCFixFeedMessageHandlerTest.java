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
package ch.algotrader.adapter.dc;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ch.algotrader.adapter.fix.DefaultFixApplication;
import ch.algotrader.adapter.fix.DefaultFixSessionStateHolder;
import ch.algotrader.adapter.fix.DefaultLogonMessageHandler;
import ch.algotrader.adapter.fix.FixConfigUtils;
import ch.algotrader.adapter.fix.NoopSessionStateListener;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.esper.AbstractEngine;
import ch.algotrader.esper.Engine;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.vo.AskVO;
import ch.algotrader.vo.BidVO;
import quickfix.CompositeLogFactory;
import quickfix.DefaultSessionFactory;
import quickfix.LogFactory;
import quickfix.MemoryStoreFactory;
import quickfix.SLF4JLogFactory;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;
import quickfix.field.SubscriptionRequestType;
import quickfix.fix44.MarketDataRequest;

public class DCFixFeedMessageHandlerTest {

    private LinkedBlockingQueue<Object> eventQueue;
    private EventDispatcher eventDispatcher;
    private Session session;
    private SocketInitiator socketInitiator;

    @Before
    public void setup() throws Exception {

        final LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<Object>();
        this.eventQueue = queue;

        Engine engine = new AbstractEngine(StrategyImpl.SERVER) {

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

        };

        this.eventDispatcher = Mockito.mock(EventDispatcher.class);

        SessionSettings settings = FixConfigUtils.loadSettings();
        SessionID sessionId = FixConfigUtils.getSessionID(settings, "DCMD");

        DefaultLogonMessageHandler dcLogonHandler = new DefaultLogonMessageHandler(settings);

        DefaultFixApplication fixApplication = new DefaultFixApplication(sessionId,
                new DCFixMarketDataMessageHandler(engine), dcLogonHandler, new DefaultFixSessionStateHolder("DC", this.eventDispatcher));

        LogFactory logFactory = new CompositeLogFactory(new LogFactory[] { new SLF4JLogFactory(settings) });

        DefaultSessionFactory sessionFactory = new DefaultSessionFactory(fixApplication, new MemoryStoreFactory(), logFactory);

        SocketInitiator socketInitiator = new SocketInitiator(sessionFactory, settings);
        socketInitiator.start();

        socketInitiator.createDynamicSession(sessionId);

        this.session = Session.lookupSession(sessionId);

        final CountDownLatch latch = new CountDownLatch(1);

        this.session.addStateListener(new NoopSessionStateListener() {

            @Override
            public void onDisconnect() {
                latch.countDown();
            }

            @Override
            public void onLogon() {
                latch.countDown();
            }

        });

        if (!this.session.isLoggedOn()) {
            latch.await(30, TimeUnit.SECONDS);
        }

        if (!this.session.isLoggedOn()) {
            Assert.fail("Session logon failed");
        }
    }

    @After
    public void shutDown() throws Exception {

        if (this.session != null) {
            if (this.session.isLoggedOn()) {
                this.session.logout("Testing");
            }
            this.session.close();
            this.session = null;
        }
        if (this.socketInitiator != null) {
            this.socketInitiator.stop();
            this.socketInitiator = null;
        }
    }

    @Test
    public void testMarketDataFeed() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setSymbol("EUR.USD");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        DCFixMarketDataRequestFactory requestFactory = new DCFixMarketDataRequestFactory();
        MarketDataRequest request = requestFactory.create(forex, new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES));

        this.session.send(request);

        String tickerId = DCUtil.getDCSymbol(forex);

        for (int i = 0; i < 10; i++) {

            Object event = this.eventQueue.poll(30, TimeUnit.SECONDS);
            if (event == null) {
                Assert.fail("No event received within specific time limit");
            }

            if (event instanceof BidVO) {
                BidVO bid = (BidVO) event;
                Assert.assertEquals(tickerId, bid.getTickerId());
            } else if (event instanceof AskVO) {
                AskVO ask = (AskVO) event;
                Assert.assertEquals(tickerId, ask.getTickerId());
            } else {
                Assert.fail("Unexpected event type: " + event.getClass());
            }
        }
    }

}
