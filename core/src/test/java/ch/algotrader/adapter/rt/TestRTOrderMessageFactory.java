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
package ch.algotrader.adapter.rt;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.adapter.fix.fix44.GenericFix44SymbologyResolver;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.AccountImpl;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.FutureImpl;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.OptionFamily;
import ch.algotrader.entity.security.OptionFamilyImpl;
import ch.algotrader.entity.security.OptionImpl;
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
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.OptionType;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.TIF;
import ch.algotrader.util.DateTimeLegacy;
import quickfix.field.CFICode;
import quickfix.field.ClOrdID;
import quickfix.field.ContractMultiplier;
import quickfix.field.ExpireTime;
import quickfix.field.HandlInst;
import quickfix.field.LocateReqd;
import quickfix.field.MaturityDate;
import quickfix.field.MaturityMonthYear;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.SecurityType;
import quickfix.field.StopPx;
import quickfix.field.StrikePrice;
import quickfix.field.Symbol;
import quickfix.field.TimeInForce;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

public class TestRTOrderMessageFactory {

    private RTFixOrderMessageFactory requestFactory;

    @Before
    public void setup() throws Exception {

        this.requestFactory = new RTFixOrderMessageFactory(new GenericFix44SymbologyResolver());
    }

    @Test
    public void testMarketOrderOption() throws Exception {

        OptionFamily family = new OptionFamilyImpl();
        family.setCurrency(Currency.BRL);
        family.setSymbolRoot("STUFF");
        family.setContractSize(100.5d);
        family.setWeekly(true);

        Option option = new OptionImpl();
        option.setSymbol("SOME_STUFF");
        option.setType(OptionType.CALL);
        option.setSecurityFamily(family);
        option.setStrike(new BigDecimal("0.5"));
        option.setExpiration(DateTimeLegacy.parseAsDateGMT("2014-12-31"));

        Account account = new AccountImpl();
        account.setBroker(Broker.RT.name());
        account.setExtAccount("Bahamas");

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(option);
        order.setSide(Side.BUY);
        order.setQuantity(2000);
        order.setAccount(account);

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new Symbol("STUFF"), message.getSymbol());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertEquals(new OrderQty(2000), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.MARKET), message.getOrdType());
        Assert.assertEquals(new quickfix.field.Currency("BRL"), message.getCurrency());
        Assert.assertEquals(new SecurityType(SecurityType.OPTION), message.getSecurityType());
        Assert.assertEquals(new CFICode("OC"), message.getCFICode());
        Assert.assertEquals(new StrikePrice(0.5d), message.getStrikePrice());
        Assert.assertEquals(new ContractMultiplier(100.5d), message.getContractMultiplier());
        Assert.assertEquals(new MaturityDate("20141231"), message.getMaturityDate());
        Assert.assertEquals(new MaturityMonthYear("201412"), message.getMaturityMonthYear());
        //RT specific
        Assert.assertEquals(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PUBLIC), message.getHandlInst());
        Assert.assertEquals(new LocateReqd(true), message.getLocateReqd());
        Assert.assertEquals(new quickfix.field.Account("Bahamas"), message.getAccount());
    }

    @Test
    public void testMarketOrderFuture() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.BRL);
        family.setSymbolRoot("STUFF");

        Future future = new FutureImpl();
        future.setSymbol("SOME_STUFF");
        future.setSecurityFamily(family);
        future.setExpiration(DateTimeLegacy.parseAsDateGMT("2014-12-31"));
        future.setRic("COILJ6:VE");

        Account account = new AccountImpl();
        account.setBroker(Broker.RT.name());
        account.setExtAccount("Bahamas");

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(future);
        order.setSide(Side.BUY);
        order.setQuantity(2000);
        order.setAccount(account);


        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new Symbol("STUFF"), message.getSymbol());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertEquals(new OrderQty(2000), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.MARKET), message.getOrdType());
        Assert.assertEquals(new quickfix.field.Currency("BRL"), message.getCurrency());
        Assert.assertEquals(new SecurityType(SecurityType.FUTURE), message.getSecurityType());
        Assert.assertEquals(new MaturityMonthYear("201604"), message.getMaturityMonthYear());
        //RT specific
        Assert.assertEquals(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PUBLIC), message.getHandlInst());
        Assert.assertEquals(new LocateReqd(true), message.getLocateReqd());
        Assert.assertEquals(new quickfix.field.Account("Bahamas"), message.getAccount());
    }

    @Test
    public void testMarketOrderStock() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Stock stock = new StockImpl();
        stock.setSecurityFamily(family);
        stock.setSymbol("APPL");

        Account account = new AccountImpl();
        account.setBroker(Broker.RT.name());
        account.setExtAccount("Bahamas");

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(stock);
        order.setSide(Side.SELL);
        order.setQuantity(10);
        order.setAccount(account);

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new Symbol("APPL"), message.getSymbol());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.SELL), message.getSide());
        Assert.assertEquals(new OrderQty(10), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.MARKET), message.getOrdType());
        Assert.assertEquals(new quickfix.field.Currency("USD"), message.getCurrency());
        Assert.assertEquals(new SecurityType(SecurityType.COMMON_STOCK), message.getSecurityType());
        //RT specific
        Assert.assertEquals(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PUBLIC), message.getHandlInst());
        Assert.assertEquals(new LocateReqd(true), message.getLocateReqd());
        Assert.assertEquals(new quickfix.field.Account("Bahamas"), message.getAccount());
    }

    @Test
    public void testMarketOrderStockNoExternalAccount() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Stock stock = new StockImpl();
        stock.setSecurityFamily(family);
        stock.setSymbol("APPL");

        Account account = new AccountImpl();
        account.setBroker(Broker.RT.name());

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(stock);
        order.setSide(Side.SELL);
        order.setQuantity(10);
        order.setAccount(account);

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new Symbol("APPL"), message.getSymbol());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.SELL), message.getSide());
        Assert.assertEquals(new OrderQty(10), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.MARKET), message.getOrdType());
        Assert.assertEquals(new quickfix.field.Currency("USD"), message.getCurrency());
        Assert.assertEquals(new SecurityType(SecurityType.COMMON_STOCK), message.getSecurityType());
        //RT specific
        Assert.assertEquals(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PUBLIC), message.getHandlInst());
        Assert.assertEquals(new LocateReqd(true), message.getLocateReqd());
        Assert.assertFalse(message.isSet(new quickfix.field.Account()));
    }

    @Test
    public void testMarketOrderForex() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setSymbol("EUR.USD");
        forex.setLmaxid("4001");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        Account account = new AccountImpl();
        account.setBroker(Broker.RT.name());
        account.setExtAccount("Bahamas");

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(forex);
        order.setSide(Side.BUY);
        order.setQuantity(2000);
        order.setAccount(account);

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new Symbol("EUR"), message.getSymbol());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertEquals(new OrderQty(2000), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.MARKET), message.getOrdType());
        Assert.assertEquals(new quickfix.field.Currency("USD"), message.getCurrency());
        Assert.assertEquals(new SecurityType(SecurityType.CASH), message.getSecurityType());
        //RT specific
        Assert.assertEquals(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PUBLIC), message.getHandlInst());
        Assert.assertEquals(new LocateReqd(true), message.getLocateReqd());
        Assert.assertEquals(new quickfix.field.Account("Bahamas"), message.getAccount());
    }

    @Test
    public void testLimitedOrderStock() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);
        family.setTickSizePattern("0<0.1");

        Stock stock = new StockImpl();
        stock.setSecurityFamily(family);
        stock.setSymbol("APPL");

        Account account = new AccountImpl();
        account.setBroker(Broker.RT.name());
        account.setExtAccount("Bahamas");

        LimitOrder order = new LimitOrderImpl();
        order.setSecurity(stock);
        order.setSide(Side.BUY);
        order.setQuantity(10);
        order.setAccount(account);
        order.setLimit(new BigDecimal("20.0"));

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new Symbol("APPL"), message.getSymbol());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertEquals(new OrderQty(10), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.LIMIT), message.getOrdType());
        Assert.assertEquals(new quickfix.field.Currency("USD"), message.getCurrency());
        Assert.assertEquals(new SecurityType(SecurityType.COMMON_STOCK), message.getSecurityType());
        Assert.assertEquals(new Price(20.0d), message.getPrice());
        //RT specific
        Assert.assertEquals(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PUBLIC), message.getHandlInst());
        Assert.assertEquals(new LocateReqd(true), message.getLocateReqd());
        Assert.assertEquals(new quickfix.field.Account("Bahamas"), message.getAccount());
    }

    @Test
    public void testStopOrderStock() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);
        family.setTickSizePattern("0<0.1");

        Stock stock = new StockImpl();
        stock.setSecurityFamily(family);
        stock.setSymbol("APPL");

        Account account = new AccountImpl();
        account.setBroker(Broker.RT.name());
        account.setExtAccount("Bahamas");

        StopOrder order = new StopOrderImpl();
        order.setSecurity(stock);
        order.setSide(Side.BUY);
        order.setQuantity(10);
        order.setAccount(account);
        order.setStop(new BigDecimal("20.0"));

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new Symbol("APPL"), message.getSymbol());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertEquals(new OrderQty(10), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.STOP), message.getOrdType());
        Assert.assertEquals(new quickfix.field.Currency("USD"), message.getCurrency());
        Assert.assertEquals(new SecurityType(SecurityType.COMMON_STOCK), message.getSecurityType());
        Assert.assertEquals(new StopPx(20.0d), message.getStopPx());
        //RT specific
        Assert.assertEquals(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PUBLIC), message.getHandlInst());
        Assert.assertEquals(new LocateReqd(true), message.getLocateReqd());
        Assert.assertEquals(new quickfix.field.Account("Bahamas"), message.getAccount());
    }

    @Test
    public void testStopLimitOrderStockOneDay() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);
        family.setTickSizePattern("0<0.1");

        Stock stock = new StockImpl();
        stock.setSecurityFamily(family);
        stock.setSymbol("APPL");

        Account account = new AccountImpl();
        account.setBroker(Broker.RT.name());
        account.setExtAccount("Bahamas");

        StopLimitOrder order = new StopLimitOrderImpl();
        order.setSecurity(stock);
        order.setSide(Side.BUY);
        order.setQuantity(10);
        order.setAccount(account);
        order.setLimit(new BigDecimal("20.0"));
        order.setStop(new BigDecimal("30.0"));
        order.setTif(TIF.DAY);

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new Symbol("APPL"), message.getSymbol());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertEquals(new OrderQty(10), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.STOP_LIMIT), message.getOrdType());
        Assert.assertEquals(new quickfix.field.Currency("USD"), message.getCurrency());
        Assert.assertEquals(new SecurityType(SecurityType.COMMON_STOCK), message.getSecurityType());
        Assert.assertEquals(new Price(20.0d), message.getPrice());
        Assert.assertEquals(new StopPx(30.0d), message.getStopPx());
        Assert.assertEquals(new TimeInForce(TimeInForce.DAY), message.getTimeInForce());
        //RT specific
        Assert.assertEquals(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PUBLIC), message.getHandlInst());
        Assert.assertEquals(new LocateReqd(true), message.getLocateReqd());
        Assert.assertEquals(new quickfix.field.Account("Bahamas"), message.getAccount());
    }

    @Test
    public void testStopLimitOrderStockGoodUntil() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);
        family.setTickSizePattern("0<0.1");

        Stock stock = new StockImpl();
        stock.setSecurityFamily(family);
        stock.setSymbol("APPL");

        Account account = new AccountImpl();
        account.setBroker(Broker.RT.name());
        account.setExtAccount("Bahamas");

        StopLimitOrder order = new StopLimitOrderImpl();
        order.setSecurity(stock);
        order.setSide(Side.BUY);
        order.setQuantity(10);
        order.setAccount(account);
        order.setLimit(new BigDecimal("20.0"));
        order.setStop(new BigDecimal("30.0"));
        order.setTif(TIF.GTD);
        order.setTifDateTime(DateTimeLegacy.parseAsDateGMT("2014-07-01"));

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new Symbol("APPL"), message.getSymbol());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertEquals(new OrderQty(10), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.STOP_LIMIT), message.getOrdType());
        Assert.assertEquals(new quickfix.field.Currency("USD"), message.getCurrency());
        Assert.assertEquals(new SecurityType(SecurityType.COMMON_STOCK), message.getSecurityType());
        Assert.assertEquals(new Price(20.0d), message.getPrice());
        Assert.assertEquals(new StopPx(30.0d), message.getStopPx());
        Assert.assertEquals(new TimeInForce(TimeInForce.GOOD_TILL_DATE), message.getTimeInForce());
        Assert.assertEquals(new ExpireTime(DateTimeLegacy.parseAsDateGMT("2014-07-01")), message.getExpireTime());
        //RT specific
        Assert.assertEquals(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PUBLIC), message.getHandlInst());
        Assert.assertEquals(new LocateReqd(true), message.getLocateReqd());
        Assert.assertEquals(new quickfix.field.Account("Bahamas"), message.getAccount());
    }

    @Test
    public void testModifyMarketOrderOption() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.BRL);
        family.setSymbolRoot("STUFF");
        family.setContractSize(100.5d);

        Option option = new OptionImpl();
        option.setSymbol("SOME_STUFF");
        option.setType(OptionType.CALL);
        option.setSecurityFamily(family);
        option.setStrike(new BigDecimal("0.5"));
        option.setExpiration(DateTimeLegacy.parseAsDateGMT("2014-12-31"));

        Account account = new AccountImpl();
        account.setBroker(Broker.RT.name());
        account.setExtAccount("Bahamas");

        MarketOrder order = new MarketOrderImpl();
        order.setIntId("previous-test-id");
        order.setSecurity(option);
        order.setSide(Side.BUY);
        order.setQuantity(2000);
        order.setAccount(account);


        OrderCancelReplaceRequest message = this.requestFactory.createModifyOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new OrigClOrdID("previous-test-id"), message.getOrigClOrdID());
        Assert.assertEquals(new Symbol("STUFF"), message.getSymbol());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertEquals(new OrderQty(2000), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.MARKET), message.getOrdType());
        Assert.assertEquals(new CFICode("OC"), message.getCFICode());
        Assert.assertEquals(new StrikePrice(0.5d), message.getStrikePrice());
        Assert.assertEquals(new MaturityMonthYear("201412"), message.getMaturityMonthYear());
        //RT specific
        Assert.assertEquals(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PUBLIC), message.getHandlInst());
        Assert.assertEquals(new LocateReqd(true), message.getLocateReqd());
        Assert.assertEquals(new quickfix.field.Account("Bahamas"), message.getAccount());
    }

    @Test
    public void testModifyMarketOrderFuture() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.BRL);
        family.setSymbolRoot("STUFF");

        Future future = new FutureImpl();
        future.setSymbol("SOME_STUFF");
        future.setSecurityFamily(family);
        future.setExpiration(DateTimeLegacy.parseAsDateGMT("2014-12-31"));
        future.setRic("COILJ6:VE");

        Account account = new AccountImpl();
        account.setBroker(Broker.RT.name());
        account.setExtAccount("Bahamas");

        MarketOrder order = new MarketOrderImpl();
        order.setIntId("previous-test-id");
        order.setSecurity(future);
        order.setSide(Side.BUY);
        order.setQuantity(2000);
        order.setAccount(account);


        OrderCancelReplaceRequest message = this.requestFactory.createModifyOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new OrigClOrdID("previous-test-id"), message.getOrigClOrdID());
        Assert.assertEquals(new Symbol("STUFF"), message.getSymbol());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertEquals(new OrderQty(2000), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.MARKET), message.getOrdType());
        Assert.assertEquals(new MaturityMonthYear("201604"), message.getMaturityMonthYear());
        //RT specific
        Assert.assertEquals(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PUBLIC), message.getHandlInst());
        Assert.assertEquals(new LocateReqd(true), message.getLocateReqd());
        Assert.assertEquals(new quickfix.field.Account("Bahamas"), message.getAccount());
    }

    @Test
    public void testModifyMarketOrderStock() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Stock stock = new StockImpl();
        stock.setSecurityFamily(family);
        stock.setSymbol("APPL");

        Account account = new AccountImpl();
        account.setBroker(Broker.RT.name());
        account.setExtAccount("Bahamas");

        MarketOrder order = new MarketOrderImpl();
        order.setIntId("previous-test-id");
        order.setSecurity(stock);
        order.setSide(Side.SELL);
        order.setQuantity(10);
        order.setAccount(account);

        OrderCancelReplaceRequest message = this.requestFactory.createModifyOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new OrigClOrdID("previous-test-id"), message.getOrigClOrdID());
        Assert.assertEquals(new Symbol("APPL"), message.getSymbol());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.SELL), message.getSide());
        Assert.assertEquals(new OrderQty(10), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.MARKET), message.getOrdType());
        //RT specific
        Assert.assertEquals(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PUBLIC), message.getHandlInst());
        Assert.assertEquals(new LocateReqd(true), message.getLocateReqd());
        Assert.assertEquals(new quickfix.field.Account("Bahamas"), message.getAccount());
    }

    @Test
    public void testModifyMarketOrderForex() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setSymbol("EUR.USD");
        forex.setLmaxid("4001");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        Account account = new AccountImpl();
        account.setBroker(Broker.RT.name());
        account.setExtAccount("Bahamas");

        MarketOrder order = new MarketOrderImpl();
        order.setIntId("previous-test-id");
        order.setSecurity(forex);
        order.setSide(Side.BUY);
        order.setQuantity(2000);
        order.setAccount(account);

        OrderCancelReplaceRequest message = this.requestFactory.createModifyOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new OrigClOrdID("previous-test-id"), message.getOrigClOrdID());
        Assert.assertEquals(new Symbol("EUR"), message.getSymbol());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertEquals(new OrderQty(2000), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.MARKET), message.getOrdType());
        //RT specific
        Assert.assertEquals(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PUBLIC), message.getHandlInst());
        Assert.assertEquals(new LocateReqd(true), message.getLocateReqd());
        Assert.assertEquals(new quickfix.field.Account("Bahamas"), message.getAccount());
    }

    @Test
    public void testModifyLimitedOrderStock() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);
        family.setTickSizePattern("0<0.1");

        Stock stock = new StockImpl();
        stock.setSecurityFamily(family);
        stock.setSymbol("APPL");

        Account account = new AccountImpl();
        account.setBroker(Broker.RT.name());
        account.setExtAccount("Bahamas");

        LimitOrder order = new LimitOrderImpl();
        order.setIntId("previous-test-id");
        order.setSecurity(stock);
        order.setSide(Side.BUY);
        order.setQuantity(10);
        order.setAccount(account);
        order.setLimit(new BigDecimal("20.0"));

        OrderCancelReplaceRequest message = this.requestFactory.createModifyOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new OrigClOrdID("previous-test-id"), message.getOrigClOrdID());
        Assert.assertEquals(new Symbol("APPL"), message.getSymbol());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertEquals(new OrderQty(10), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.LIMIT), message.getOrdType());
        Assert.assertEquals(new SecurityType(SecurityType.COMMON_STOCK), message.getSecurityType());
        Assert.assertEquals(new Price(20.0d), message.getPrice());
        //RT specific
        Assert.assertEquals(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PUBLIC), message.getHandlInst());
        Assert.assertEquals(new LocateReqd(true), message.getLocateReqd());
        Assert.assertEquals(new quickfix.field.Account("Bahamas"), message.getAccount());
    }

    @Test
    public void testModifyStopOrderStock() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);
        family.setTickSizePattern("0<0.1");

        Stock stock = new StockImpl();
        stock.setSecurityFamily(family);
        stock.setSymbol("APPL");

        Account account = new AccountImpl();
        account.setBroker(Broker.RT.name());
        account.setExtAccount("Bahamas");

        StopOrder order = new StopOrderImpl();
        order.setIntId("previous-test-id");
        order.setSecurity(stock);
        order.setSide(Side.BUY);
        order.setQuantity(10);
        order.setAccount(account);
        order.setStop(new BigDecimal("20.0"));

        OrderCancelReplaceRequest message = this.requestFactory.createModifyOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new OrigClOrdID("previous-test-id"), message.getOrigClOrdID());
        Assert.assertEquals(new Symbol("APPL"), message.getSymbol());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertEquals(new OrderQty(10), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.STOP), message.getOrdType());
        Assert.assertEquals(new StopPx(20.0d), message.getStopPx());
        //RT specific
        Assert.assertEquals(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PUBLIC), message.getHandlInst());
        Assert.assertEquals(new LocateReqd(true), message.getLocateReqd());
        Assert.assertEquals(new quickfix.field.Account("Bahamas"), message.getAccount());
    }

    @Test
    public void testModifyStopOrderStockNoExternalAccount() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);
        family.setTickSizePattern("0<0.1");

        Stock stock = new StockImpl();
        stock.setSecurityFamily(family);
        stock.setSymbol("APPL");

        Account account = new AccountImpl();
        account.setBroker(Broker.RT.name());

        StopOrder order = new StopOrderImpl();
        order.setIntId("previous-test-id");
        order.setSecurity(stock);
        order.setSide(Side.BUY);
        order.setQuantity(10);
        order.setAccount(account);
        order.setStop(new BigDecimal("20.0"));

        OrderCancelReplaceRequest message = this.requestFactory.createModifyOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new OrigClOrdID("previous-test-id"), message.getOrigClOrdID());
        Assert.assertEquals(new Symbol("APPL"), message.getSymbol());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertEquals(new OrderQty(10), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.STOP), message.getOrdType());
        Assert.assertEquals(new StopPx(20.0d), message.getStopPx());
        //RT specific
        Assert.assertEquals(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PUBLIC), message.getHandlInst());
        Assert.assertEquals(new LocateReqd(true), message.getLocateReqd());
        Assert.assertFalse(message.isSet(new quickfix.field.Account()));
    }

    @Test
    public void testModifyStopLimitOrderStockOneDay() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);
        family.setTickSizePattern("0<0.1");

        Stock stock = new StockImpl();
        stock.setSecurityFamily(family);
        stock.setSymbol("APPL");

        Account account = new AccountImpl();
        account.setBroker(Broker.RT.name());
        account.setExtAccount("Bahamas");

        StopLimitOrder order = new StopLimitOrderImpl();
        order.setIntId("previous-test-id");
        order.setSecurity(stock);
        order.setSide(Side.BUY);
        order.setQuantity(10);
        order.setAccount(account);
        order.setLimit(new BigDecimal("20.0"));
        order.setStop(new BigDecimal("30.0"));
        order.setTif(TIF.DAY);

        OrderCancelReplaceRequest message = this.requestFactory.createModifyOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new OrigClOrdID("previous-test-id"), message.getOrigClOrdID());
        Assert.assertEquals(new Symbol("APPL"), message.getSymbol());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertEquals(new OrderQty(10), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.STOP_LIMIT), message.getOrdType());
        Assert.assertEquals(new Price(20.0d), message.getPrice());
        Assert.assertEquals(new StopPx(30.0d), message.getStopPx());
        Assert.assertEquals(new TimeInForce(TimeInForce.DAY), message.getTimeInForce());
        //RT specific
        Assert.assertEquals(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PUBLIC), message.getHandlInst());
        Assert.assertEquals(new LocateReqd(true), message.getLocateReqd());
        Assert.assertEquals(new quickfix.field.Account("Bahamas"), message.getAccount());
    }

    @Test
    public void testModifyStopLimitOrderStockGoodUntil() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);
        family.setTickSizePattern("0<0.1");

        Stock stock = new StockImpl();
        stock.setSecurityFamily(family);
        stock.setSymbol("APPL");

        Account account = new AccountImpl();
        account.setBroker(Broker.RT.name());
        account.setExtAccount("Bahamas");

        StopLimitOrder order = new StopLimitOrderImpl();
        order.setIntId("previous-test-id");
        order.setSecurity(stock);
        order.setSide(Side.BUY);
        order.setQuantity(10);
        order.setAccount(account);
        order.setLimit(new BigDecimal("20.0"));
        order.setStop(new BigDecimal("30.0"));
        order.setTif(TIF.GTD);
        order.setTifDateTime(DateTimeLegacy.parseAsDateGMT("2014-07-01"));

        OrderCancelReplaceRequest message = this.requestFactory.createModifyOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new OrigClOrdID("previous-test-id"), message.getOrigClOrdID());
        Assert.assertEquals(new Symbol("APPL"), message.getSymbol());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertEquals(new OrderQty(10), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.STOP_LIMIT), message.getOrdType());
        Assert.assertEquals(new Price(20.0d), message.getPrice());
        Assert.assertEquals(new StopPx(30.0d), message.getStopPx());
        Assert.assertEquals(new TimeInForce(TimeInForce.GOOD_TILL_DATE), message.getTimeInForce());
        Assert.assertEquals(new ExpireTime(DateTimeLegacy.parseAsDateGMT("2014-07-01")), message.getExpireTime());
        //RT specific
        Assert.assertEquals(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PUBLIC), message.getHandlInst());
        Assert.assertEquals(new LocateReqd(true), message.getLocateReqd());
        Assert.assertEquals(new quickfix.field.Account("Bahamas"), message.getAccount());
    }

    @Test
    public void testCancelMarketOrderOption() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.BRL);
        family.setSymbolRoot("STUFF");
        family.setContractSize(100.5d);

        Option option = new OptionImpl();
        option.setSymbol("SOME_STUFF");
        option.setType(OptionType.CALL);
        option.setSecurityFamily(family);
        option.setStrike(new BigDecimal("0.5"));
        option.setExpiration(DateTimeLegacy.parseAsDateGMT("2014-12-31"));

        Account account = new AccountImpl();
        account.setBroker(Broker.RT.name());
        account.setExtAccount("Bahamas");

        MarketOrder order = new MarketOrderImpl();
        order.setIntId("previous-test-id");
        order.setSecurity(option);
        order.setSide(Side.BUY);
        order.setQuantity(2000);
        order.setAccount(account);

        OrderCancelRequest message = this.requestFactory.createOrderCancelMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new Symbol("STUFF"), message.getSymbol());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertEquals(new OrderQty(2000), message.getOrderQty());
        Assert.assertEquals(new SecurityType(SecurityType.OPTION), message.getSecurityType());
        Assert.assertEquals(new CFICode(), message.getCFICode()); // TODO: not yet implemented
        Assert.assertEquals(new StrikePrice(0.5d), message.getStrikePrice());
        Assert.assertEquals(new MaturityMonthYear("201412"), message.getMaturityMonthYear());
        //RT specific
        Assert.assertEquals(new quickfix.field.Account("Bahamas"), message.getAccount());
    }

    @Test
    public void testCancelMarketOrderFuture() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.BRL);
        family.setSymbolRoot("STUFF");

        Future future = new FutureImpl();
        future.setSymbol("SOME_STUFF");
        future.setSecurityFamily(family);
        future.setExpiration(DateTimeLegacy.parseAsDateGMT("2014-12-31"));
        future.setRic("COILJ6:VE");

        Account account = new AccountImpl();
        account.setBroker(Broker.RT.name());
        account.setExtAccount("Bahamas");

        MarketOrder order = new MarketOrderImpl();
        order.setIntId("previous-test-id");
        order.setSecurity(future);
        order.setSide(Side.BUY);
        order.setQuantity(2000);
        order.setAccount(account);


        OrderCancelRequest message = this.requestFactory.createOrderCancelMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new Symbol("STUFF"), message.getSymbol());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertEquals(new OrderQty(2000), message.getOrderQty());
        Assert.assertEquals(new SecurityType(SecurityType.FUTURE), message.getSecurityType());
        Assert.assertEquals(new MaturityMonthYear("201604"), message.getMaturityMonthYear());
        //RT specific
        Assert.assertEquals(new quickfix.field.Account("Bahamas"), message.getAccount());
    }

    @Test
    public void testCancelMarketOrderStock() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Stock stock = new StockImpl();
        stock.setSecurityFamily(family);
        stock.setSymbol("APPL");

        Account account = new AccountImpl();
        account.setBroker(Broker.RT.name());
        account.setExtAccount("Bahamas");

        MarketOrder order = new MarketOrderImpl();
        order.setIntId("previous-test-id");
        order.setSecurity(stock);
        order.setSide(Side.SELL);
        order.setQuantity(10);
        order.setAccount(account);

        OrderCancelRequest message = this.requestFactory.createOrderCancelMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new Symbol("APPL"), message.getSymbol());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.SELL), message.getSide());
        Assert.assertEquals(new OrderQty(10), message.getOrderQty());
        Assert.assertEquals(new SecurityType(SecurityType.COMMON_STOCK), message.getSecurityType());
        //RT specific
        Assert.assertEquals(new quickfix.field.Account("Bahamas"), message.getAccount());
    }

    @Test
    public void testCancelMarketOrderStockNoExternalAccount() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Stock stock = new StockImpl();
        stock.setSecurityFamily(family);
        stock.setSymbol("APPL");

        Account account = new AccountImpl();
        account.setBroker(Broker.RT.name());

        MarketOrder order = new MarketOrderImpl();
        order.setIntId("previous-test-id");
        order.setSecurity(stock);
        order.setSide(Side.SELL);
        order.setQuantity(10);
        order.setAccount(account);

        OrderCancelRequest message = this.requestFactory.createOrderCancelMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new Symbol("APPL"), message.getSymbol());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.SELL), message.getSide());
        Assert.assertEquals(new OrderQty(10), message.getOrderQty());
        Assert.assertEquals(new SecurityType(SecurityType.COMMON_STOCK), message.getSecurityType());
        //RT specific
        Assert.assertFalse(message.isSet(new quickfix.field.Account()));
    }

    @Test
    public void testCancelMarketOrderForex() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setSymbol("EUR.USD");
        forex.setLmaxid("4001");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        Account account = new AccountImpl();
        account.setBroker(Broker.RT.name());
        account.setExtAccount("Bahamas");

        MarketOrder order = new MarketOrderImpl();
        order.setIntId("previous-test-id");
        order.setSecurity(forex);
        order.setSide(Side.BUY);
        order.setQuantity(2000);
        order.setAccount(account);

        OrderCancelRequest message = this.requestFactory.createOrderCancelMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new Symbol("EUR"), message.getSymbol());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertEquals(new OrderQty(2000), message.getOrderQty());
        Assert.assertEquals(new SecurityType(SecurityType.CASH), message.getSecurityType());
        //RT specific
        Assert.assertEquals(new quickfix.field.Account("Bahamas"), message.getAccount());
    }

}
