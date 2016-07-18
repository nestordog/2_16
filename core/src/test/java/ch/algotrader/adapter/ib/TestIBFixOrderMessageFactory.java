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
package ch.algotrader.adapter.ib;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.config.IBConfig;
import ch.algotrader.config.IBConfigBuilder;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.AccountImpl;
import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.exchange.ExchangeImpl;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.FutureImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.MarketOrderImpl;
import ch.algotrader.entity.trade.OrderProperty;
import ch.algotrader.entity.trade.OrderPropertyImpl;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.OrderPropertyType;
import ch.algotrader.enumeration.Side;
import ch.algotrader.util.DateTimeLegacy;
import quickfix.field.AllocationGroup;
import quickfix.field.AllocationMethod;
import quickfix.field.AllocationProfile;
import quickfix.field.ClOrdID;
import quickfix.field.ClearingAccount;
import quickfix.field.CustomerOrFirm;
import quickfix.field.ExDestination;
import quickfix.field.HandlInst;
import quickfix.field.MaturityMonthYear;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.SecurityType;
import quickfix.field.Symbol;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelReplaceRequest;
import quickfix.fix42.OrderCancelRequest;

public class TestIBFixOrderMessageFactory {

    private IBFixOrderMessageFactory requestFactory;
    private SecurityFamily family;
    private Future future;
    private Account account;

    @Before
    public void setup() throws Exception {

        IBConfig ibConfig = IBConfigBuilder.create()
                .setFaMethod("AvailableEquity")
                .build();
        this.requestFactory = new IBFixOrderMessageFactory(ibConfig);

        Exchange exchange = new ExchangeImpl();
        exchange.setCode("DTB");

        this.family = new SecurityFamilyImpl();
        this.family.setCurrency(Currency.EUR);
        this.family.setSymbolRoot("STUFF");
        this.family.setExchange(exchange);

        this.future = new FutureImpl();
        this.future.setSymbol("SOME_STUFF");
        this.future.setRic("COILJ6:VE");
        this.future.setSecurityFamily(this.family);
        this.future.setExpiration(DateTimeLegacy.parseAsDateGMT("2015-03-31"));
        this.future.setMonthYear("201503");

        this.account = new AccountImpl();
        this.account.setBroker(Broker.RT.name());
        this.account.setExtAccount("U1449112");
        this.account.setExtClearingAccount("U1449112");
        this.account.setExtAccountGroup("acc-group");
        this.account.setExtAllocationProfile("profile");
    }

    @Test
    public void testMarketOrder() throws Exception {

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(this.future);
        order.setSide(Side.BUY);
        order.setQuantity(3);
        order.setAccount(this.account);

        OrderProperty p1 = new OrderPropertyImpl();
        p1.setName("tTargetStrategyName");
        p1.setType(OrderPropertyType.FIX);
        p1.setValue("PctVol");

        order.getOrderProperties().put("847", p1);

        NewOrderSingle message = this.requestFactory.createNewOrderMessage(order, "58823.0");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("58823.0"), message.getClOrdID());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new Symbol("STUFF"), message.getSymbol());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertEquals(new OrderQty(3), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.MARKET), message.getOrdType());
        Assert.assertEquals(new quickfix.field.Currency("EUR"), message.getCurrency());
        Assert.assertEquals(new SecurityType(SecurityType.FUTURE), message.getSecurityType());
        Assert.assertEquals(new MaturityMonthYear("201503"), message.getMaturityMonthYear());
        Assert.assertEquals(new ExDestination("DTB"), message.getExDestination());

        Assert.assertEquals(new quickfix.field.Account("U1449112"), message.getAccount());
        Assert.assertEquals(new ClearingAccount("U1449112"), message.getClearingAccount());
        Assert.assertEquals(new AllocationGroup("acc-group"), message.getAllocationGroup());
        Assert.assertEquals(new AllocationMethod("AvailableEquity"), message.getAllocationMethod());
        Assert.assertFalse(message.isSetField(AllocationProfile.FIELD));

        Assert.assertEquals(new HandlInst('1'), message.getHandlInst());
        Assert.assertEquals(new CustomerOrFirm(0), message.getCustomerOrFirm());
        Assert.assertEquals("PctVol", message.getString(847));
    }

    @Test
    public void testModifyMarketOrder() throws Exception {

        MarketOrder order = new MarketOrderImpl();
        order.setIntId("previous-test-id");
        order.setSecurity(this.future);
        order.setSide(Side.BUY);
        order.setQuantity(3);
        order.setAccount(this.account);

        OrderProperty p1 = new OrderPropertyImpl();
        p1.setName("tTargetStrategyName");
        p1.setType(OrderPropertyType.FIX);
        p1.setValue("PctVol");

        order.getOrderProperties().put("847", p1);

        OrderCancelReplaceRequest message = this.requestFactory.createModifyOrderMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new OrigClOrdID("previous-test-id"), message.getOrigClOrdID());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new Symbol("STUFF"), message.getSymbol());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertEquals(new OrderQty(3), message.getOrderQty());
        Assert.assertEquals(new OrdType(OrdType.MARKET), message.getOrdType());
        Assert.assertEquals(new SecurityType(SecurityType.FUTURE), message.getSecurityType());
        Assert.assertEquals(new MaturityMonthYear("201503"), message.getMaturityMonthYear());

        Assert.assertEquals(new quickfix.field.Account("U1449112"), message.getAccount());
        Assert.assertEquals(new ClearingAccount("U1449112"), message.getClearingAccount());
        Assert.assertEquals(new AllocationGroup("acc-group"), message.getAllocationGroup());
        Assert.assertEquals(new AllocationMethod("AvailableEquity"), message.getAllocationMethod());
        Assert.assertFalse(message.isSetField(AllocationProfile.FIELD));

        Assert.assertEquals(new HandlInst('1'), message.getHandlInst());
        Assert.assertEquals(new CustomerOrFirm(0), message.getCustomerOrFirm());
        Assert.assertEquals("PctVol", message.getString(847));
    }

    @Test
    public void testCancelMarketOrder() throws Exception {

        MarketOrder order = new MarketOrderImpl();
        order.setIntId("previous-test-id");
        order.setSecurity(this.future);
        order.setSide(Side.BUY);
        order.setQuantity(3);
        order.setAccount(this.account);

        OrderProperty p1 = new OrderPropertyImpl();
        p1.setName("tTargetStrategyName");
        p1.setType(OrderPropertyType.FIX);
        p1.setValue("PctVol");

        order.getOrderProperties().put("847", p1);

        OrderCancelRequest message = this.requestFactory.createOrderCancelMessage(order, "test-id");

        Assert.assertNotNull(message);
        Assert.assertEquals(new ClOrdID("test-id"), message.getClOrdID());
        Assert.assertEquals(new Symbol("STUFF"), message.getSymbol());
        Assert.assertNotNull(message.getTransactTime());
        Assert.assertEquals(new quickfix.field.Side(quickfix.field.Side.BUY), message.getSide());
        Assert.assertEquals(new OrderQty(3), message.getOrderQty());
        Assert.assertEquals(new SecurityType(SecurityType.FUTURE), message.getSecurityType());
        Assert.assertEquals(new MaturityMonthYear("201503"), message.getMaturityMonthYear());

        Assert.assertEquals(new quickfix.field.Account("U1449112"), message.getAccount());
        Assert.assertEquals(new AllocationGroup("acc-group"), message.getAllocationGroup());
        Assert.assertEquals(new AllocationMethod("AvailableEquity"), message.getAllocationMethod());
        Assert.assertFalse(message.isSetField(AllocationProfile.FIELD));
    }

}
