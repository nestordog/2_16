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

import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ch.algotrader.adapter.fix.DefaultFixApplication;
import ch.algotrader.adapter.fix.DefaultFixSessionStateHolder;
import ch.algotrader.adapter.fix.DefaultLogonMessageHandler;
import ch.algotrader.adapter.fix.FixApplicationTestBase;
import ch.algotrader.adapter.fix.FixConfigUtils;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.MarketOrderImpl;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Status;
import ch.algotrader.esper.AbstractEngine;
import ch.algotrader.esper.Engine;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.ordermgmt.OrderRegistry;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.field.Account;
import quickfix.field.ClOrdID;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Side;
import quickfix.field.StopPx;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

public class FXCMFixOrderMessageHandlerTest extends FixApplicationTestBase {

    private LinkedBlockingQueue<Object> eventQueue;
    private EventDispatcher eventDispatcher;
    private OrderRegistry orderRegistry;
    private String account;
    private FXCMFixOrderMessageHandler messageHandler;

    @Before
    public void setup() throws Exception {

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
        SessionID sessionId = FixConfigUtils.getSessionID(settings, "FXCM");
        this.account = "01727399";

        DefaultLogonMessageHandler logonHandler = new DefaultLogonMessageHandler(settings);

        this.orderRegistry = Mockito.mock(OrderRegistry.class);
        FXCMFixOrderMessageHandler messageHandlerImpl = new FXCMFixOrderMessageHandler(this.orderRegistry, engine);
        this.messageHandler = Mockito.spy(messageHandlerImpl);

        DefaultFixApplication fixApplication = new DefaultFixApplication(sessionId, this.messageHandler, logonHandler,
                new DefaultFixSessionStateHolder("FXCM", this.eventDispatcher));

        setupSession(settings, sessionId, fixApplication);
    }

    @Test
    public void testMarketOrder() throws Exception {

        String orderId = Long.toHexString(System.currentTimeMillis());

        NewOrderSingle orderSingle = new NewOrderSingle();
        orderSingle.set(new ClOrdID(orderId));
        orderSingle.set(new Account(this.account));
        orderSingle.set(new Symbol("EUR/USD"));
        orderSingle.set(new Side(Side.BUY));
        orderSingle.set(new TransactTime(new Date()));
        orderSingle.set(new OrderQty(1000.0d));
        orderSingle.set(new OrdType(OrdType.MARKET));

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);
        family.setScale(3);

        Forex forex = new ForexImpl();
        forex.setSymbol("EUR.USD");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(forex);

        Mockito.when(this.orderRegistry.getByIntId(orderId)).thenReturn(order);

        this.session.send(orderSingle);

