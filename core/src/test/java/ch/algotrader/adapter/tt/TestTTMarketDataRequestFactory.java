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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.adapter.fix.DefaultFixTicketIdGenerator;
import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.FutureFamily;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.ExpirationType;
import quickfix.Group;
import quickfix.field.MDEntryType;
import quickfix.field.MDReqID;
import quickfix.field.NoMDEntryTypes;
import quickfix.field.NoRelatedSym;
import quickfix.field.SecurityExchange;
import quickfix.field.SecurityID;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;
import quickfix.fix42.MarketDataRequest;

public class TestTTMarketDataRequestFactory {

    private TTFixMarketDataRequestFactory requestFactory;

    @Before
    public void setup() throws Exception {

        this.requestFactory = new TTFixMarketDataRequestFactory(new DefaultFixTicketIdGenerator());
    }

    @Test
    public void testRequestSecurity() throws Exception {

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
        future.setId(123L);
        future.setSymbol("CL JUN/16");
        future.setTtid("00A0KP00CLZ");
        future.setSecurityFamily(futureFamily);

        MarketDataRequest marketDataRequest = this.requestFactory.create(future, SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES);

        Assert.assertEquals(new MDReqID("123"), marketDataRequest.getMDReqID());
        Assert.assertEquals(1, marketDataRequest.getGroupCount(NoRelatedSym.FIELD));
        Group symGroup = marketDataRequest.getGroup(1, NoRelatedSym.FIELD);
        Assert.assertEquals("00A0KP00CLZ", symGroup.getString(SecurityID.FIELD));
        Assert.assertEquals("CL", symGroup.getString(Symbol.FIELD));
        Assert.assertEquals("CME", symGroup.getString(SecurityExchange.FIELD));

        Assert.assertEquals(3, marketDataRequest.getGroupCount(NoMDEntryTypes.FIELD));
        Group bidGroup = marketDataRequest.getGroup(1, NoMDEntryTypes.FIELD);
        Assert.assertEquals(MDEntryType.BID, bidGroup.getChar(MDEntryType.FIELD));

        Group offerGroup = marketDataRequest.getGroup(2, NoMDEntryTypes.FIELD);
        Assert.assertEquals(MDEntryType.OFFER, offerGroup.getChar(MDEntryType.FIELD));

        Group tradeGroup = marketDataRequest.getGroup(3, NoMDEntryTypes.FIELD);
        Assert.assertEquals(MDEntryType.TRADE, tradeGroup.getChar(MDEntryType.FIELD));

        Assert.assertEquals(new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES), marketDataRequest.getSubscriptionRequestType());
    }

}
