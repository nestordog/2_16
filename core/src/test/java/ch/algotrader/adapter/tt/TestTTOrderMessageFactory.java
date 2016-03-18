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

import ch.algotrader.adapter.BrokerAdapterException;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.FutureFamily;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.OptionFamily;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.Stock;
import ch.algotrader.entity.security.StockImpl;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.LimitOrderImpl;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.MarketOrderImpl;
import ch.algotrader.entity.trade.StopOrder;
import ch.algotrader.entity.trade.StopOrderImpl;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.ExpirationType;
import ch.algotrader.enumeration.OptionType;
import ch.algotrader.enumeration.Side;
import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.util.DateTimeUtil;
import quickfix.field.ClOrdID;
import quickfix.field.MaturityMonthYear;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.PutOrCall;
import quickfix.field.SecurityExchange;
import quickfix.field.SecurityType;
import quickfix.field.StopPx;
import quickfix.field.StrikePrice;
import quickfix.field.Symbol;
import quickfix.field.TimeInForce;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelReplaceRequest;

public class TestTTOrderMessageFactory {

    private Future clNov2015;
    private Option coffee;
    private Account account;
    private TTFixOrderMessageFactory requestFactory;

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

        this.account = Account.Factory.newInstance();
        this.account.setName("TT_TEST");
        this.account.setExtAccount("ratkodts2");
        this.account.setBroker(Broker.TT.name());

