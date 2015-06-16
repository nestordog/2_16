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
package ch.algotrader.adapter.ftx;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.adapter.fix.FixApplicationException;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.AccountImpl;
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
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.TIF;
import quickfix.field.ClOrdID;
import quickfix.field.CumQty;
import quickfix.field.HandlInst;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.SecurityType;
import quickfix.field.StopPx;
import quickfix.field.Symbol;
import quickfix.field.TimeInForce;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

public class TestFTXOrderMessageFactory {

    private Forex forex;
    private Account account;
    private FTXFixOrderMessageFactory requestFactory;

    @Before
    public void setup() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        this.forex = new ForexImpl();
        this.forex.setSymbol("EUR.USD");
        this.forex.setLmaxid("4001");
        this.forex.setBaseCurrency(Currency.EUR);
        this.forex.setSecurityFamily(family);

        this.account = new AccountImpl();
        this.account.setBroker(Broker.FTX);


        this.requestFactory = new FTXFixOrderMessageFactory();
    }

    @Test
    public void testMarketOrderForex() throws Exception {

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(this.forex);
        order.setSide(Side.BUY);
        order.setQuantity(2000);
        order.setAccount(this.account);

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new Symbol("EUR/USD"), message.getSymbol());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertEquals(new OrderQty(2000), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.MARKET), message.getOrdType());
        Assert.assertEquals(new SecurityType(SecurityType.FOREIGN_EXCHANGE_CONTRACT), message.getSecurityType());
        Assert.assertEquals(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE), message.getHandlInst());
        Assert.assertEquals(new TimeInForce(TimeInForce.GOOD_TILL_CANCEL), message.getTimeInForce());
    }

    @Test
    public void testLimitOrderForex() throws Exception {

        LimitOrder order = new LimitOrderImpl();
        order.setSecurity(this.forex);
        order.setSide(Side.BUY);
        order.setQuantity(2000);
        order.setAccount(this.account);
        order.setLimit(new BigDecimal("1.10"));
        order.setTif(TIF.IOC);

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new Symbol("EUR/USD"), message.getSymbol());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertEquals(new OrderQty(2000), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.LIMIT), message.getOrdType());
        Assert.assertEquals(new Price(1.1d), message.getPrice());
        Assert.assertEquals(new SecurityType(SecurityType.FOREIGN_EXCHANGE_CONTRACT), message.getSecurityType());
        Assert.assertEquals(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE), message.getHandlInst());
        Assert.assertEquals(new TimeInForce(TimeInForce.IMMEDIATE_OR_CANCEL), message.getTimeInForce());
    }

    @Test
    public void testModifyMarketOrderForex() throws Exception {

        MarketOrder order = new MarketOrderImpl();
        order.setIntId("test-id-1");
        order.setSecurity(this.forex);
        order.setSide(Side.BUY);
        order.setQuantity(2000);
        order.setAccount(this.account);

        OrderCancelReplaceRequest message = this.requestFactory.createModifyOrderMessage(order, "test-id-2", 500);

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id-2"), message.getClOrdID());
        Assert.assertEquals(new OrigClOrdID("test-id-1"), message.getOrigClOrdID());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new Symbol("EUR/USD"), message.getSymbol());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertEquals(new OrderQty(2000), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.MARKET), message.getOrdType());
        Assert.assertEquals(new CumQty(500.0d), new CumQty(message.getDouble(CumQty.FIELD)));
        Assert.assertEquals(new SecurityType(SecurityType.FOREIGN_EXCHANGE_CONTRACT), message.getSecurityType());
        Assert.assertEquals(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE), message.getHandlInst());
        Assert.assertEquals(new TimeInForce(TimeInForce.GOOD_TILL_CANCEL), message.getTimeInForce());
    }

    @Test
    public void testCancelMarketOrderForex() throws Exception {

        MarketOrder order = new MarketOrderImpl();
        order.setIntId("test-id-1");
        order.setSecurity(this.forex);
        order.setSide(Side.BUY);
        order.setQuantity(2000);
        order.setAccount(this.account);

        OrderCancelRequest message = this.requestFactory.createOrderCancelMessage(order, "test-id-3");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id-3"), message.getClOrdID());
        Assert.assertEquals(new OrigClOrdID("test-id-1"), message.getOrigClOrdID());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new Symbol("EUR/USD"), message.getSymbol());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertEquals(new OrderQty(2000), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.MARKET), new OrdType(message.getChar(OrdType.FIELD)));
        Assert.assertEquals(new SecurityType(SecurityType.FOREIGN_EXCHANGE_CONTRACT), message.getSecurityType());
    }

    @Test(expected = FixApplicationException.class)
    public void testStopLimitOrderForex() throws Exception {

        StopLimitOrder order = new StopLimitOrderImpl();
        order.setSecurity(this.forex);
        order.setSide(Side.BUY);
        order.setQuantity(10);
        order.setAccount(this.account);
        order.setLimit(new BigDecimal("20.0"));
        order.setStop(new BigDecimal("30.0"));
        order.setTif(TIF.GTC);

        this.requestFactory.createNewOrderMessage(order, "test-id");
    }

    @Test
    public void testTIFResolution() throws Exception {

        Assert.assertEquals(new TimeInForce(TimeInForce.GOOD_TILL_CANCEL), this.requestFactory.resolveTimeInForce(TIF.GTC));
        Assert.assertEquals(new TimeInForce(TimeInForce.IMMEDIATE_OR_CANCEL), this.requestFactory.resolveTimeInForce(TIF.IOC));
        Assert.assertEquals(new TimeInForce(TimeInForce.FILL_OR_KILL), this.requestFactory.resolveTimeInForce(TIF.FOK));
        try {
            this.requestFactory.resolveTimeInForce(TIF.DAY);
            Assert.fail("FixApplicationException expected");
        } catch (FixApplicationException ignore) {
        }
        try {
            this.requestFactory.resolveTimeInForce(TIF.GTD);
            Assert.fail("FixApplicationException expected");
        } catch (FixApplicationException ignore) {
        }
        try {
            this.requestFactory.resolveTimeInForce(TIF.ATO);
            Assert.fail("FixApplicationException expected");
        } catch (FixApplicationException ignore) {
        }
        try {
            this.requestFactory.resolveTimeInForce(TIF.ATC);
            Assert.fail("FixApplicationException expected");
        } catch (FixApplicationException ignore) {
        }
    }

}
