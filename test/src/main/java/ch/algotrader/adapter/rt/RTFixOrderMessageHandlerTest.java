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
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ch.algotrader.adapter.fix.DefaultFixApplication;
import ch.algotrader.adapter.fix.DefaultFixSessionStateHolder;
import ch.algotrader.adapter.fix.FixApplicationTestBase;
import ch.algotrader.adapter.fix.FixConfigUtils;
import ch.algotrader.adapter.fix.fix44.GenericFix44SymbologyResolver;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.AccountImpl;
import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.exchange.ExchangeImpl;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.FutureImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.security.Stock;
import ch.algotrader.entity.security.StockImpl;
import ch.algotrader.entity.strategy.StrategyImpl;
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
import ch.algotrader.esper.AbstractEngine;
import ch.algotrader.esper.Engine;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.service.OrderExecutionService;
import ch.algotrader.service.TransactionService;
import ch.algotrader.util.DateTimeLegacy;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelRequest;

public class RTFixOrderMessageHandlerTest extends FixApplicationTestBase {

    private LinkedBlockingQueue<Object> eventQueue;
    private EventDispatcher eventDispatcher;
    private OrderExecutionService orderExecutionService;
    private TransactionService transactionService;
    private RTFixOrderMessageFactory messageFactory;
    private RTFixOrderMessageHandler messageHandler;

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
        SessionID sessionId = FixConfigUtils.getSessionID(settings, "RT");

        this.orderExecutionService = Mockito.mock(OrderExecutionService.class);
        this.transactionService = Mockito.mock(TransactionService.class);
        RTFixOrderMessageHandler messageHandlerImpl = new RTFixOrderMessageHandler(this.orderExecutionService, this.transactionService, engine);
        this.messageHandler = Mockito.spy(messageHandlerImpl);
        this.messageFactory = new RTFixOrderMessageFactory(new GenericFix44SymbologyResolver());

        DefaultFixApplication fixApplication = new DefaultFixApplication(sessionId, this.messageHandler, null,
                new DefaultFixSessionStateHolder("RT", this.eventDispatcher));

