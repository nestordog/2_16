/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.adapter.tt;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ch.algotrader.adapter.fix.DefaultFixApplication;
import ch.algotrader.adapter.fix.DefaultFixSessionStateHolder;
import ch.algotrader.adapter.fix.DefaultFixTicketIdGenerator;
import ch.algotrader.adapter.fix.FixApplicationTestBase;
import ch.algotrader.adapter.fix.FixConfigUtils;
import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.FutureFamily;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.ExpirationType;
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
import quickfix.field.SecurityExchange;
import quickfix.field.SecurityID;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;
import quickfix.fix42.MarketDataRequest;
import quickfix.fix42.MarketDataSnapshotFullRefresh;

public class TTFixFeedMessageHandlerTest extends FixApplicationTestBase {

    private LinkedBlockingQueue<Object> eventQueue;
    private EventDispatcher eventDispatcher;
    private TTFixMarketDataMessageHandler messageHandler;

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
        SessionID sessionId = FixConfigUtils.getSessionID(settings, "TTMD");

        TTLogonMessageHandler logonMessageHandler = new TTLogonMessageHandler(settings);

        this.messageHandler = Mockito.spy(new TTFixMarketDataMessageHandler(engine));

        DefaultFixApplication fixApplication = new DefaultFixApplication(sessionId, this.messageHandler, logonMessageHandler,
                new DefaultFixSessionStateHolder("TTMD", this.eventDispatcher));

        setupSession(settings, sessionId, fixApplication);
    }

    @Test
    public void testMarketDataFeed() throws Exception {

        Exchange exchange = Exchange.Factory.newInstance();
        exchange.setName("CME");
        exchange.setCode("CME");
        exchange.setTimeZone("US/Central");

        FutureFamily futureFamily = FutureFamily.Factory.newInstance();
        futureFamily.setSymbolRoot("CL");
        futureFamily.setExpirationType(ExpirationType.NEXT_3_RD_MONDAY_3_MONTHS);
        futureFamily.setCurrency(Currency.USD);
        futureFamily.setExchange(exchange);

        Future future = Future.Factory.newInstance();
        future.setId(1L);
        future.setSymbol("CL JUN/16");
        future.setTtid("00A0KP00CLZ");
        future.setSecurityFamily(futureFamily);

        TTFixMarketDataRequestFactory requestFactory = new TTFixMarketDataRequestFactory(new DefaultFixTicketIdGenerator());
        MarketDataRequest marketDataRequest = requestFactory.create(future, SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES);

        this.session.send(marketDataRequest);

        for (int i = 0; i < 2; i++) {

            Object event = this.eventQueue.poll(30, TimeUnit.SECONDS);
            if (event == null) {
                Assert.fail("No event received within specific time limit");
            }

            if (event instanceof BidVO) {
                BidVO bid = (BidVO) event;
                Assert.assertEquals("1", bid.getTickerId());
            } else if (event instanceof AskVO) {
                AskVO ask = (AskVO) event;
                Assert.assertEquals("1", ask.getTickerId());
            } else {
                Assert.fail("Unexpected event type: " + event.getClass());
            }
        }

        Mockito.verify(this.messageHandler, Mockito.atLeast(1)).onMessage(
                Mockito.<MarketDataSnapshotFullRefresh>any(), Mockito.eq(this.session.getSessionID()));
    }

    @Test
    public void testMarketDataInvalidRequest() throws Exception {

        MarketDataRequest request = new MarketDataRequest();
        request.set(new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES));
        request.set(new MDReqID("stuff"));
        request.set(new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES));

        request.set(new MarketDepth(1));
        request.set(new MDUpdateType(MDUpdateType.FULL_REFRESH));
        request.set(new AggregatedBook(true));

        MarketDataRequest.NoMDEntryTypes bid = new MarketDataRequest.NoMDEntryTypes();
        bid.set(new MDEntryType(MDEntryType.BID));
        request.addGroup(bid);

        MarketDataRequest.NoMDEntryTypes offer = new MarketDataRequest.NoMDEntryTypes();
        offer.set(new MDEntryType(MDEntryType.OFFER));
        request.addGroup(offer);

        MarketDataRequest.NoRelatedSym symbol = new MarketDataRequest.NoRelatedSym();

        symbol.set(new SecurityExchange("CME"));
        symbol.set(new Symbol("Stuff"));
        symbol.set(new SecurityID("stuff"));
        request.addGroup(symbol);

        this.session.send(request);

        Object event = this.eventQueue.poll(5, TimeUnit.SECONDS);
        Assert.assertNull(event);

        Mockito.verify(this.messageHandler, Mockito.never()).onMessage(
                Mockito.<MarketDataSnapshotFullRefresh>any(), Mockito.eq(this.session.getSessionID()));
    }

}
