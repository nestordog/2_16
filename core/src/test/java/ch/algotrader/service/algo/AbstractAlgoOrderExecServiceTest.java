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

package ch.algotrader.service.algo;

import java.util.Collections;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.Stock;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.OrderStatusVO;
import ch.algotrader.entity.trade.algo.AlgoOrder;
import ch.algotrader.entity.trade.algo.AlgoOrderStateVO;
import ch.algotrader.entity.trade.algo.SlicingOrder;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.service.OrderExecutionService;
import ch.algotrader.service.SimpleOrderService;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class AbstractAlgoOrderExecServiceTest {

    static class SimpleOrderStateVO extends AlgoOrderStateVO {

        private static final long serialVersionUID = 6747508881143582418L;

    }

    @Mock
    private OrderExecutionService orderExecutionService;
    @Mock
    private SimpleOrderService simpleOrderService;

    private Strategy strategy;
    private SecurityFamily family;
    private Exchange exchange;
    private Stock stock;

    private AbstractAlgoOrderExecService<AlgoOrder, SimpleOrderStateVO> impl;

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

        this.impl = new AbstractAlgoOrderExecService<AlgoOrder, SimpleOrderStateVO>(this.orderExecutionService, this.simpleOrderService) {

            @Override
            public Class<? extends AlgoOrder> getAlgoOrderType() {
                return SlicingOrder.class;
            }

            @Override
            protected SimpleOrderStateVO createAlgoOrderState(AlgoOrder algoOrder) {
                return new SimpleOrderStateVO();
            }

            @Override
            public void handleSendOrder(AlgoOrder algoOrder, SimpleOrderStateVO algoOrderState) {
            }

            @Override
            protected void handleModifyOrder(AlgoOrder order, SimpleOrderStateVO algoOrderState) {
            }
        };
    }

    @Test
    public void testAlgoOrderCancel() throws Exception {

        AlgoOrder algoOrder = new SlicingOrder();
        algoOrder.setIntId("a1.0");

        MarketOrder order = MarketOrder.Factory.newInstance();
        order.setIntId("Blah");
        order.setQuantity(25L);
        order.setStrategy(this.strategy);
        order.setSecurity(this.stock);
        order.setParentOrder(algoOrder);

        Mockito.when(this.orderExecutionService.getOpenOrderByIntId("Blah")).thenReturn(order);
        Mockito.when(this.orderExecutionService.getOpenOrderByIntId("a1.0")).thenReturn(algoOrder);
        Mockito.when(this.orderExecutionService.getStatusByIntId("a1.0")).thenReturn(new OrderStatusVO(0L, null, Status.OPEN, 0L, 25L, 0L, "a1.0", 0L, 0L));
        Mockito.when(this.orderExecutionService.getOpenOrdersByParentIntId("a1.0")).thenReturn(Collections.singletonList(order));

        this.impl.validateOrder(algoOrder);
        this.impl.cancelOrder(algoOrder);

        ArgumentCaptor<OrderStatus> argumentCaptor1 = ArgumentCaptor.forClass(OrderStatus.class);
        Mockito.verify(this.orderExecutionService, Mockito.times(1)).handleOrderStatus(argumentCaptor1.capture());

        OrderStatus orderStatus2 = argumentCaptor1.getValue();
        Assert.assertEquals("a1.0", orderStatus2.getIntId());
        Assert.assertEquals(Status.CANCELED, orderStatus2.getStatus());
        Assert.assertEquals(0L, orderStatus2.getFilledQuantity());
        Assert.assertEquals(25L, orderStatus2.getRemainingQuantity());

        Mockito.verify(this.simpleOrderService, Mockito.times(1)).cancelOrder(order);
    }

    @Test
    public void testAlgoOrderSubmittedPropagation() throws Exception {

        AlgoOrder algoOrder = new SlicingOrder();
        algoOrder.setIntId("a1.0");

        MarketOrder order = MarketOrder.Factory.newInstance();
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

        Mockito.when(this.orderExecutionService.getOpenOrderByIntId("Blah")).thenReturn(order);
        Mockito.when(this.orderExecutionService.getOpenOrderByIntId("a1.0")).thenReturn(algoOrder);
        Mockito.when(this.orderExecutionService.getStatusByIntId("a1.0")).thenReturn(new OrderStatusVO(0L, null, Status.OPEN, 0L, 24L, 0L, "a1.0", 0L, 0L));

        this.impl.validateOrder(algoOrder);
        this.impl.handleChildOrderStatus(algoOrder, orderStatus1);

        ArgumentCaptor<OrderStatus> argumentCaptor1 = ArgumentCaptor.forClass(OrderStatus.class);
        Mockito.verify(this.orderExecutionService, Mockito.times(1)).handleOrderStatus(argumentCaptor1.capture());

        OrderStatus orderStatus2 = argumentCaptor1.getValue();
        Assert.assertEquals("a1.0", orderStatus2.getIntId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus2.getStatus());
        Assert.assertEquals(0L, orderStatus2.getFilledQuantity());
        Assert.assertEquals(24L, orderStatus2.getRemainingQuantity());
    }

    @Test
    public void testAlgoPartialFillPropagation() throws Exception {

        AlgoOrder algoOrder = new SlicingOrder();
        algoOrder.setIntId("a1.0");

        MarketOrder order = MarketOrder.Factory.newInstance();
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

        Mockito.when(this.orderExecutionService.getOpenOrderByIntId("Blah")).thenReturn(order);
        Mockito.when(this.orderExecutionService.getOpenOrderByIntId("a1.0")).thenReturn(algoOrder);
        Mockito.when(this.orderExecutionService.getStatusByIntId("a1.0")).thenReturn(new OrderStatusVO(0L, null, Status.PARTIALLY_EXECUTED, 5L, 20L, 0L, "a1.0", 0L, 0L));

        this.impl.validateOrder(algoOrder);
        this.impl.handleChildFill(algoOrder, fill1);

        ArgumentCaptor<OrderStatus> argumentCaptor1 = ArgumentCaptor.forClass(OrderStatus.class);
        Mockito.verify(this.orderExecutionService, Mockito.times(1)).handleOrderStatus(argumentCaptor1.capture());

        OrderStatus orderStatus2 = argumentCaptor1.getValue();
        Assert.assertEquals("a1.0", orderStatus2.getIntId());
        Assert.assertEquals(Status.PARTIALLY_EXECUTED, orderStatus2.getStatus());
        Assert.assertEquals(12L, orderStatus2.getFilledQuantity());
        Assert.assertEquals(13L, orderStatus2.getRemainingQuantity());
    }

    @Test
    public void testAlgoFillPropagation() throws Exception {

        AlgoOrder algoOrder = new SlicingOrder();
        algoOrder.setIntId("a1.0");

        MarketOrder order = MarketOrder.Factory.newInstance();
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

        Mockito.when(this.orderExecutionService.getOpenOrderByIntId("Blah")).thenReturn(order);
        Mockito.when(this.orderExecutionService.getOpenOrderByIntId("a1.0")).thenReturn(algoOrder);
        Mockito.when(this.orderExecutionService.getStatusByIntId("a1.0")).thenReturn(new OrderStatusVO(0L, null, Status.PARTIALLY_EXECUTED, 18L, 7L, 0L, "a1.0", 0L, 0L));

        this.impl.validateOrder(algoOrder);
        this.impl.handleChildFill(algoOrder, fill1);

        ArgumentCaptor<OrderStatus> argumentCaptor1 = ArgumentCaptor.forClass(OrderStatus.class);
        Mockito.verify(this.orderExecutionService, Mockito.times(1)).handleOrderStatus(argumentCaptor1.capture());

        OrderStatus orderStatus2 = argumentCaptor1.getValue();
        Assert.assertEquals("a1.0", orderStatus2.getIntId());
        Assert.assertEquals(Status.EXECUTED, orderStatus2.getStatus());
        Assert.assertEquals(25L, orderStatus2.getFilledQuantity());
        Assert.assertEquals(0L, orderStatus2.getRemainingQuantity());
    }

}
