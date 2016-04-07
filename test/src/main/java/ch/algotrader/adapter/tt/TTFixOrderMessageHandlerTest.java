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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ch.algotrader.adapter.MockOrderExecutionService;
import ch.algotrader.adapter.fix.DefaultFixApplication;
import ch.algotrader.adapter.fix.DefaultFixSessionStateHolder;
import ch.algotrader.adapter.fix.FixApplicationTestBase;
import ch.algotrader.adapter.fix.FixConfigUtils;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.FutureFamily;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.ExpirationType;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.ordermgmt.DefaultOrderBook;
import ch.algotrader.ordermgmt.OrderBook;
import ch.algotrader.service.LookupService;
import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.util.DateTimeUtil;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelReplaceRequest;
import quickfix.fix42.OrderCancelRequest;

public class TTFixOrderMessageHandlerTest extends FixApplicationTestBase {

    private LinkedBlockingQueue<Object> eventQueue;
    private OrderBook orderBook;
    private MockOrderExecutionService orderExecutionService;
    private Future future;
    private Account account;
    private TTFixOrderMessageFactory messageFactory;
    private TTFixOrderMessageHandler messageHandler;

    @Before
    public void setup() throws Exception {

        Exchange exchange = Exchange.Factory.newInstance();
        exchange.setName("CME");
        exchange.setCode("CME");
        exchange.setTimeZone("US/Central");

        FutureFamily futureFamily = FutureFamily.Factory.newInstance();
        futureFamily.setSymbolRoot("CL");
        futureFamily.setExpirationType(ExpirationType.NEXT_3_RD_MONDAY_3_MONTHS);
        futureFamily.setCurrency(Currency.USD);
        futureFamily.setExchange(exchange);
        futureFamily.setTickSizePattern("0<0.01");

        this.future = Future.Factory.newInstance();
        this.future.setId(1L);
        this.future.setSymbol("CL NOV/16");
        this.future.setTtid("00A0KP00CLZ");
        this.future.setSecurityFamily(futureFamily);
        this.future.setExpiration(DateTimeLegacy.toLocalDate(DateTimeUtil.parseLocalDate("2015-11-01")));
        this.future.setMonthYear("201511");

        this.account = Account.Factory.newInstance();
        this.account.setName("TT_TEST");
        this.account.setExtAccount("ratkodts2");
        this.account.setBroker(Broker.TT.name());

        this.eventQueue = new LinkedBlockingQueue<>();
        this.orderBook = new DefaultOrderBook();
        this.orderExecutionService = new MockOrderExecutionService(this.eventQueue, this.orderBook);

        SessionSettings settings = FixConfigUtils.loadSettings();
        SessionID sessionId = FixConfigUtils.getSessionID(settings, "TTT");

        TTLogonMessageHandler logonHandler = new TTLogonMessageHandler(settings);

        this.messageFactory = new TTFixOrderMessageFactory();

        TTFixOrderMessageHandler messageHandlerImpl = new TTFixOrderMessageHandler(this.orderExecutionService, Mockito.mock(LookupService.class), null);
        this.messageHandler = Mockito.spy(messageHandlerImpl);

        DefaultFixApplication fixApplication = new DefaultFixApplication(sessionId, this.messageHandler, logonHandler,
                new DefaultFixSessionStateHolder("TTT", Mockito.mock(EventDispatcher.class)));

        setupSession(settings, sessionId, fixApplication);

        while (this.eventQueue.poll(1, TimeUnit.SECONDS) != null) {
        }
    }

    @Test
    public void testMarketOrder() throws Exception {

        MarketOrder order = MarketOrder.Factory.newInstance();
        order.setQuantity(1L);
        order.setSide(Side.BUY);
        order.setSecurity(this.future);
        order.setAccount(this.account);

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
        Assert.assertEquals(1, orderStatus2.getFilledQuantity());

        Object event3 = this.eventQueue.poll(20, TimeUnit.SECONDS);
        Assert.assertTrue(event3 instanceof Fill);
        Fill fill1 = (Fill) event3;
        Assert.assertSame(order, fill1.getOrder());
        Assert.assertNotNull(fill1.getExtDateTime());
        Assert.assertEquals(ch.algotrader.enumeration.Side.BUY, fill1.getSide());
        Assert.assertEquals(1, fill1.getQuantity());
        Assert.assertNotNull(fill1.getPrice());

        Object event4 = this.eventQueue.poll(5, TimeUnit.SECONDS);
        Assert.assertNull(event4);
    }

