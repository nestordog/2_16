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

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.FutureFamily;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.OptionFamily;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.ExpirationType;
import ch.algotrader.enumeration.OptionType;
import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.util.DateTimeUtil;
import quickfix.field.MaturityMonthYear;
import quickfix.field.PutOrCall;
import quickfix.field.SecurityExchange;
import quickfix.field.SecurityID;
import quickfix.field.SecurityType;
import quickfix.field.StrikePrice;
import quickfix.field.Symbol;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelReplaceRequest;

public class TestTTSymbologyResolver {

    private Future clNov2015;
    private Option coffee;
    private TTSymbologyResolver symbologyResolver;

    @Before
    public void setup() throws Exception {

        Exchange cme = Exchange.Factory.newInstance();
        cme.setName("CME");
        cme.setCode("CME");
        cme.setTimeZone("US/Central");

        FutureFamily futureFamily = FutureFamily.Factory.newInstance();
        futureFamily.setSymbolRoot("CL");
        futureFamily.setExpirationType(ExpirationType.NEXT_3_RD_MONDAY_3_MONTHS);
        futureFamily.setCurrency(Currency.USD);
        futureFamily.setExchange(cme);
        futureFamily.setTickSizePattern("0<0.01");

        this.clNov2015 = Future.Factory.newInstance();
        this.clNov2015.setId(1L);
        this.clNov2015.setSymbol("CL NOV/15");
        this.clNov2015.setTtid("00A0KP00CLZ");
        this.clNov2015.setSecurityFamily(futureFamily);
        this.clNov2015.setExpiration(DateTimeLegacy.toLocalDate(DateTimeUtil.parseLocalDate("2015-11-01")));
        this.clNov2015.setMonthYear("201511");

        Exchange iceipe = Exchange.Factory.newInstance();
        iceipe.setName("ICE_IPE");
        iceipe.setCode("ICE_IPE");
        iceipe.setTimeZone("America/New_York");

        OptionFamily optionFamily = OptionFamily.Factory.newInstance();
        optionFamily.setSymbolRoot("Coffee C");
        optionFamily.setExchange(iceipe);

        this.coffee = Option.Factory.newInstance();
        this.coffee.setId(2);
        this.coffee.setSymbol("Coffee C");
        this.coffee.setDescription("Coffee \"C\" Futures - NYCC");
        this.coffee.setTtid("92900317");
        this.coffee.setSecurityFamily(optionFamily);
        this.coffee.setExpiration(DateTimeLegacy.toLocalDate(DateTimeUtil.parseLocalDate("2015-10-09")));
        this.coffee.setOptionType(OptionType.CALL);
        this.coffee.setStrike(new BigDecimal("50.0"));

        this.symbologyResolver = new TTSymbologyResolver();
    }

    @Test
    public void testResolveFuture() throws Exception {

        NewOrderSingle message = new NewOrderSingle();

        this.symbologyResolver.resolve(message, this.clNov2015, Broker.TT.name());

        Assert.assertNotNull(message);
        Assert.assertEquals(new SecurityType(SecurityType.FUTURE), message.getSecurityType());
        Assert.assertEquals(new Symbol("CL"), message.getSymbol());
        Assert.assertEquals(new SecurityExchange("CME"), message.getSecurityExchange());
        Assert.assertEquals(new MaturityMonthYear("201511"), message.getMaturityMonthYear());
        Assert.assertEquals(new SecurityID("00A0KP00CLZ"), message.getSecurityID());
    }

    @Test
    public void testResolveOption() throws Exception {

        NewOrderSingle message = new NewOrderSingle();

        this.symbologyResolver.resolve(message, this.coffee, Broker.TT.name());

        Assert.assertEquals(new SecurityType(SecurityType.OPTION), message.getSecurityType());
        Assert.assertEquals(new Symbol("Coffee C"), message.getSymbol());
        Assert.assertEquals(new SecurityExchange("ICE_IPE"), message.getSecurityExchange());
        Assert.assertEquals(new MaturityMonthYear("201510"), message.getMaturityMonthYear());
        Assert.assertEquals(new PutOrCall(PutOrCall.CALL), message.getPutOrCall());
        Assert.assertEquals(new StrikePrice(50.0d), message.getStrikePrice());
        Assert.assertEquals(new SecurityID("92900317"), message.getSecurityID());
    }

    @Test
    public void testResolveFutureForModify() throws Exception {

        OrderCancelReplaceRequest message = new OrderCancelReplaceRequest();

        this.symbologyResolver.resolve(message, this.clNov2015, Broker.TT.name());

        Assert.assertEquals(new SecurityType(SecurityType.FUTURE), message.getSecurityType());
        Assert.assertEquals(new Symbol("CL"), message.getSymbol());
        Assert.assertEquals(new SecurityExchange("CME"), message.getSecurityExchange());
        Assert.assertEquals(new MaturityMonthYear("201511"), message.getMaturityMonthYear());
        Assert.assertEquals(new SecurityID("00A0KP00CLZ"), message.getSecurityID());
    }

    @Test
    public void testResolveOptionForModify() throws Exception {

        OrderCancelReplaceRequest message = new OrderCancelReplaceRequest();

        this.symbologyResolver.resolve(message, this.coffee, Broker.TT.name());

        Assert.assertEquals(new SecurityType(SecurityType.OPTION), message.getSecurityType());
        Assert.assertEquals(new Symbol("Coffee C"), message.getSymbol());
        Assert.assertEquals(new SecurityExchange("ICE_IPE"), message.getSecurityExchange());
        Assert.assertEquals(new MaturityMonthYear("201510"), message.getMaturityMonthYear());
        Assert.assertEquals(new PutOrCall(PutOrCall.CALL), message.getPutOrCall());
        Assert.assertEquals(new StrikePrice(50.0d), message.getStrikePrice());
        Assert.assertEquals(new SecurityID("92900317"), message.getSecurityID());
    }

    @Test
    public void testResolveTTExchangeCode() throws Exception {

        this.clNov2015.getSecurityFamily().getExchange().setTtCode("CME_TT");

        NewOrderSingle message = new NewOrderSingle();

        this.symbologyResolver.resolve(message, this.clNov2015, Broker.TT.name());

        Assert.assertNotNull(message);
        Assert.assertEquals(new SecurityType(SecurityType.FUTURE), message.getSecurityType());
        Assert.assertEquals(new Symbol("CL"), message.getSymbol());
        Assert.assertEquals(new SecurityExchange("CME_TT"), message.getSecurityExchange());
    }

}
