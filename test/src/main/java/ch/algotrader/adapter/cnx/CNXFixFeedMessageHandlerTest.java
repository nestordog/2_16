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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ch.algotrader.adapter.fix.DefaultFixSessionStateHolder;
import ch.algotrader.adapter.fix.DefaultLogonMessageHandler;
import ch.algotrader.adapter.fix.FixApplicationTestBase;
import ch.algotrader.adapter.fix.FixConfigUtils;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.esper.AbstractEngine;
import ch.algotrader.esper.Engine;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.vo.marketData.AskVO;
import ch.algotrader.vo.marketData.BidVO;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.field.AggregatedBook;
import quickfix.field.MDEntryType;
import quickfix.field.MDReqID;
import quickfix.field.MDUpdateType;
import quickfix.field.MarketDepth;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;
import quickfix.fix44.MarketDataIncrementalRefresh;
import quickfix.fix44.MarketDataRequest;

public class CNXFixFeedMessageHandlerTest extends FixApplicationTestBase {

    private CNXFixMarketDataRequestFactory requestFactory;
    private LinkedBlockingQueue<Object> eventQueue;
    private EventDispatcher eventDispatcher;
    private CNXFixMarketDataMessageHandler messageHandler;

    @Before
    public void setup() throws Exception {

        this.requestFactory = new CNXFixMarketDataRequestFactory(new CNXTickerIdGenerator());

        final LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
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
        SessionID sessionId = FixConfigUtils.getSessionID(settings, "CNXMD");

        DefaultLogonMessageHandler logonMessageHandler = new DefaultLogonMessageHandler(settings);

        this.messageHandler = Mockito.spy(new CNXFixMarketDataMessageHandler(engine));

        CNXFixApplication fixApplication = new CNXFixApplication(sessionId, this.messageHandler, logonMessageHandler, new DefaultFixSessionStateHolder("CNX", this.eventDispatcher));

        setupSession(settings, sessionId, fixApplication);
    }

    @Test
    public void testMarketDataFeed() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setSymbol("EUR.USD");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        MarketDataRequest request = this.requestFactory.create(forex, SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES);

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
