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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.CommonConfigBuilder;
import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.Stock;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
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

}
