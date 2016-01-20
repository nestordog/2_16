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

package ch.algotrader.service;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.CommonConfigBuilder;
import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.Stock;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.AlgoOrder;
import ch.algotrader.entity.trade.ExecutionStatusVO;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.FillVO;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.OrderStatusVO;
import ch.algotrader.entity.trade.SlicingOrder;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.ordermgmt.OrderBook;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class OrderExecutionServiceTest {

    @Mock
    private OrderPersistenceService orderPersistenceService;
    @Mock
    private OrderBook orderBook;
    @Mock
    private EventDispatcher eventDispatcher;
    @Mock
    private EngineManager engineManager;
    @Mock
    private Engine engine;

    private Strategy strategy;
    private SecurityFamily family;
    private Exchange exchange;
    private Stock stock;

    private OrderExecutionServiceImpl impl;

    @Before
    public void setup() throws Exception {

        MockitoAnnotations.initMocks(this);

        this.strategy = Strategy.Factory.newInstance();
        this.strategy.setName("TestStrategy");
        this.family = SecurityFamily.Factory.newInstance();
        this.family.setId(1);
        this.family.setSymbolRoot("Stocks");
        this.family.setCurrency(Currency.USD);
        this.family.setTickSizePattern("0<0.00005");
        this.family.setTradeable(true);
        this.family.setScale(4);
        this.exchange = Exchange.Factory.newInstance("exchange", "GMT");
        this.exchange.setId(5L);
        this.family.setExchange(this.exchange);

        this.stock = Stock.Factory.newInstance();
        this.stock.setId(1);
        this.stock.setSymbol("GOOG");
        this.stock.setSecurityFamily(this.family);

        CommonConfig commonConfig = CommonConfigBuilder.create().setSimulation(false).build();
        this.impl = new OrderExecutionServiceImpl(commonConfig, this.orderPersistenceService, this.orderBook,
                this.eventDispatcher, this.engineManager, this.engine);

    }

    @Test
    public void testOrderStatusPropagation() throws Exception {

        Order order = MarketOrder.Factory.newInstance();
        order.setIntId("Blah");
        order.setQuantity(25L);
        order.setStrategy(this.strategy);
        order.setSecurity(this.stock);

        OrderStatus orderStatus1 = OrderStatus.Factory.newInstance();
        orderStatus1.setStatus(Status.SUBMITTED);
        orderStatus1.setIntId("Blah");
        orderStatus1.setExtDateTime(new Date());
        orderStatus1.setDateTime(orderStatus1.getExtDateTime());
        orderStatus1.setFilledQuantity(0L);
        orderStatus1.setRemainingQuantity(25L);
        orderStatus1.setOrder(order);
        orderStatus1.setSequenceNumber(1);

        Mockito.when(this.orderBook.getOpenOrderByIntId("Blah")).thenReturn(order);

        this.impl.handleOrderStatus(orderStatus1);

        Mockito.verify(this.orderBook, Mockito.times(1)).updateExecutionStatus("Blah", null, Status.SUBMITTED, 0L, 25L);
        Mockito.verify(this.eventDispatcher, Mockito.times(1)).sendEvent(Mockito.eq("TestStrategy"), Mockito.any());
        Mockito.verify(this.orderPersistenceService, Mockito.times(1)).persistOrderStatus(orderStatus1);
        Mockito.verify(this.engine, Mockito.never()).sendEvent(Mockito.any());

    }

    @Test(expected = ServiceException.class)
    public void testOrderStatusPropagationOrderNotFound() throws Exception {

        OrderStatus orderStatus1 = OrderStatus.Factory.newInstance();
        orderStatus1.setStatus(Status.SUBMITTED);
        orderStatus1.setIntId("Blah");
        orderStatus1.setExtDateTime(new Date());
        orderStatus1.setDateTime(orderStatus1.getExtDateTime());
        orderStatus1.setFilledQuantity(0L);
        orderStatus1.setRemainingQuantity(25L);
        orderStatus1.setSequenceNumber(1);

        Mockito.when(this.orderBook.getOpenOrderByIntId("Blah")).thenReturn(null);

        this.impl.handleOrderStatus(orderStatus1);
    }

    @Test
    public void testOrderStatusPropagationDateTimeAttribute() throws Exception {

        Order order = MarketOrder.Factory.newInstance();
        order.setIntId("Blah");
        order.setQuantity(25L);
        order.setStrategy(this.strategy);
        order.setSecurity(this.stock);

        Date currentTime = new Date();
        Date pastTime = new Date(currentTime.getTime() - 10000);

        OrderStatus orderStatus1 = OrderStatus.Factory.newInstance();
        orderStatus1.setStatus(Status.SUBMITTED);
        orderStatus1.setIntId("Blah");
        orderStatus1.setExtDateTime(pastTime);
        orderStatus1.setDateTime(null);
        orderStatus1.setFilledQuantity(0L);
        orderStatus1.setRemainingQuantity(25L);
        orderStatus1.setOrder(order);
        orderStatus1.setSequenceNumber(1);

        Mockito.when(this.orderBook.getOpenOrderByIntId("Blah")).thenReturn(order);
        Mockito.when(this.engine.getCurrentTime()).thenReturn(currentTime);

        this.impl.handleOrderStatus(orderStatus1);

        Assert.assertEquals(pastTime, orderStatus1.getExtDateTime());
        Assert.assertEquals(pastTime, orderStatus1.getDateTime());
    }

    @Test
    public void testOrderStatusPropagationDateTimeAttribute2() throws Exception {

        Order order = MarketOrder.Factory.newInstance();
        order.setIntId("Blah");
        order.setQuantity(25L);
        order.setStrategy(this.strategy);
        order.setSecurity(this.stock);

        Date currentTime = new Date();

        OrderStatus orderStatus1 = OrderStatus.Factory.newInstance();
        orderStatus1.setStatus(Status.SUBMITTED);
        orderStatus1.setIntId("Blah");
        orderStatus1.setExtDateTime(null);
        orderStatus1.setDateTime(null);
        orderStatus1.setFilledQuantity(0L);
        orderStatus1.setRemainingQuantity(25L);
        orderStatus1.setOrder(order);
        orderStatus1.setSequenceNumber(1);

        Mockito.when(this.orderBook.getOpenOrderByIntId("Blah")).thenReturn(order);
        Mockito.when(this.engine.getCurrentTime()).thenReturn(currentTime);

        this.impl.handleOrderStatus(orderStatus1);

        Assert.assertEquals(null, orderStatus1.getExtDateTime());
        Assert.assertEquals(currentTime, orderStatus1.getDateTime());
    }

    @Test
    public void testAlgoOrderSubmittedPropagation() throws Exception {

        AlgoOrder algoOrder = new SlicingOrder();
        algoOrder.setIntId("a1.0");

        Order order = MarketOrder.Factory.newInstance();
        order.setIntId("Blah");
        order.setQuantity(25L);
        order.setStrategy(this.strategy);
        order.setSecurity(this.stock);
        order.setParentOrder(algoOrder);

        OrderStatus orderStatus1 = OrderStatus.Factory.newInstance();
        orderStatus1.setStatus(Status.SUBMITTED);
        orderStatus1.setIntId("Blah");
        orderStatus1.setExtDateTime(new Date());
        orderStatus1.setDateTime(orderStatus1.getExtDateTime());
        orderStatus1.setFilledQuantity(0L);
        orderStatus1.setRemainingQuantity(25L);
        orderStatus1.setOrder(order);
        orderStatus1.setSequenceNumber(1);

        Date currentTime = new Date();

        Mockito.when(this.orderBook.getOpenOrderByIntId("Blah")).thenReturn(order);
        Mockito.when(this.orderBook.getOpenOrderByIntId("a1.0")).thenReturn(algoOrder);
        Mockito.when(this.orderBook.getStatusByIntId("a1.0")).thenReturn(new ExecutionStatusVO("a1.0", Status.OPEN, 1L, 24L, null));
        Mockito.when(this.engine.getCurrentTime()).thenReturn(currentTime);

        this.impl.handleOrderStatus(orderStatus1);

        Mockito.verify(this.orderBook, Mockito.times(1)).updateExecutionStatus("Blah", null, Status.SUBMITTED, 0L, 25L);
        Mockito.verify(this.orderBook, Mockito.times(1)).updateExecutionStatus("a1.0", null, Status.SUBMITTED, 0L, 24L);

        ArgumentCaptor<Object> argumentCaptor1 = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.eventDispatcher, Mockito.times(2)).sendEvent(Mockito.eq("TestStrategy"), argumentCaptor1.capture());

        List<Object> events = argumentCaptor1.getAllValues();
        Assert.assertNotNull(events);
        Assert.assertEquals(2, events.size());
        Object event1 = events.get(0);
        Assert.assertTrue(event1 instanceof OrderStatusVO);
        OrderStatusVO orderStatus2 = (OrderStatusVO) event1;
        Assert.assertEquals("Blah", orderStatus2.getIntId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus2.getStatus());
        Assert.assertEquals(0L, orderStatus2.getFilledQuantity());
        Assert.assertEquals(25L, orderStatus2.getRemainingQuantity());
        Object event2 = events.get(1);
        Assert.assertTrue(event2 instanceof OrderStatusVO);
        OrderStatusVO orderStatus3 = (OrderStatusVO) event2;
        Assert.assertEquals("a1.0", orderStatus3.getIntId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus3.getStatus());
        Assert.assertEquals(0L, orderStatus3.getFilledQuantity());
        Assert.assertEquals(24L, orderStatus3.getRemainingQuantity());

        ArgumentCaptor<Object> argumentCaptor2 = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.engine, Mockito.times(1)).sendEvent(argumentCaptor2.capture());
        Object event3 = argumentCaptor2.getValue();
        Assert.assertTrue(event3 instanceof OrderStatus);
        OrderStatus orderStatus4 = (OrderStatus) event3;
        Assert.assertEquals("a1.0", orderStatus4.getIntId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus4.getStatus());
        Assert.assertEquals(0L, orderStatus4.getFilledQuantity());
        Assert.assertEquals(24L, orderStatus4.getRemainingQuantity());

        Mockito.verify(this.orderPersistenceService, Mockito.times(1)).persistOrderStatus(orderStatus1);

    }

    @Test
    public void testAlgoOrderCanceledPropagation() throws Exception {

        AlgoOrder algoOrder = new SlicingOrder();
        algoOrder.setIntId("a1.0");

        Order order = MarketOrder.Factory.newInstance();
        order.setIntId("Blah");
        order.setQuantity(25L);
        order.setStrategy(this.strategy);
        order.setSecurity(this.stock);
        order.setParentOrder(algoOrder);

        OrderStatus orderStatus1 = OrderStatus.Factory.newInstance();
        orderStatus1.setStatus(Status.CANCELED);
        orderStatus1.setIntId("Blah");
        orderStatus1.setExtDateTime(new Date());
        orderStatus1.setDateTime(orderStatus1.getExtDateTime());
        orderStatus1.setFilledQuantity(0L);
        orderStatus1.setRemainingQuantity(25L);
        orderStatus1.setOrder(order);
        orderStatus1.setSequenceNumber(1);

        Date currentTime = new Date();

        Mockito.when(this.orderBook.getOpenOrderByIntId("Blah")).thenReturn(order);
        Mockito.when(this.orderBook.getOpenOrderByIntId("a1.0")).thenReturn(algoOrder);
        Mockito.when(this.orderBook.getStatusByIntId("a1.0")).thenReturn(new ExecutionStatusVO("a1.0", Status.SUBMITTED, 1L, 24L, null));
        Mockito.when(this.engine.getCurrentTime()).thenReturn(currentTime);

        this.impl.handleOrderStatus(orderStatus1);

        Mockito.verify(this.orderBook, Mockito.times(1)).updateExecutionStatus("Blah", null, Status.CANCELED, 0L, 25L);
        Mockito.verify(this.orderBook, Mockito.never()).updateExecutionStatus(Mockito.eq("a1.0"), Mockito.anyString(), Mockito.any(), Mockito.anyLong(), Mockito.anyLong());

        ArgumentCaptor<Object> argumentCaptor1 = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.eventDispatcher, Mockito.times(1)).sendEvent(Mockito.eq("TestStrategy"), argumentCaptor1.capture());

        List<Object> events = argumentCaptor1.getAllValues();
        Assert.assertNotNull(events);
        Assert.assertEquals(1, events.size());
        Object event1 = events.get(0);
        Assert.assertTrue(event1 instanceof OrderStatusVO);
        OrderStatusVO orderStatus2 = (OrderStatusVO) event1;
        Assert.assertEquals("Blah", orderStatus2.getIntId());
        Assert.assertEquals(Status.CANCELED, orderStatus2.getStatus());
        Assert.assertEquals(0L, orderStatus2.getFilledQuantity());
        Assert.assertEquals(25L, orderStatus2.getRemainingQuantity());

        Mockito.verify(this.engine, Mockito.never()).sendEvent(Mockito.any());

        Mockito.verify(this.orderPersistenceService, Mockito.times(1)).persistOrderStatus(orderStatus1);

    }

    @Test
    public void testFillPropagation() throws Exception {

        Order order = MarketOrder.Factory.newInstance();
        order.setIntId("Blah");
        order.setQuantity(25L);
        order.setSide(Side.BUY);
        order.setStrategy(this.strategy);
        order.setSecurity(this.stock);

        Fill fill1 = new Fill();
        fill1.setExtDateTime(new Date());
        fill1.setDateTime(fill1.getExtDateTime());
        fill1.setQuantity(7L);
        fill1.setSide(Side.BUY);
        fill1.setOrder(order);
        fill1.setSequenceNumber(1);

        Mockito.when(this.orderBook.getOpenOrderByIntId("Blah")).thenReturn(order);

        this.impl.handleFill(fill1);

        Mockito.verify(this.eventDispatcher, Mockito.times(1)).sendEvent(Mockito.eq("TestStrategy"), Mockito.any());
        Mockito.verify(this.engine, Mockito.never()).sendEvent(Mockito.any());
    }

    @Test
    public void testAlgoPartialFillPropagation() throws Exception {

        AlgoOrder algoOrder = new SlicingOrder();
        algoOrder.setIntId("a1.0");

        Order order = MarketOrder.Factory.newInstance();
        order.setIntId("Blah");
        order.setQuantity(25L);
        order.setStrategy(this.strategy);
        order.setSecurity(this.stock);
        order.setParentOrder(algoOrder);

        Fill fill1 = new Fill();
        fill1.setExtDateTime(new Date());
        fill1.setDateTime(fill1.getExtDateTime());
        fill1.setQuantity(7L);
        fill1.setSide(Side.BUY);
        fill1.setOrder(order);
        fill1.setExtId("boohbooh");
        fill1.setSequenceNumber(1);

        Date currentTime = new Date();

        Mockito.when(this.orderBook.getOpenOrderByIntId("Blah")).thenReturn(order);
        Mockito.when(this.orderBook.getOpenOrderByIntId("a1.0")).thenReturn(algoOrder);
        Mockito.when(this.orderBook.getStatusByIntId("a1.0")).thenReturn(new ExecutionStatusVO("a1.0", Status.PARTIALLY_EXECUTED, 5L, 20L, null));
        Mockito.when(this.engine.getCurrentTime()).thenReturn(currentTime);

        this.impl.handleFill(fill1);

        Mockito.verify(this.orderBook, Mockito.times(1)).updateExecutionStatus("a1.0", null, Status.PARTIALLY_EXECUTED, 12L, 13L);
        ArgumentCaptor<Object> argumentCaptor1 = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.eventDispatcher, Mockito.times(2)).sendEvent(Mockito.eq("TestStrategy"), argumentCaptor1.capture());

        List<Object> events = argumentCaptor1.getAllValues();
        Assert.assertNotNull(events);
        Assert.assertEquals(2, events.size());
        Object event1 = events.get(0);
        Assert.assertTrue(event1 instanceof FillVO);
        FillVO fill2 = (FillVO) event1;
        Assert.assertEquals("boohbooh", fill2.getExtId());
        Assert.assertEquals(Side.BUY, fill2.getSide());
        Assert.assertEquals(7L, fill2.getQuantity());
        Object event2 = events.get(1);
        Assert.assertTrue(event2 instanceof OrderStatusVO);
        OrderStatusVO orderStatus2 = (OrderStatusVO) event2;
        Assert.assertEquals("a1.0", orderStatus2.getIntId());
        Assert.assertEquals(Status.PARTIALLY_EXECUTED, orderStatus2.getStatus());
        Assert.assertEquals(12L, orderStatus2.getFilledQuantity());
        Assert.assertEquals(13L, orderStatus2.getRemainingQuantity());

        ArgumentCaptor<Object> argumentCaptor2 = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.engine, Mockito.times(1)).sendEvent(argumentCaptor2.capture());
        Object event3 = argumentCaptor2.getValue();
        Assert.assertTrue(event3 instanceof OrderStatus);
        OrderStatus orderStatus3 = (OrderStatus) event3;
        Assert.assertEquals("a1.0", orderStatus3.getIntId());
        Assert.assertEquals(Status.PARTIALLY_EXECUTED, orderStatus3.getStatus());
        Assert.assertEquals(12L, orderStatus3.getFilledQuantity());
        Assert.assertEquals(13L, orderStatus3.getRemainingQuantity());

    }

    @Test
    public void testAlgoFillPropagation() throws Exception {

        AlgoOrder algoOrder = new SlicingOrder();
        algoOrder.setIntId("a1.0");

        Order order = MarketOrder.Factory.newInstance();
        order.setIntId("Blah");
        order.setQuantity(25L);
        order.setStrategy(this.strategy);
        order.setSecurity(this.stock);
        order.setParentOrder(algoOrder);

        Fill fill1 = new Fill();
        fill1.setExtDateTime(new Date());
        fill1.setDateTime(fill1.getExtDateTime());
        fill1.setQuantity(7L);
        fill1.setSide(Side.BUY);
        fill1.setOrder(order);
        fill1.setExtId("boohbooh");
        fill1.setSequenceNumber(1);

        Date currentTime = new Date();

        Mockito.when(this.orderBook.getOpenOrderByIntId("Blah")).thenReturn(order);
        Mockito.when(this.orderBook.getOpenOrderByIntId("a1.0")).thenReturn(algoOrder);
        Mockito.when(this.orderBook.getStatusByIntId("a1.0")).thenReturn(new ExecutionStatusVO("a1.0", Status.PARTIALLY_EXECUTED, 18L, 7L, null));
        Mockito.when(this.engine.getCurrentTime()).thenReturn(currentTime);

        this.impl.handleFill(fill1);

        Mockito.verify(this.orderBook, Mockito.times(1)).updateExecutionStatus("a1.0", null, Status.EXECUTED, 25L, 0L);
        ArgumentCaptor<Object> argumentCaptor1 = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.eventDispatcher, Mockito.times(2)).sendEvent(Mockito.eq("TestStrategy"), argumentCaptor1.capture());

        List<Object> events = argumentCaptor1.getAllValues();
        Assert.assertNotNull(events);
        Assert.assertEquals(2, events.size());
        Object event1 = events.get(0);
        Assert.assertTrue(event1 instanceof FillVO);
        FillVO fill2 = (FillVO) event1;
        Assert.assertEquals("boohbooh", fill2.getExtId());
        Assert.assertEquals(Side.BUY, fill2.getSide());
        Assert.assertEquals(7L, fill2.getQuantity());
        Object event2 = events.get(1);
        Assert.assertTrue(event2 instanceof OrderStatusVO);
        OrderStatusVO orderStatus2 = (OrderStatusVO) event2;
        Assert.assertEquals("a1.0", orderStatus2.getIntId());
        Assert.assertEquals(Status.EXECUTED, orderStatus2.getStatus());
        Assert.assertEquals(25L, orderStatus2.getFilledQuantity());
        Assert.assertEquals(0L, orderStatus2.getRemainingQuantity());

        ArgumentCaptor<Object> argumentCaptor2 = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.engine, Mockito.times(1)).sendEvent(argumentCaptor2.capture());
        Object event3 = argumentCaptor2.getValue();
        Assert.assertTrue(event3 instanceof OrderStatus);
        OrderStatus orderStatus3 = (OrderStatus) event3;
        Assert.assertEquals("a1.0", orderStatus3.getIntId());
        Assert.assertEquals(Status.EXECUTED, orderStatus3.getStatus());
        Assert.assertEquals(25L, orderStatus3.getFilledQuantity());
        Assert.assertEquals(0L, orderStatus3.getRemainingQuantity());

    }

}
