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
package ch.algotrader.adapter.fxcm;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.adapter.BrokerAdapterException;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.AccountImpl;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.LimitOrderImpl;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.MarketOrderImpl;
import ch.algotrader.entity.trade.StopLimitOrder;
import ch.algotrader.entity.trade.StopLimitOrderImpl;
import ch.algotrader.entity.trade.StopOrder;
import ch.algotrader.entity.trade.StopOrderImpl;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.TIF;
import quickfix.field.ClOrdID;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.StopPx;
import quickfix.field.Symbol;
import quickfix.field.TimeInForce;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

public class TestFXCMMOrderMessageFactory {

    private SecurityFamily family;
    private Forex forex;
    private Account account;

    private FXCMFixOrderMessageFactory requestFactory;

    @Before
    public void setup() throws Exception {

        this.family = new SecurityFamilyImpl();
        this.family.setCurrency(Currency.USD);
        this.family.setTickSizePattern("0<0.001");

        this.forex = new ForexImpl();
        this.forex.setSymbol("EUR.USD");
        this.forex.setBaseCurrency(Currency.EUR);
        this.forex.setSecurityFamily(this.family);

        this.account = new AccountImpl();
        this.account.setBroker(Broker.IB.name());
        this.account.setExtAccount("test-account");

        this.requestFactory = new FXCMFixOrderMessageFactory();
    }