        setupSession(settings, sessionId, fixApplication);
    }

    @Test
    public void testMarketOrderForex() throws Exception {

        String orderId = Long.toHexString(System.currentTimeMillis());

        SecurityFamily securityFamily = new SecurityFamilyImpl();
        securityFamily.setCurrency(Currency.USD);

        Stock stock = new StockImpl();
        stock.setSymbol("MSFT");
        stock.setSecurityFamily(securityFamily);

        Account testAccount = new AccountImpl();
        testAccount.setBroker(Broker.RT.name());
        testAccount.setExtAccount("20580736-2");

        MarketOrder order = new MarketOrderImpl();
        order.setAccount(testAccount);
        order.setSecurity(stock);
        order.setQuantity(100);
        order.setSide(Side.BUY);

        NewOrderSingle message = this.messageFactory.createNewOrderMessage(order, orderId);

        Mockito.when(this.orderExecutionService.getOpenOrderByIntId(orderId)).thenReturn(order);

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
        Assert.assertEquals(100, orderStatus2.getFilledQuantity());

        Object event3 = this.eventQueue.poll(20, TimeUnit.SECONDS);
        Assert.assertTrue(event3 instanceof Fill);
        Fill fill1 = (Fill) event3;
        Assert.assertEquals(orderStatus2.getExtId(), fill1.getExtId());
        Assert.assertSame(order, fill1.getOrder());
        Assert.assertNotNull(fill1.getExtDateTime());
        Assert.assertEquals(ch.algotrader.enumeration.Side.BUY, fill1.getSide());
        Assert.assertEquals(100L, fill1.getQuantity());
        Assert.assertNotNull(fill1.getPrice());

        Object event4 = this.eventQueue.poll(5, TimeUnit.SECONDS);
        Assert.assertNull(event4);
    }

    @Test
    public void testMarketOrderFuture() throws Exception {

        String orderId = Long.toHexString(System.currentTimeMillis());

        Exchange exchange = new ExchangeImpl();
        exchange.setCode("XEUR");

        SecurityFamily securityFamily = new SecurityFamilyImpl();
        securityFamily.setSymbolRoot("DU");
        securityFamily.setCurrency(Currency.EUR);
        securityFamily.setExchange(exchange);

        Future future = new FutureImpl();
        future.setSecurityFamily(securityFamily);
        future.setExpiration(DateTimeLegacy.parseAsLocalDate("2014-12-01"));
        future.setMonthYear("201412");

        Account testAccount = new AccountImpl();
        testAccount.setBroker(Broker.RT.name());

        long totalQuantity = 10L;

        MarketOrder order = new MarketOrderImpl();
        order.setAccount(testAccount);
        order.setSecurity(future);
        order.setSide(Side.BUY);
        order.setQuantity(totalQuantity);

        NewOrderSingle message = this.messageFactory.createNewOrderMessage(order, orderId);

        Mockito.when(this.orderExecutionService.getOpenOrderByIntId(orderId)).thenReturn(order);

        this.session.send(message);

        Object event1 = this.eventQueue.poll(1, TimeUnit.MINUTES);
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals(orderId, orderStatus1.getIntId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus1.getStatus());
        Assert.assertNotNull(orderStatus1.getExtId());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());

        long totalFilled = 0;
        while (totalFilled < totalQuantity) {

            Object event2 = this.eventQueue.poll(1, TimeUnit.MINUTES);

            Assert.assertTrue(event2 instanceof OrderStatus);
            OrderStatus orderStatus2 = (OrderStatus) event2;
            Assert.assertEquals(orderId, orderStatus2.getIntId());
            Assert.assertNotNull(orderStatus2.getExtId());
            Assert.assertTrue(orderStatus2.getStatus() == Status.EXECUTED || orderStatus2.getStatus() == Status.PARTIALLY_EXECUTED);
            Assert.assertSame(order, orderStatus2.getOrder());

            Object event3 = this.eventQueue.poll(1, TimeUnit.MINUTES);

            Assert.assertTrue(event3 instanceof Fill);
            Fill fill1 = (Fill) event3;
            Assert.assertEquals(orderStatus2.getExtId(), fill1.getExtId());
            Assert.assertSame(order, fill1.getOrder());
            Assert.assertNotNull(fill1.getExtDateTime());
            Assert.assertEquals(Side.BUY, fill1.getSide());
            Assert.assertNotNull(fill1.getPrice());

            totalFilled += fill1.getQuantity();
        }

        Object event4 = this.eventQueue.poll(5, TimeUnit.SECONDS);
        Assert.assertNull(event4);
    }


    @Test
    public void testInvalidOrder() throws Exception {

        String orderId = Long.toHexString(System.currentTimeMillis());

        SecurityFamily securityFamily = new SecurityFamilyImpl();
        securityFamily.setCurrency(Currency.USD);

        Stock stock = new StockImpl();
        stock.setSymbol("grass");
        stock.setSecurityFamily(securityFamily);

        Account testAccount = new AccountImpl();
        testAccount.setBroker(Broker.RT.name());
        testAccount.setExtAccount("20580736-2");

        MarketOrder order = new MarketOrderImpl();
        order.setAccount(testAccount);
        order.setSecurity(stock);
        order.setQuantity(100);
        order.setSide(Side.BUY);

        NewOrderSingle message = this.messageFactory.createNewOrderMessage(order, orderId);

        this.session.send(message);

        Object event4 = this.eventQueue.poll(5, TimeUnit.SECONDS);
        Assert.assertNull(event4);
    }

    @Test
    public void testLimitOrderCancel() throws Exception {

        String orderId1 = Long.toHexString(System.currentTimeMillis());

        SecurityFamily securityFamily = new SecurityFamilyImpl();
        securityFamily.setCurrency(Currency.USD);

        Stock stock = new StockImpl();
        stock.setSymbol("MSFT");
        stock.setSecurityFamily(securityFamily);

        Account testAccount = new AccountImpl();
        testAccount.setBroker(Broker.RT.name());
        testAccount.setExtAccount("20580736-2");

        LimitOrder order = new LimitOrderImpl();
        order.setAccount(testAccount);
        order.setSecurity(stock);
        order.setQuantity(100);
        order.setSide(Side.BUY);
        order.setLimit(new BigDecimal("35.0"));

        NewOrderSingle message1 = this.messageFactory.createNewOrderMessage(order, orderId1);

        Mockito.when(this.orderExecutionService.getOpenOrderByIntId(orderId1)).thenReturn(order);

        this.session.send(message1);

        Object event1 = this.eventQueue.poll(20, TimeUnit.SECONDS);
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals(orderId1, orderStatus1.getIntId());
        Assert.assertNotNull(orderStatus1.getExtId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());

        String orderId2 = Long.toHexString(System.currentTimeMillis());

        order.setIntId(orderId1);
        OrderCancelRequest message2 = this.messageFactory.createOrderCancelMessage(order, orderId2);

        Mockito.when(this.orderExecutionService.getOpenOrderByIntId(orderId2)).thenReturn(order);

        this.session.send(message2);

        Object event2 = this.eventQueue.poll(20, TimeUnit.SECONDS);

        Assert.assertTrue(event2 instanceof OrderStatus);
        OrderStatus orderStatus2 = (OrderStatus) event2;
        Assert.assertEquals(orderId2, orderStatus2.getIntId());
        Assert.assertNotNull(orderStatus2.getExtId());
        Assert.assertEquals(Status.CANCELED, orderStatus2.getStatus());
        Assert.assertSame(order, orderStatus2.getOrder());
        Assert.assertEquals(0, orderStatus2.getFilledQuantity());
        Assert.assertEquals(100, orderStatus2.getRemainingQuantity());

        Object event3 = this.eventQueue.poll(5, TimeUnit.SECONDS);
        Assert.assertNull(event3);
    }

}