        Object event1 = this.eventQueue.poll(20, TimeUnit.SECONDS);
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals(orderId, orderStatus1.getIntId());
        Assert.assertNotNull(orderStatus1.getExtId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus1.getStatus());
        Assert.assertNotNull(orderStatus1.getExtId());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());

        Object event2 = this.eventQueue.poll(20, TimeUnit.SECONDS);
        Assert.assertTrue(event2 instanceof OrderStatus);
        OrderStatus orderStatus2 = (OrderStatus) event2;
        Assert.assertEquals(orderId, orderStatus2.getIntId());
        Assert.assertNotNull(orderStatus2.getExtId());
        Assert.assertEquals(Status.EXECUTED, orderStatus2.getStatus());
        Assert.assertNotNull(orderStatus2.getExtId());
        Assert.assertSame(order, orderStatus2.getOrder());
        Assert.assertEquals(1000L, orderStatus2.getFilledQuantity());

        Object event3 = this.eventQueue.poll(20, TimeUnit.SECONDS);
        Assert.assertTrue(event3 instanceof Fill);
        Fill fill1 = (Fill) event3;
        Assert.assertEquals(orderStatus2.getExtId(), fill1.getExtId());
        Assert.assertSame(order, fill1.getOrder());
        Assert.assertNotNull(fill1.getExtDateTime());
        Assert.assertEquals(ch.algotrader.enumeration.Side.BUY, fill1.getSide());
        Assert.assertEquals(1000L, fill1.getQuantity());
        Assert.assertNotNull(fill1.getPrice());

        Object event4 = this.eventQueue.poll(20, TimeUnit.SECONDS);
        Assert.assertTrue(event4 instanceof OrderStatus);
        OrderStatus orderStatus3 = (OrderStatus) event4;
        Assert.assertEquals(orderId, orderStatus3.getIntId());
        Assert.assertNotNull(orderStatus3.getExtId());
        Assert.assertEquals(Status.EXECUTED, orderStatus3.getStatus());
        Assert.assertNotNull(orderStatus3.getExtId());
        Assert.assertSame(order, orderStatus3.getOrder());
        Assert.assertEquals(1000L, orderStatus3.getFilledQuantity());

        Object event5 = this.eventQueue.poll(5, TimeUnit.SECONDS);
        Assert.assertNull(event5);
    }

    @Test
    public void testOrderInvalidSymbol() throws Exception {

        String orderId = Long.toHexString(System.currentTimeMillis());

        NewOrderSingle orderSingle = new NewOrderSingle();
        orderSingle.set(new ClOrdID(orderId));
        orderSingle.set(new Account(this.account));
        orderSingle.set(new Symbol("STUFF"));
        orderSingle.set(new Side(Side.BUY));
        orderSingle.set(new TransactTime(new Date()));
        orderSingle.set(new OrderQty(1000.0d));
        orderSingle.set(new OrdType(OrdType.MARKET));

        this.session.send(orderSingle);

        Object event4 = this.eventQueue.poll(5, TimeUnit.SECONDS);
        Assert.assertNull(event4);
    }

    @Test
    public void testOrderUnsupportedSymbolPair() throws Exception {

        String orderId = Long.toHexString(System.currentTimeMillis());

        NewOrderSingle orderSingle = new NewOrderSingle();
        orderSingle.set(new ClOrdID(orderId));
        orderSingle.set(new Account(this.account));
        orderSingle.set(new Symbol("RUB/UAH"));
        orderSingle.set(new Side(Side.BUY));
        orderSingle.set(new TransactTime(new Date()));
        orderSingle.set(new OrderQty(1000.0d));
        orderSingle.set(new OrdType(OrdType.MARKET));

        this.session.send(orderSingle);

        Object event4 = this.eventQueue.poll(5, TimeUnit.SECONDS);
        Assert.assertNull(event4);
    }


    @Test
    public void testStopOrderCancel() throws Exception {

        String orderId1 = Long.toHexString(System.currentTimeMillis());

        NewOrderSingle orderSingle = new NewOrderSingle();
        orderSingle.set(new ClOrdID(orderId1));
        orderSingle.set(new Account(this.account));
        orderSingle.set(new Symbol("EUR/USD"));
        orderSingle.set(new Side(Side.BUY));
        orderSingle.set(new TransactTime(new Date()));
        orderSingle.set(new OrderQty(1000.0d));
        orderSingle.set(new OrdType(OrdType.STOP));
        orderSingle.set(new StopPx(2.0d));

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);
        family.setScale(3);

        Forex forex = new ForexImpl();
        forex.setSymbol("EUR.USD");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(forex);

        Mockito.when(this.orderRegistry.getByIntId(orderId1)).thenReturn(order);

        this.session.send(orderSingle);

        Object event1 = this.eventQueue.poll(20, TimeUnit.SECONDS);
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals(orderId1, orderStatus1.getIntId());
        Assert.assertNotNull(orderStatus1.getExtId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());

        String orderId2 = Long.toHexString(System.currentTimeMillis());

        OrderCancelRequest cancelRequest = new OrderCancelRequest();
        cancelRequest.set(new OrderID(orderStatus1.getExtId()));
        cancelRequest.set(new OrigClOrdID(orderId1));
        cancelRequest.set(new ClOrdID(orderId2));
        cancelRequest.set(new Account(this.account));
        cancelRequest.set(new Symbol("EUR/USD"));
        cancelRequest.set(new Side(Side.BUY));
        cancelRequest.set(new TransactTime(new Date()));
        cancelRequest.set(new OrderQty(1000.0d));

        Mockito.when(this.orderRegistry.getByIntId(orderId2)).thenReturn(order);

        this.session.send(cancelRequest);

        Object event2 = this.eventQueue.poll(20, TimeUnit.SECONDS);

        Assert.assertTrue(event2 instanceof OrderStatus);
        OrderStatus orderStatus2 = (OrderStatus) event2;
        Assert.assertEquals(orderId2, orderStatus2.getIntId());
        Assert.assertNotNull(orderStatus2.getExtId());
        Assert.assertEquals(Status.CANCELED, orderStatus2.getStatus());
        Assert.assertSame(order, orderStatus2.getOrder());
        Assert.assertEquals(0, orderStatus2.getFilledQuantity());
        Assert.assertEquals(1000L, orderStatus2.getRemainingQuantity());

        Object event3 = this.eventQueue.poll(5, TimeUnit.SECONDS);
        Assert.assertNull(event3);
    }

    @Test
    public void testStopOrderReplace() throws Exception {

        String orderId1 = Long.toHexString(System.currentTimeMillis());

        NewOrderSingle orderSingle = new NewOrderSingle();
        orderSingle.set(new ClOrdID(orderId1));
        orderSingle.set(new Account(this.account));
        orderSingle.set(new Symbol("EUR/USD"));
        orderSingle.set(new Side(Side.BUY));
        orderSingle.set(new TransactTime(new Date()));
        orderSingle.set(new OrderQty(1000.0d));
        orderSingle.set(new OrdType(OrdType.STOP));
        orderSingle.set(new StopPx(2.0d));

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);
        family.setScale(3);

        Forex forex = new ForexImpl();
        forex.setSymbol("EUR.USD");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(forex);

        Mockito.when(this.orderRegistry.getByIntId(orderId1)).thenReturn(order);

        this.session.send(orderSingle);

        Object event1 = this.eventQueue.poll(20, TimeUnit.SECONDS);
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals(orderId1, orderStatus1.getIntId());
        Assert.assertNotNull(orderStatus1.getExtId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());

        String orderId2 = Long.toHexString(System.currentTimeMillis());

        OrderCancelReplaceRequest replaceRequest = new OrderCancelReplaceRequest();
        replaceRequest.set(new OrderID(orderStatus1.getExtId()));
        replaceRequest.set(new OrigClOrdID(orderId1));
        replaceRequest.set(new ClOrdID(orderId2));
        replaceRequest.set(new Account(this.account));
        replaceRequest.set(new Symbol("EUR/USD"));
        replaceRequest.set(new Side(Side.BUY));
        replaceRequest.set(new TransactTime(new Date()));
        replaceRequest.set(new OrderQty(1000.0d));
        replaceRequest.set(new OrdType(OrdType.STOP));
        replaceRequest.set(new StopPx(1.9d));

        Mockito.when(this.orderRegistry.getByIntId(orderId2)).thenReturn(order);

        this.session.send(replaceRequest);

        Object event2 = this.eventQueue.poll(20, TimeUnit.SECONDS);

        Assert.assertTrue(event2 instanceof OrderStatus);
        OrderStatus orderStatus2 = (OrderStatus) event2;
        Assert.assertEquals(orderId2, orderStatus2.getIntId());
        Assert.assertNotNull(orderStatus2.getExtId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus2.getStatus());
        Assert.assertSame(order, orderStatus2.getOrder());
        Assert.assertEquals(0, orderStatus2.getFilledQuantity());
        Assert.assertEquals(1000L, orderStatus2.getRemainingQuantity());

        Object event3 = this.eventQueue.poll(5, TimeUnit.SECONDS);
        Assert.assertNull(event3);
    }

}