        this.requestFactory = new TTFixOrderMessageFactory();
    }

    @Test
    public void testMarketOrderFuture() throws Exception {

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(this.clNov2015);
        order.setSide(Side.BUY);
        order.setQuantity(3);
        order.setAccount(this.account);

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new SecurityType(SecurityType.FUTURE), message.getSecurityType());
        Assert.assertEquals(new Symbol("CL"), message.getSymbol());
        Assert.assertEquals(new SecurityExchange("CME"), message.getSecurityExchange());
        Assert.assertEquals(new MaturityMonthYear("201511"), message.getMaturityMonthYear());
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new OrderQty(3), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.MARKET), message.getOrdType());
        Assert.assertFalse(message.isSetField(Price.FIELD));
        Assert.assertFalse(message.isSetField(StopPx.FIELD));
        Assert.assertFalse(message.isSetField(TimeInForce.FIELD));
    }

    @Test
    public void testMarketOrderOption() throws Exception {

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(this.coffee);
        order.setSide(Side.BUY);
        order.setQuantity(3);
        order.setAccount(this.account);

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new SecurityType(SecurityType.OPTION), message.getSecurityType());
        Assert.assertEquals(new Symbol("Coffee C"), message.getSymbol());
        Assert.assertEquals(new SecurityExchange("ICE_IPE"), message.getSecurityExchange());
        Assert.assertEquals(new MaturityMonthYear("201510"), message.getMaturityMonthYear());
        Assert.assertEquals(new PutOrCall(PutOrCall.CALL), message.getPutOrCall());
        Assert.assertEquals(new StrikePrice(50.0d), message.getStrikePrice());
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new OrderQty(3), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.MARKET), message.getOrdType());
        Assert.assertFalse(message.isSetField(Price.FIELD));
        Assert.assertFalse(message.isSetField(StopPx.FIELD));
        Assert.assertFalse(message.isSetField(TimeInForce.FIELD));
    }

    @Test(expected = BrokerAdapterException.class)
    public void testOrderForexUnsupportedSecurityType() throws Exception {

        Exchange cme = Exchange.Factory.newInstance();
        cme.setName("CME");

        SecurityFamily securityFamily = SecurityFamily.Factory.newInstance();
        securityFamily.setExchange(cme);
        Stock stock = new StockImpl();
        stock.setSecurityFamily(securityFamily);
        stock.setSymbol("GOOG");

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(stock);
        order.setSide(Side.BUY);
        order.setQuantity(2000);
        order.setAccount(this.account);

        this.requestFactory.createNewOrderMessage(order, "test-id");
    }

    @Test
    public void testLimitOrderFuture() throws Exception {

        LimitOrder order = new LimitOrderImpl();
        order.setSecurity(this.clNov2015);
        order.setAccount(this.account);
        order.setSide(Side.BUY);
        order.setQuantity(5);
        order.setLimit(new BigDecimal("1234"));

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new SecurityType(SecurityType.FUTURE), message.getSecurityType());
        Assert.assertEquals(new Symbol("CL"), message.getSymbol());
        Assert.assertEquals(new SecurityExchange("CME"), message.getSecurityExchange());
        Assert.assertEquals(new MaturityMonthYear("201511"), message.getMaturityMonthYear());
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new OrderQty(5), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.LIMIT), message.getOrdType());
        Assert.assertEquals(new Price(1234), message.getPrice());
        Assert.assertFalse(message.isSetField(StopPx.FIELD));
        Assert.assertFalse(message.isSetField(TimeInForce.FIELD));
    }

    @Test
    public void testStopOrderFuture() throws Exception {

        StopOrder order = new StopOrderImpl();
        order.setSecurity(this.clNov2015);
        order.setAccount(this.account);
        order.setSide(Side.BUY);
        order.setQuantity(5);
        order.setStop(new BigDecimal("1234"));

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new SecurityType(SecurityType.FUTURE), message.getSecurityType());
        Assert.assertEquals(new Symbol("CL"), message.getSymbol());
        Assert.assertEquals(new SecurityExchange("CME"), message.getSecurityExchange());
        Assert.assertEquals(new MaturityMonthYear("201511"), message.getMaturityMonthYear());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new OrderQty(5), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.STOP), message.getOrdType());
        Assert.assertEquals(new StopPx(1234), message.getStopPx());
        Assert.assertFalse(message.isSetField(Price.FIELD));
        Assert.assertFalse(message.isSetField(TimeInForce.FIELD));
    }

    @Test
    public void testModifyLimitOrderFuture() throws Exception {

        LimitOrder order = new LimitOrderImpl();
        order.setIntId("test-id");
        order.setSecurity(this.clNov2015);
        order.setAccount(this.account);
        order.setSide(Side.BUY);
        order.setQuantity(7);
        order.setLimit(new BigDecimal("1255"));

        OrderCancelReplaceRequest message = this.requestFactory.createModifyOrderMessage(order, "test-id-2");

        Assert.assertNotNull(message);
        Assert.assertEquals(new SecurityType(SecurityType.FUTURE), message.getSecurityType());
        Assert.assertEquals(new Symbol("CL"), message.getSymbol());
        Assert.assertEquals(new SecurityExchange("CME"), message.getSecurityExchange());
        Assert.assertEquals(new MaturityMonthYear("201511"), message.getMaturityMonthYear());
        Assert.assertEquals(new ClOrdID("test-id-2"), message.getClOrdID());
        Assert.assertEquals(new OrigClOrdID("test-id"), message.getOrigClOrdID());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new OrderQty(7), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.LIMIT), message.getOrdType());
        Assert.assertEquals(new Price(1255), message.getPrice());
        Assert.assertFalse(message.isSetField(StopPx.FIELD));
        Assert.assertFalse(message.isSetField(TimeInForce.FIELD));
    }

    @Test
    public void testTTExchangeCode() throws Exception {

        this.clNov2015.getSecurityFamily().getExchange().setTtCode("CME_TT");

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(this.clNov2015);
        order.setSide(Side.BUY);
        order.setQuantity(3);
        order.setAccount(this.account);

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new SecurityType(SecurityType.FUTURE), message.getSecurityType());
        Assert.assertEquals(new Symbol("CL"), message.getSymbol());
        Assert.assertEquals(new SecurityExchange("CME_TT"), message.getSecurityExchange());
    }

}
