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

import java.math.BigDecimal;
import java.text.DateFormat;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.adapter.fix.FixApplicationException;
import ch.algotrader.adapter.fix.fix44.FixTestUtils;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.security.Stock;
import ch.algotrader.entity.security.StockImpl;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.LimitOrderImpl;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.MarketOrderImpl;
import ch.algotrader.entity.trade.StopLimitOrder;
import ch.algotrader.entity.trade.StopLimitOrderImpl;
import ch.algotrader.entity.trade.StopOrder;
import ch.algotrader.entity.trade.StopOrderImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.TIF;
import quickfix.IntField;
import quickfix.field.ClOrdID;
import quickfix.field.ExpireTime;
import quickfix.field.HandlInst;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.Product;
import quickfix.field.StopPx;
import quickfix.field.Symbol;
import quickfix.field.TimeInForce;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

public class TestCNXOrderMessageFactory {

    private CNXFixOrderMessageFactory requestFactory;

    @Before
    public void setup() throws Exception {

        this.requestFactory = new CNXFixOrderMessageFactory();
    }

    @Test
    public void testMarketOrderForex() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(forex);
        order.setSide(Side.BUY);
        order.setQuantity(2000);

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE), message.getHandlInst());
        Assert.assertEquals(new Symbol("EUR/USD"), message.getSymbol());
        Assert.assertEquals(new Product(Product.CURRENCY), message.getProduct());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new OrderQty(2000), message.getOrderQty());
        Assert.assertEquals(new quickfix.field.Currency("EUR"), message.getCurrency());

        Assert.assertEquals(new OrdType(OrdType.FOREX_MARKET), message.getOrdType());
        Assert.assertFalse(message.isSetField(Price.FIELD));
        Assert.assertFalse(message.isSetField(StopPx.FIELD));
        Assert.assertFalse(message.isSetField(TimeInForce.FIELD));
    }

    @Test(expected = FixApplicationException.class)
    public void testOrderForexUnsupportedSecurityType() throws Exception {

        Stock stock = new StockImpl();
        stock.setSymbol("GOOG");

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(stock);
        order.setSide(Side.BUY);
        order.setQuantity(2000);

        this.requestFactory.createNewOrderMessage(order, "test-id");
    }

    @Test
    public void testLimitOrderForex() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        LimitOrder order = new LimitOrderImpl();
        order.setSecurity(forex);
        order.setSide(Side.BUY);
        order.setQuantity(2000);
        order.setLimit(new BigDecimal("1.345"));

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE), message.getHandlInst());
        Assert.assertEquals(new Symbol("EUR/USD"), message.getSymbol());
        Assert.assertEquals(new Product(Product.CURRENCY), message.getProduct());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new OrderQty(2000), message.getOrderQty());
        Assert.assertEquals(new quickfix.field.Currency("EUR"), message.getCurrency());

        Assert.assertEquals(new OrdType(OrdType.FOREX_LIMIT), message.getOrdType());
        Assert.assertEquals(new Price(1.345), message.getPrice());
        Assert.assertFalse(message.isSetField(StopPx.FIELD));
        Assert.assertFalse(message.isSetField(TimeInForce.FIELD));
    }

    @Test
    public void testStopOrderForex() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        StopOrder order = new StopOrderImpl();
        order.setSecurity(forex);
        order.setSide(Side.BUY);
        order.setQuantity(2000);
        order.setStop(new BigDecimal("1.345"));

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE), message.getHandlInst());
        Assert.assertEquals(new Symbol("EUR/USD"), message.getSymbol());
        Assert.assertEquals(new Product(Product.CURRENCY), message.getProduct());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new OrderQty(2000), message.getOrderQty());
        Assert.assertEquals(new quickfix.field.Currency("EUR"), message.getCurrency());

        Assert.assertEquals(new OrdType(OrdType.STOP), message.getOrdType());
        Assert.assertEquals(new IntField(7534, 1), message.getField(new IntField(7534)));
        Assert.assertEquals(new StopPx(1.345), message.getStopPx());
        Assert.assertFalse(message.isSetField(Price.FIELD));
        Assert.assertFalse(message.isSetField(TimeInForce.FIELD));
    }

    @Test
    public void testStopLimitOrderForex() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        StopLimitOrder order = new StopLimitOrderImpl();
        order.setSecurity(forex);
        order.setSide(Side.SELL);
        order.setQuantity(2000);
        order.setLimit(new BigDecimal("1.355"));
        order.setStop(new BigDecimal("1.345"));

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE), message.getHandlInst());
        Assert.assertEquals(new Symbol("EUR/USD"), message.getSymbol());
        Assert.assertEquals(new Product(Product.CURRENCY), message.getProduct());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.SELL), message.getSide());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new OrderQty(2000), message.getOrderQty());
        Assert.assertEquals(new quickfix.field.Currency("EUR"), message.getCurrency());

        Assert.assertEquals(new OrdType(OrdType.STOP_LIMIT), message.getOrdType());
        Assert.assertEquals(new IntField(7534, 2), message.getField(new IntField(7534)));
        Assert.assertEquals(new Price(1.355), message.getPrice());
        Assert.assertEquals(new StopPx(1.345), message.getStopPx());
        Assert.assertFalse(message.isSetField(TimeInForce.FIELD));
    }

    @Test
    public void testModifyMarketOrderForex() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        MarketOrder order = new MarketOrderImpl();
        order.setIntId("some-int-id");
        order.setExtId("some-ext-id");
        order.setSecurity(forex);
        order.setSide(Side.BUY);
        order.setQuantity(3000);

        OrderCancelReplaceRequest message = this.requestFactory.createModifyOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new OrigClOrdID("some-int-id"), message.getOrigClOrdID());
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new OrderID("some-ext-id"), message.getOrderID());
        Assert.assertEquals(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE), message.getHandlInst());
        Assert.assertEquals(new Symbol("EUR/USD"), message.getSymbol());
        Assert.assertEquals(new Product(Product.CURRENCY), message.getProduct());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new OrderQty(3000), message.getOrderQty());
        Assert.assertEquals(new quickfix.field.Currency("EUR"), message.getCurrency());

        Assert.assertEquals(new OrdType(OrdType.FOREX_MARKET), message.getOrdType());
        Assert.assertFalse(message.isSetField(Price.FIELD));
        Assert.assertFalse(message.isSetField(StopPx.FIELD));
        Assert.assertFalse(message.isSetField(TimeInForce.FIELD));
    }

    @Test(expected = FixApplicationException.class)
    public void testModifyOrderForexUnsupportedSecurityType() throws Exception {

        Stock stock = new StockImpl();
        stock.setSymbol("GOOG");

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(stock);
        order.setSide(Side.BUY);
        order.setQuantity(2000);

        this.requestFactory.createModifyOrderMessage(order, "test-id");
    }

    @Test
    public void testModifyLimitOrderForex() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        LimitOrder order = new LimitOrderImpl();
        order.setIntId("some-int-id");
        order.setExtId("some-ext-id");
        order.setSecurity(forex);
        order.setSide(Side.BUY);
        order.setQuantity(3000);
        order.setLimit(new BigDecimal("2.345"));

        OrderCancelReplaceRequest message = this.requestFactory.createModifyOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new OrigClOrdID("some-int-id"), message.getOrigClOrdID());
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new OrderID("some-ext-id"), message.getOrderID());
        Assert.assertEquals(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE), message.getHandlInst());
        Assert.assertEquals(new Symbol("EUR/USD"), message.getSymbol());
        Assert.assertEquals(new Product(Product.CURRENCY), message.getProduct());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new OrderQty(3000), message.getOrderQty());
        Assert.assertEquals(new quickfix.field.Currency("EUR"), message.getCurrency());

        Assert.assertEquals(new OrdType(OrdType.FOREX_LIMIT), message.getOrdType());
        Assert.assertEquals(new Price(2.345), message.getPrice());
        Assert.assertFalse(message.isSetField(StopPx.FIELD));
        Assert.assertFalse(message.isSetField(TimeInForce.FIELD));
    }

    @Test
    public void testModifyStopOrderForex() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        StopOrder order = new StopOrderImpl();
        order.setIntId("some-int-id");
        order.setExtId("some-ext-id");
        order.setSecurity(forex);
        order.setSide(Side.BUY);
        order.setQuantity(4000);
        order.setStop(new BigDecimal("3.345"));

        OrderCancelReplaceRequest message = this.requestFactory.createModifyOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new OrigClOrdID("some-int-id"), message.getOrigClOrdID());
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new OrderID("some-ext-id"), message.getOrderID());
        Assert.assertEquals(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE), message.getHandlInst());
        Assert.assertEquals(new Symbol("EUR/USD"), message.getSymbol());
        Assert.assertEquals(new Product(Product.CURRENCY), message.getProduct());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new OrderQty(4000), message.getOrderQty());
        Assert.assertEquals(new quickfix.field.Currency("EUR"), message.getCurrency());

        Assert.assertEquals(new OrdType(OrdType.STOP), message.getOrdType());
        Assert.assertEquals(new StopPx(3.345), message.getStopPx());
        Assert.assertFalse(message.isSetField(Price.FIELD));
        Assert.assertFalse(message.isSetField(TimeInForce.FIELD));
    }

    @Test
    public void testModifyStopLimitOrderForex() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        StopLimitOrder order = new StopLimitOrderImpl();
        order.setIntId("some-int-id");
        order.setExtId("some-ext-id");
        order.setSecurity(forex);
        order.setSide(Side.SELL);
        order.setQuantity(5000);
        order.setLimit(new BigDecimal("4.355"));
        order.setStop(new BigDecimal("5.345"));

        OrderCancelReplaceRequest message = this.requestFactory.createModifyOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new OrigClOrdID("some-int-id"), message.getOrigClOrdID());
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new OrderID("some-ext-id"), message.getOrderID());
        Assert.assertEquals(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE), message.getHandlInst());
        Assert.assertEquals(new Symbol("EUR/USD"), message.getSymbol());
        Assert.assertEquals(new Product(Product.CURRENCY), message.getProduct());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.SELL), message.getSide());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new OrderQty(5000), message.getOrderQty());
        Assert.assertEquals(new quickfix.field.Currency("EUR"), message.getCurrency());

        Assert.assertEquals(new OrdType(OrdType.STOP_LIMIT), message.getOrdType());
        Assert.assertEquals(new Price(4.355), message.getPrice());
        Assert.assertEquals(new StopPx(5.345), message.getStopPx());
        Assert.assertFalse(message.isSetField(TimeInForce.FIELD));
    }

    @Test
    public void testCancelMarketOrderForex() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        MarketOrder order = new MarketOrderImpl();
        order.setIntId("some-int-id");
        order.setExtId("some-ext-id");
        order.setSecurity(forex);
        order.setSide(Side.BUY);
        order.setQuantity(3000);

        OrderCancelRequest message = this.requestFactory.createOrderCancelMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new OrigClOrdID("some-int-id"), message.getOrigClOrdID());
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new OrderID("some-ext-id"), message.getOrderID());
        Assert.assertEquals(new Symbol("EUR/USD"), message.getSymbol());
        Assert.assertEquals(new Product(Product.CURRENCY), message.getProduct());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertNotNull(message.getTransactTime());

        Assert.assertEquals(new OrdType(OrdType.FOREX_MARKET), message.getField(new OrdType()));
    }

    @Test(expected = FixApplicationException.class)
    public void testCancelOrderForexUnsupportedSecurityType() throws Exception {

        Stock stock = new StockImpl();
        stock.setSymbol("GOOG");

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(stock);
        order.setSide(Side.BUY);
        order.setQuantity(2000);

        this.requestFactory.createOrderCancelMessage(order, "test-id");
    }

    @Test
    public void testCancelLimitOrderForex() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        LimitOrder order = new LimitOrderImpl();
        order.setIntId("some-int-id");
        order.setExtId("some-ext-id");
        order.setSecurity(forex);
        order.setSide(Side.BUY);
        order.setQuantity(3000);
        order.setLimit(new BigDecimal("2.345"));

        OrderCancelRequest message = this.requestFactory.createOrderCancelMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new OrigClOrdID("some-int-id"), message.getOrigClOrdID());
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new OrderID("some-ext-id"), message.getOrderID());
        Assert.assertEquals(new Symbol("EUR/USD"), message.getSymbol());
        Assert.assertEquals(new Product(Product.CURRENCY), message.getProduct());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertFalse(message.isSetField(OrderQty.FIELD));

        Assert.assertEquals(new OrdType(OrdType.FOREX_LIMIT), message.getField(new OrdType()));
    }

    @Test
    public void testCancelStopOrderForex() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        StopOrder order = new StopOrderImpl();
        order.setIntId("some-int-id");
        order.setExtId("some-ext-id");
        order.setSecurity(forex);
        order.setSide(Side.BUY);
        order.setQuantity(4000);
        order.setStop(new BigDecimal("3.345"));

        OrderCancelRequest message = this.requestFactory.createOrderCancelMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new OrigClOrdID("some-int-id"), message.getOrigClOrdID());
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new OrderID("some-ext-id"), message.getOrderID());
        Assert.assertEquals(new Symbol("EUR/USD"), message.getSymbol());
        Assert.assertEquals(new Product(Product.CURRENCY), message.getProduct());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertFalse(message.isSetField(OrderQty.FIELD));

        Assert.assertEquals(new OrdType(OrdType.STOP), message.getField(new OrdType()));
    }

    @Test
    public void testCancelStopLimitOrderForex() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        StopLimitOrder order = new StopLimitOrderImpl();
        order.setIntId("some-int-id");
        order.setExtId("some-ext-id");
        order.setSecurity(forex);
        order.setSide(Side.SELL);
        order.setQuantity(5000);
        order.setLimit(new BigDecimal("4.355"));
        order.setStop(new BigDecimal("5.345"));

        OrderCancelRequest message = this.requestFactory.createOrderCancelMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new OrigClOrdID("some-int-id"), message.getOrigClOrdID());
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new OrderID("some-ext-id"), message.getOrderID());
        Assert.assertEquals(new Symbol("EUR/USD"), message.getSymbol());
        Assert.assertEquals(new Product(Product.CURRENCY), message.getProduct());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.SELL), message.getSide());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertFalse(message.isSetField(OrderQty.FIELD));

        Assert.assertEquals(new OrdType(OrdType.STOP_LIMIT), message.getField(new OrdType()));
    }

    @Test
    public void testMarketOrderForexTimeInForceDay() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(forex);
        order.setSide(Side.BUY);
        order.setQuantity(2000);
        order.setTif(TIF.DAY);

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new TimeInForce(TimeInForce.DAY), message.getTimeInForce());
    }

    @Test
    public void testMarketOrderForexTimeInForceGoodTillCanceled() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(forex);
        order.setSide(Side.BUY);
        order.setQuantity(2000);
        order.setTif(TIF.GTC);

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new TimeInForce(TimeInForce.GOOD_TILL_CANCEL), message.getTimeInForce());
    }

    @Test
    public void testMarketOrderForexTimeInForceImmediateOrCancel() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(forex);
        order.setSide(Side.BUY);
        order.setQuantity(2000);
        order.setTif(TIF.IOC);

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new TimeInForce(TimeInForce.IMMEDIATE_OR_CANCEL), message.getTimeInForce());
    }

    @Test(expected = FixApplicationException.class)
    public void testStopOrderForexTimeInForceImmediateOrCancel() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        StopLimitOrder order = new StopLimitOrderImpl();
        order.setSecurity(forex);
        order.setSide(Side.SELL);
        order.setQuantity(2000);
        order.setLimit(new BigDecimal("1.355"));
        order.setStop(new BigDecimal("1.345"));
        order.setTif(TIF.IOC);

        this.requestFactory.createNewOrderMessage(order, "test-id");
    }

    @Test
    public void testMarketOrderForexTimeInForceFillOrKill() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(forex);
        order.setSide(Side.BUY);
        order.setQuantity(2000);
        order.setTif(TIF.FOK);

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new TimeInForce(TimeInForce.FILL_OR_KILL), message.getTimeInForce());
    }

    @Test(expected = FixApplicationException.class)
    public void testStopOrderForexTimeInForceFillOrKill() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        StopLimitOrder order = new StopLimitOrderImpl();
        order.setSecurity(forex);
        order.setSide(Side.SELL);
        order.setQuantity(2000);
        order.setLimit(new BigDecimal("1.355"));
        order.setStop(new BigDecimal("1.345"));
        order.setTif(TIF.FOK);

        this.requestFactory.createNewOrderMessage(order, "test-id");
    }

    @Test
    public void testMarketOrderForexTimeInForceGoodTillDate() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        DateFormat dateFormat = FixTestUtils.getSimpleDateTimeFormat();

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(forex);
        order.setSide(Side.BUY);
        order.setQuantity(2000);
        order.setTif(TIF.GTD);
        order.setTifDateTime(dateFormat.parse("20140313-16:00:00.000"));

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new TimeInForce(TimeInForce.GOOD_TILL_DATE), message.getTimeInForce());
        Assert.assertEquals(new ExpireTime(dateFormat.parse("20140313-16:00:00.000")), message.getExpireTime());
    }

    @Test(expected = FixApplicationException.class)
    public void testMarketOrderForexUnsupportedTimeInForce() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(forex);
        order.setSide(Side.BUY);
        order.setQuantity(2000);
        order.setTif(TIF.ATO);

        this.requestFactory.createNewOrderMessage(order, "test-id");
    }

    @Test(expected = FixApplicationException.class)
    public void testMarketOrderForexTimeInForceGoodTillDateMissing() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(forex);
        order.setSide(Side.BUY);
        order.setQuantity(2000);
        order.setTif(TIF.GTD);

        this.requestFactory.createNewOrderMessage(order, "test-id");
    }

}
