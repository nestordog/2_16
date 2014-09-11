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
package ch.algotrader.adapter.cnx;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ch.algotrader.adapter.fix.DefaultFixSessionLifecycle;
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
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.vo.AskVO;
import ch.algotrader.vo.BidVO;
import quickfix.DefaultSessionFactory;
import quickfix.LogFactory;
import quickfix.MemoryStoreFactory;
import quickfix.ScreenLogFactory;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;
import quickfix.field.AggregatedBook;
import quickfix.field.MDEntryType;
import quickfix.field.MDReqID;
import quickfix.field.MDUpdateType;
import quickfix.field.MarketDepth;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;
import quickfix.fix44.MarketDataIncrementalRefresh;
import quickfix.fix44.MarketDataRequest;

public class CNXFixFeedMessageHandlerTest {

    private LinkedBlockingQueue<Object> eventQueue;
    private CNXFixMarketDataMessageHandler messageHandler;
    private Session session;
    private SocketInitiator socketInitiator;

    @Before
    public void setup() throws Exception {

        final LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<Object>();
        this.eventQueue = queue;

        EngineLocator.instance().setEngine(StrategyImpl.BASE, new AbstractEngine() {

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

        SessionSettings settings = FixConfigUtils.loadSettings();
        SessionID sessionId = FixConfigUtils.getSessionID(settings, "CNXMD");

        DefaultLogonMessageHandler logonMessageHandler = new DefaultLogonMessageHandler(settings);

        this.messageHandler = Mockito.spy(new CNXFixMarketDataMessageHandler());

        LogFactory logFactory = new ScreenLogFactory(true, true, true);

        CNXFixApplication fixApplication = new CNXFixApplication(sessionId, this.messageHandler, logonMessageHandler, new DefaultFixSessionLifecycle());

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

        CNXFixMarketDataRequestFactory requestFactory = new CNXFixMarketDataRequestFactory();
        MarketDataRequest request = requestFactory.create(forex, new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES));

        this.session.send(request);

        for (int i = 0; i < 10; i++) {

            Object event = this.eventQueue.poll(30, TimeUnit.SECONDS);
            if (event == null) {
                Assert.fail("No event received within specific time limit");
            }

            if (event instanceof BidVO) {
                BidVO bid = (BidVO) event;
                Assert.assertEquals("EUR/USD", bid.getTickerId());
            } else if (event instanceof AskVO) {
                AskVO ask = (AskVO) event;
                Assert.assertEquals("EUR/USD", ask.getTickerId());
            } else {
                Assert.fail("Unexpected event type: " + event.getClass());
            }
        }

        Mockito.verify(this.messageHandler, Mockito.atLeast(5)).onMessage(Mockito.<MarketDataIncrementalRefresh>any(), Mockito.eq(this.session.getSessionID()));
    }

    @Test
    public void testMarketDataInvalidRequest() throws Exception {

        MarketDataRequest request = new MarketDataRequest();
        request.set(new MDReqID("stuff"));
        request.set(new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES));
        request.set(new MarketDepth(1));
        request.set(new MDUpdateType(MDUpdateType.INCREMENTAL_REFRESH));
        request.set(new AggregatedBook(true));

        MarketDataRequest.NoMDEntryTypes bid = new MarketDataRequest.NoMDEntryTypes();
        bid.set(new MDEntryType(MDEntryType.BID));
        request.addGroup(bid);

        MarketDataRequest.NoMDEntryTypes offer = new MarketDataRequest.NoMDEntryTypes();
        offer.set(new MDEntryType(MDEntryType.OFFER));
        request.addGroup(offer);

        MarketDataRequest.NoRelatedSym symGroup = new MarketDataRequest.NoRelatedSym();
        symGroup.set(new Symbol("STUFF"));
        request.addGroup(symGroup);

        this.session.send(request);

        Object event = this.eventQueue.poll(5, TimeUnit.SECONDS);
        Assert.assertNull(event);

        Mockito.verify(this.messageHandler, Mockito.never()).onMessage(Mockito.<MarketDataIncrementalRefresh>any(), Mockito.eq(this.session.getSessionID()));
    }

}
