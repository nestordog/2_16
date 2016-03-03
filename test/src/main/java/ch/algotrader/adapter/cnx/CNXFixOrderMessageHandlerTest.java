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
package ch.algotrader.adapter.cnx;

import java.math.BigDecimal;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ch.algotrader.adapter.MockOrderExecutionService;
import ch.algotrader.adapter.fix.DefaultFixApplication;
import ch.algotrader.adapter.fix.DefaultFixSessionStateHolder;
import ch.algotrader.adapter.fix.DefaultLogonMessageHandler;
import ch.algotrader.adapter.fix.FixApplicationTestBase;
import ch.algotrader.adapter.fix.FixConfigUtils;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.AccountImpl;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.LimitOrderImpl;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.MarketOrderImpl;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.ordermgmt.DefaultOrderBook;
import ch.algotrader.ordermgmt.OrderBook;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

public class CNXFixOrderMessageHandlerTest extends FixApplicationTestBase {

    private LinkedBlockingQueue<Object> eventQueue;
    private OrderBook orderBook;
    private MockOrderExecutionService orderExecutionService;
    private CNXFixOrderMessageFactory messageFactory;
    private CNXFixOrderMessageHandler messageHandler;

    @Before
    public void setup() throws Exception {

        this.eventQueue = new LinkedBlockingQueue<>();
        this.orderBook = new DefaultOrderBook();
        this.orderExecutionService = new MockOrderExecutionService(this.eventQueue, this.orderBook);

        SessionSettings settings = FixConfigUtils.loadSettings();
        SessionID sessionId = FixConfigUtils.getSessionID(settings, "CNXT");

        CNXFixOrderMessageHandler messageHandlerImpl = new CNXFixOrderMessageHandler(this.orderExecutionService);
        this.messageHandler = Mockito.spy(messageHandlerImpl);
        this.messageFactory = new CNXFixOrderMessageFactory();

        DefaultLogonMessageHandler logonMessageHandler = new DefaultLogonMessageHandler(settings);
        DefaultFixApplication fixApplication = new DefaultFixApplication(sessionId, this.messageHandler, logonMessageHandler,
                new DefaultFixSessionStateHolder("CNX", Mockito.mock(EventDispatcher.class)));

        setupSession(settings, sessionId, fixApplication);
    }

    @Test
    public void testNewOrder() throws Exception {

        SecurityFamily securityFamily = new SecurityFamilyImpl();
        securityFamily.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(securityFamily);

        Account testAccount = new AccountImpl();
        testAccount.setBroker(Broker.CNX.name());

        MarketOrder order = new MarketOrderImpl();
        order.setAccount(testAccount);
        order.setSecurity(forex);
        order.setSide(Side.BUY);
        order.setQuantity(2000);

        String orderId = Long.toHexString(System.currentTimeMillis());
        order.setIntId(orderId);

        NewOrderSingle message = this.messageFactory.createNewOrderMessage(order, orderId);

        this.orderBook.add(order);

        this.session.send(message);

        Object event1 = this.eventQueue.poll(20, TimeUnit.SECONDS);
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals(orderId, orderStatus1.getIntId());
        Assert.assertNotNull(orderStatus1.getExtId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());

        Object event2 = this.eventQueue.poll(20, TimeUnit.SECONDS);
        Assert.assertTrue(event2 instanceof OrderStatus);
        OrderStatus orderStatus2 = (OrderStatus) event2;
        Assert.assertEquals(orderId, orderStatus2.getIntId());
        Assert.assertNotNull(orderStatus2.getExtId());
        Assert.assertEquals(Status.EXECUTED, orderStatus2.getStatus());
        Assert.assertSame(order, orderStatus2.getOrder());
        Assert.assertEquals(2000L, orderStatus2.getFilledQuantity());

        Object event3 = this.eventQueue.poll(20, TimeUnit.SECONDS);
        Assert.assertTrue(event3 instanceof Fill);
        Fill fill1 = (Fill) event3;
        Assert.assertEquals(orderStatus2.getExtId(), fill1.getExtId());
        Assert.assertSame(order, fill1.getOrder());
        Assert.assertNotNull(fill1.getExtDateTime());
        Assert.assertEquals(Side.BUY, fill1.getSide());
        Assert.assertEquals(2000L, fill1.getQuantity());
        Assert.assertNotNull(fill1.getPrice());

        Object event4 = this.eventQueue.poll(5, TimeUnit.SECONDS);
        Assert.assertNull(event4);
    }

    @Test
    public void testInvalidOrder() throws Exception {

        SecurityFamily securityFamily = new SecurityFamilyImpl();
        securityFamily.setCurrency(Currency.RUB);

        Forex forex = new ForexImpl();
        forex.setBaseCurrency(Currency.INR);
        forex.setSecurityFamily(securityFamily);

        Account testAccount = new AccountImpl();
        testAccount.setBroker(Broker.CNX.name());

        MarketOrder order = new MarketOrderImpl();
        order.setAccount(testAccount);
        order.setSecurity(forex);
        order.setQuantity(3000);
        order.setSide(Side.BUY);

        String orderId = Long.toHexString(System.currentTimeMillis());
        order.setIntId(orderId);

        NewOrderSingle message = this.messageFactory.createNewOrderMessage(order, orderId);

        this.session.send(message);

        Object event4 = this.eventQueue.poll(5, TimeUnit.SECONDS);
        Assert.assertNull(event4);
    }