    @Test
    public void testLimitOrderCancel() throws Exception {

        String orderId1 = Long.toHexString(System.currentTimeMillis());

        LimitOrder order = LimitOrder.Factory.newInstance();
        order.setQuantity(1L);
        order.setIntId(orderId1);
        order.setSide(Side.SELL);
        order.setLimit(new BigDecimal("4800"));
        order.setSecurity(this.future);
        order.setAccount(this.account);

        this.orderBook.add(order);

        NewOrderSingle message = this.messageFactory.createNewOrderMessage(order, orderId1);

        this.session.send(message);

        Object event1 = this.eventQueue.poll(20, TimeUnit.SECONDS);
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals(orderId1, orderStatus1.getIntId());
        Assert.assertNotNull(orderStatus1.getExtId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());

        String orderId2 = Long.toHexString(System.currentTimeMillis());

        OrderCancelRequest cancelRequest = this.messageFactory.createOrderCancelMessage(order, orderId2);

        this.session.send(cancelRequest);

        Object event2 = this.eventQueue.poll(20, TimeUnit.SECONDS);

        Assert.assertTrue(event2 instanceof OrderStatus);
        OrderStatus orderStatus2 = (OrderStatus) event2;
        Assert.assertEquals(orderId1, orderStatus2.getIntId());
        Assert.assertNotNull(orderStatus2.getExtId());
        Assert.assertEquals(Status.CANCELED, orderStatus2.getStatus());
        Assert.assertSame(order, orderStatus2.getOrder());
        Assert.assertEquals(0, orderStatus2.getFilledQuantity());
        Assert.assertEquals(1, orderStatus2.getRemainingQuantity());

        Object event3 = this.eventQueue.poll(5, TimeUnit.SECONDS);
        Assert.assertNull(event3);
    }

    @Test
    public void testLimitOrderReplaceThenCancel() throws Exception {

        String orderId1 = Long.toHexString(System.currentTimeMillis());

        LimitOrder order1 = LimitOrder.Factory.newInstance();
        order1.setQuantity(1L);
        order1.setSide(Side.SELL);
        order1.setLimit(new BigDecimal("4800"));
        order1.setSecurity(this.future);
        order1.setAccount(this.account);
        order1.setIntId(orderId1);

        this.orderBook.add(order1);

        NewOrderSingle message = this.messageFactory.createNewOrderMessage(order1, orderId1);

        this.session.send(message);

        Object event1 = this.eventQueue.poll(20, TimeUnit.SECONDS);
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals(orderId1, orderStatus1.getIntId());
        Assert.assertNotNull(orderStatus1.getExtId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus1.getStatus());
        Assert.assertSame(order1, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());

        String orderId2 = Long.toHexString(System.currentTimeMillis());

        LimitOrder order2 = LimitOrder.Factory.newInstance();
        order2.setIntId(orderId2);
        order2.setQuantity(1L);
        order2.setSide(Side.SELL);
        order2.setLimit(new BigDecimal("4805"));
        order2.setSecurity(this.future);
        order2.setAccount(this.account);
        order2.setIntId(orderId2);

        OrderCancelReplaceRequest replaceRequest = this.messageFactory.createModifyOrderMessage(order2, orderId2);

        this.session.send(replaceRequest);

        Object event2 = this.eventQueue.poll(20, TimeUnit.SECONDS);

        Assert.assertTrue(event2 instanceof OrderStatus);
        OrderStatus orderStatus2 = (OrderStatus) event2;
        Assert.assertEquals(orderId1, orderStatus2.getIntId());
        Assert.assertNull(orderStatus2.getExtId());
        Assert.assertEquals(Status.CANCELED, orderStatus2.getStatus());
        Assert.assertSame(order1, orderStatus2.getOrder());
        Assert.assertEquals(0, orderStatus2.getFilledQuantity());
        Assert.assertEquals(1, orderStatus2.getRemainingQuantity());

        Object event3 = this.eventQueue.poll(20, TimeUnit.SECONDS);

        Assert.assertTrue(event3 instanceof OrderStatus);
        OrderStatus orderStatus3 = (OrderStatus) event3;
        Assert.assertEquals(orderId2, orderStatus3.getIntId());
        Assert.assertNotNull(orderStatus3.getExtId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus3.getStatus());
        Assert.assertSame(order2, orderStatus3.getOrder());
        Assert.assertEquals(0, orderStatus3.getFilledQuantity());
        Assert.assertEquals(1, orderStatus3.getRemainingQuantity());

        String orderId3 = Long.toHexString(System.currentTimeMillis());
        OrderCancelRequest cancelRequest = this.messageFactory.createOrderCancelMessage(order2, orderId3);

        this.session.send(cancelRequest);

        Object event4 = this.eventQueue.poll(20, TimeUnit.SECONDS);

        Assert.assertTrue(event4 instanceof OrderStatus);
        OrderStatus orderStatus4 = (OrderStatus) event4;
        Assert.assertEquals(orderId2, orderStatus4.getIntId());
        Assert.assertNotNull(orderStatus4.getExtId());
        Assert.assertEquals(Status.CANCELED, orderStatus4.getStatus());
        Assert.assertSame(order2, orderStatus4.getOrder());
        Assert.assertEquals(0, orderStatus4.getFilledQuantity());
        Assert.assertEquals(1, orderStatus4.getRemainingQuantity());

        Object event5 = this.eventQueue.poll(5, TimeUnit.SECONDS);
        Assert.assertNull(event5);
    }

}