    @Test
    public void testMarketOrderForex() throws Exception {

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(this.forex);
        order.setAccount(this.account);
        order.setSide(Side.BUY);
        order.setQuantity(123);

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new quickfix.field.Account("test-account"), message.getAccount());
        Assert.assertEquals(new Symbol("EUR/USD"), message.getSymbol());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new OrderQty(123), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.MARKET), message.getOrdType());
        Assert.assertFalse(message.isSetField(Price.FIELD));
        Assert.assertFalse(message.isSetField(StopPx.FIELD));
        Assert.assertFalse(message.isSetField(TimeInForce.FIELD));
    }

    @Test
    public void testLimitOrderForex() throws Exception {

        LimitOrder order = new LimitOrderImpl();
        order.setSecurity(this.forex);
        order.setAccount(this.account);
        order.setSide(Side.BUY);
        order.setQuantity(123);
        order.setLimit(new BigDecimal("1.345"));

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new quickfix.field.Account("test-account"), message.getAccount());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new Symbol("EUR/USD"), message.getSymbol());
        Assert.assertEquals(new OrderQty(123), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.LIMIT), message.getOrdType());
        Assert.assertEquals(new Price(1.345), message.getPrice());
        Assert.assertFalse(message.isSetField(StopPx.FIELD));
        Assert.assertFalse(message.isSetField(TimeInForce.FIELD));
    }

    @Test
    public void testStopOrderForex() throws Exception {

        StopOrder order = new StopOrderImpl();
        order.setSecurity(this.forex);
        order.setAccount(this.account);
        order.setSide(Side.BUY);
        order.setQuantity(123);
        order.setStop(new BigDecimal("1.345"));

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new quickfix.field.Account("test-account"), message.getAccount());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new Symbol("EUR/USD"), message.getSymbol());
        Assert.assertEquals(new OrderQty(123), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.STOP), message.getOrdType());
        Assert.assertEquals(new StopPx(1.345), message.getStopPx());
        Assert.assertFalse(message.isSetField(Price.FIELD));
        Assert.assertFalse(message.isSetField(TimeInForce.FIELD));
    }

    @Test
    public void testStopLimitOrder() throws Exception {

        StopLimitOrder order = new StopLimitOrderImpl();
        order.setSecurity(this.forex);
        order.setAccount(this.account);
        order.setSide(Side.BUY);
        order.setQuantity(123);
        order.setLimit(new BigDecimal("1.355"));
        order.setStop(new BigDecimal("1.345"));

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new quickfix.field.Account("test-account"), message.getAccount());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new Symbol("EUR/USD"), message.getSymbol());
        Assert.assertEquals(new OrderQty(123), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.STOP_LIMIT), message.getOrdType());
        Assert.assertEquals(new Price(1.355), message.getPrice());
        Assert.assertEquals(new StopPx(1.345), message.getStopPx());
        Assert.assertFalse(message.isSetField(TimeInForce.FIELD));
    }

    @Test
    public void testModifyLimitOrderForex() throws Exception {

        LimitOrder order = new LimitOrderImpl();
        order.setIntId("test-id");
        order.setSecurity(this.forex);
        order.setAccount(this.account);
        order.setSide(Side.BUY);
        order.setQuantity(123);
        order.setLimit(new BigDecimal("1.345"));

        OrderCancelReplaceRequest message = this.requestFactory.createModifyOrderMessage(order, "test-id-2");

        Assert.assertNotNull(message);
        Assert.assertEquals(new OrigClOrdID("test-id"), message.getOrigClOrdID());
        Assert.assertEquals(new ClOrdID("test-id-2"), message.getClOrdID());
        Assert.assertEquals(new quickfix.field.Account("test-account"), message.getAccount());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new Symbol("EUR/USD"), message.getSymbol());
        Assert.assertEquals(new OrderQty(123), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.LIMIT), message.getOrdType());
        Assert.assertEquals(new Price(1.345), message.getPrice());
        Assert.assertFalse(message.isSetField(StopPx.FIELD));
        Assert.assertFalse(message.isSetField(TimeInForce.FIELD));
    }

    @Test
    public void testModifyStopOrderForex() throws Exception {

        StopOrder order = new StopOrderImpl();
        order.setIntId("test-id");
        order.setSecurity(this.forex);
        order.setAccount(this.account);
        order.setSide(Side.BUY);
        order.setQuantity(123);
        order.setStop(new BigDecimal("1.353"));

        OrderCancelReplaceRequest message = this.requestFactory.createModifyOrderMessage(order, "test-id-2");

        Assert.assertNotNull(message);
        Assert.assertEquals(new OrigClOrdID("test-id"), message.getOrigClOrdID());
        Assert.assertEquals(new ClOrdID("test-id-2"), message.getClOrdID());
        Assert.assertEquals(new quickfix.field.Account("test-account"), message.getAccount());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new Symbol("EUR/USD"), message.getSymbol());
        Assert.assertEquals(new OrderQty(123), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.STOP), message.getOrdType());
        Assert.assertEquals(new StopPx(1.353), message.getStopPx());
        Assert.assertFalse(message.isSetField(Price.FIELD));
        Assert.assertFalse(message.isSetField(TimeInForce.FIELD));
    }

    @Test
    public void testModifyStopLimitOrderForex() throws Exception {

        StopLimitOrder order = new StopLimitOrderImpl();
        order.setIntId("test-id");
        order.setSecurity(this.forex);
        order.setAccount(this.account);
        order.setSide(Side.BUY);
        order.setQuantity(123);
        order.setLimit(new BigDecimal("1.355"));
        order.setStop(new BigDecimal("1.345"));

        OrderCancelReplaceRequest message = this.requestFactory.createModifyOrderMessage(order, "test-id-2");

        Assert.assertNotNull(message);
        Assert.assertEquals(new OrigClOrdID("test-id"), message.getOrigClOrdID());
        Assert.assertEquals(new ClOrdID("test-id-2"), message.getClOrdID());
        Assert.assertEquals(new quickfix.field.Account("test-account"), message.getAccount());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new Symbol("EUR/USD"), message.getSymbol());
        Assert.assertEquals(new OrderQty(123), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.STOP_LIMIT), message.getOrdType());
        Assert.assertEquals(new Price(1.355), message.getPrice());
        Assert.assertEquals(new StopPx(1.345), message.getStopPx());
        Assert.assertFalse(message.isSetField(TimeInForce.FIELD));
    }

    @Test(expected = BrokerAdapterException.class)
    public void testModifyOrderUnsupportedType() throws Exception {

        MarketOrder order = new MarketOrderImpl();
        order.setIntId("test-id");
        order.setSecurity(this.forex);
        order.setAccount(this.account);
        order.setSide(Side.BUY);
        order.setQuantity(123);

        this.requestFactory.createModifyOrderMessage(order, "test-id-2");
    }

    @Test
    public void testCancelOrderForex() throws Exception {

        LimitOrder order = new LimitOrderImpl();
        order.setIntId("test-id");
        order.setSecurity(this.forex);
        order.setAccount(this.account);
        order.setSide(Side.BUY);
        order.setQuantity(123);
        order.setLimit(new BigDecimal("1.345"));

        OrderCancelRequest message = this.requestFactory.createOrderCancelMessage(order, "test-id-3");

        Assert.assertNotNull(message);
        Assert.assertEquals(new OrigClOrdID("test-id"), message.getOrigClOrdID());
        Assert.assertEquals(new ClOrdID("test-id-3"), message.getClOrdID());
        Assert.assertEquals(new quickfix.field.Account("test-account"), message.getAccount());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new Symbol("EUR/USD"), message.getSymbol());
        Assert.assertEquals(new OrderQty(123), message.getOrderQty());
    }

    @Test(expected = BrokerAdapterException.class)
    public void testMarketOrderATC() throws Exception {

        MarketOrder order = new MarketOrderImpl();
        order.setTif(TIF.ATC);
        this.requestFactory.resolveTimeInForce(order);
    }

    @Test(expected = BrokerAdapterException.class)
    public void testMarketOrderATO() throws Exception {

        MarketOrder order = new MarketOrderImpl();
        order.setTif(TIF.ATO);
        this.requestFactory.resolveTimeInForce(order);
    }

}