    @Test
    public void testLimitOrderCancel() throws Exception {

        SecurityFamily securityFamily = new SecurityFamilyImpl();
        securityFamily.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(securityFamily);

        Account testAccount = new AccountImpl();
        testAccount.setBroker(Broker.CNX.name());

        LimitOrder order = new LimitOrderImpl();
        order.setAccount(testAccount);
        order.setSecurity(forex);
        order.setQuantity(1000);
        order.setSide(Side.BUY);
        order.setLimit(new BigDecimal("1.0"));

        String orderId = Long.toHexString(System.currentTimeMillis());
        order.setIntId(orderId);

        NewOrderSingle message1 = this.messageFactory.createNewOrderMessage(order, orderId);

        this.orderBook.add(order);

        this.session.send(message1);

        Object event1 = this.eventQueue.poll(20, TimeUnit.SECONDS);
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals(orderId, orderStatus1.getIntId());
        Assert.assertNotNull(orderStatus1.getExtId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());

        order.setIntId(orderStatus1.getIntId());
        order.setExtId(orderStatus1.getExtId());

        String orderId2 = Long.toHexString(System.currentTimeMillis());

        OrderCancelRequest message2 = this.messageFactory.createOrderCancelMessage(order, orderId2);

        this.session.send(message2);

        Object event2 = this.eventQueue.poll(20, TimeUnit.SECONDS);

        Assert.assertTrue(event2 instanceof OrderStatus);
        OrderStatus orderStatus2 = (OrderStatus) event2;
        Assert.assertEquals(orderId2, orderStatus2.getIntId());
        Assert.assertNotNull(orderStatus2.getExtId());
        Assert.assertEquals(Status.CANCELED, orderStatus2.getStatus());
        Assert.assertSame(order, orderStatus2.getOrder());
        Assert.assertEquals(0, orderStatus2.getFilledQuantity());
        Assert.assertEquals(1000, orderStatus2.getRemainingQuantity());

        Object event3 = this.eventQueue.poll(5, TimeUnit.SECONDS);
        Assert.assertNull(event3);
    }

    @Test
    public void testLimitOrderModifyCancel() throws Exception {

        SecurityFamily securityFamily = new SecurityFamilyImpl();
        securityFamily.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(securityFamily);

        Account testAccount = new AccountImpl();
        testAccount.setBroker(Broker.CNX.name());

        LimitOrder order = new LimitOrderImpl();
        order.setAccount(testAccount);
        order.setSecurity(forex);
        order.setQuantity(1000);
        order.setSide(Side.BUY);
        order.setLimit(new BigDecimal("1.0"));

        String orderId = Long.toHexString(System.currentTimeMillis());
        order.setIntId(orderId);;

        NewOrderSingle message1 = this.messageFactory.createNewOrderMessage(order, orderId);

        this.orderBook.add(order);

        this.session.send(message1);

        Object event1 = this.eventQueue.poll(20, TimeUnit.SECONDS);
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals(orderId, orderStatus1.getIntId());
        Assert.assertNotNull(orderStatus1.getExtId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());

        order.setIntId(orderStatus1.getIntId());
        order.setExtId(orderStatus1.getExtId());

        LimitOrder modifiedOrder = new LimitOrderImpl();
        modifiedOrder.setAccount(testAccount);
        modifiedOrder.setSecurity(forex);
        modifiedOrder.setQuantity(1000);
        modifiedOrder.setSide(Side.BUY);
        modifiedOrder.setLimit(new BigDecimal("1.01"));

        String orderId2 = Long.toHexString(System.currentTimeMillis());
        modifiedOrder.setIntId(orderId2);

        this.orderBook.add(modifiedOrder);

        OrderCancelReplaceRequest message2 = this.messageFactory.createModifyOrderMessage(modifiedOrder, orderId2);

        this.session.send(message2);

        Object event2 = this.eventQueue.poll(20, TimeUnit.SECONDS);

        Assert.assertTrue(event2 instanceof OrderStatus);
        OrderStatus orderStatus2 = (OrderStatus) event2;
        Assert.assertEquals(orderId2, orderStatus2.getIntId());
        Assert.assertNotNull(orderStatus2.getExtId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus2.getStatus());
        Assert.assertSame(modifiedOrder, orderStatus2.getOrder());
        Assert.assertEquals(0, orderStatus2.getFilledQuantity());
        Assert.assertEquals(1000, orderStatus2.getRemainingQuantity());

        modifiedOrder.setIntId(orderStatus2.getIntId());
        modifiedOrder.setExtId(orderStatus2.getExtId());

        String orderId3 = Long.toHexString(System.currentTimeMillis());

        OrderCancelRequest message3 = this.messageFactory.createOrderCancelMessage(modifiedOrder, orderId3);

        this.session.send(message3);

        Object event3 = this.eventQueue.poll(20, TimeUnit.SECONDS);

        Assert.assertTrue(event3 instanceof OrderStatus);
        OrderStatus orderStatus3 = (OrderStatus) event3;
        Assert.assertEquals(orderId3, orderStatus3.getIntId());
        Assert.assertNotNull(orderStatus3.getExtId());
        Assert.assertEquals(Status.CANCELED, orderStatus3.getStatus());
        Assert.assertSame(modifiedOrder, orderStatus3.getOrder());
        Assert.assertEquals(0, orderStatus3.getFilledQuantity());
        Assert.assertEquals(1000, orderStatus3.getRemainingQuantity());

        Object event4 = this.eventQueue.poll(5, TimeUnit.SECONDS);
        Assert.assertNull(event4);
    }

}
