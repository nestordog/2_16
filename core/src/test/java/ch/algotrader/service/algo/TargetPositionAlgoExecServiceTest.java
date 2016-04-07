package ch.algotrader.service.algo;

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import ch.algotrader.entity.security.Stock;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.OrderStatusVO;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.entity.trade.algo.TargetPositionOrder;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.OrderExecutionService;
import ch.algotrader.service.SimpleOrderService;

@RunWith(MockitoJUnitRunner.class)
public class TargetPositionAlgoExecServiceTest {

    @Mock
    private OrderExecutionService orderExecutionService;
    @Mock
    private LookupService lookupService;
    @Mock
    private SimpleOrderService simpleOrderService;
    @Captor
    private ArgumentCaptor<SimpleOrder> orderCaptor;

    private Strategy strategy;
    private Stock security;
    private TargetPositionOrderService impl;

    @Before
    public void setup() {

        strategy = Strategy.Factory.newInstance();
        strategy.setName("blah");
        strategy.setId(3456L);
        Mockito.when(lookupService.getStrategyByName("blah")).thenReturn(strategy);

        security = Stock.Factory.newInstance();
        security.setId(4567L);
        security.setSymbol("stuff");

        impl = new TargetPositionOrderService(orderExecutionService, lookupService, simpleOrderService);
    }

    @Test
    public void testPositionChangeNoDelta() throws Exception {

        // (1) actual position at +20
        // (2) target reset to +20
        // (3) no actions

        TargetPositionOrder algoOrder = new TargetPositionOrder();
        algoOrder.setSecurity(security);
        algoOrder.setStrategy(strategy);
        algoOrder.setTarget(20L);

        TargetPositionOrderStateVO algoOrderState = new TargetPositionOrderStateVO();
        algoOrderState.setActualQty(20L);
        algoOrderState.setTargetQty(20L);

        impl.adjustPosition(algoOrder, algoOrderState);

        Mockito.verifyZeroInteractions(simpleOrderService);
        Mockito.verifyZeroInteractions(orderExecutionService);
    }

    @Test
    public void testLongPositionIncrease() throws Exception {

        // (1) actual position at +20
        // (2) target increased (+20 -> +40)
        // (3) order +20
        // (4) target reset to +40
        // (5) no actions

        TargetPositionOrder algoOrder = new TargetPositionOrder();
        algoOrder.setSecurity(security);
        algoOrder.setStrategy(strategy);
        algoOrder.setTarget(40L);

        TargetPositionOrderStateVO algoOrderState = new TargetPositionOrderStateVO();
        algoOrderState.setActualQty(20L);
        algoOrderState.setTargetQty(40L);

        impl.adjustPosition(algoOrder, algoOrderState);

        Mockito.verify(simpleOrderService).sendOrder(orderCaptor.capture());

        final Order order = orderCaptor.getValue();
        Assert.assertEquals(20L, order.getQuantity());
        Assert.assertEquals(Side.BUY, order.getSide());
        Assert.assertEquals(null, order.getIntId());

        Assert.assertEquals(Status.OPEN, algoOrderState.getOrderStatus());

        impl.adjustPosition(algoOrder, algoOrderState);

        Mockito.verifyNoMoreInteractions(simpleOrderService);
    }

    @Test
    public void testLongPositionDecrease() throws Exception {

        // (1) actual position at +40
        // (2) target decreased (+40 -> +30)
        // (3) order -10
        // (4) target reset to +30
        // (5) no actions

        TargetPositionOrder algoOrder = new TargetPositionOrder();
        algoOrder.setSecurity(security);
        algoOrder.setStrategy(strategy);
        algoOrder.setTarget(30L);

        TargetPositionOrderStateVO algoOrderState = new TargetPositionOrderStateVO();
        algoOrderState.setActualQty(40L);
        algoOrderState.setTargetQty(30L);

        impl.adjustPosition(algoOrder, algoOrderState);

        Mockito.verify(simpleOrderService).sendOrder(orderCaptor.capture());

        final Order order = orderCaptor.getValue();
        Assert.assertEquals(10L, order.getQuantity());
        Assert.assertEquals(Side.SELL, order.getSide());
        Assert.assertEquals(null, order.getIntId());

        Assert.assertEquals(Status.OPEN, algoOrderState.getOrderStatus());

        impl.adjustPosition(algoOrder, algoOrderState);

        Mockito.verifyNoMoreInteractions(simpleOrderService);
    }

    @Test
    public void testShortPositionIncrease() throws Exception {

        // (1) actual position at -20
        // (2) target increased (-20 -> -40)
        // (3) order -20
        // (4) target reset to -20
        // (5) no actions

        TargetPositionOrder algoOrder = new TargetPositionOrder();
        algoOrder.setSecurity(security);
        algoOrder.setStrategy(strategy);
        algoOrder.setTarget(-40L);

        TargetPositionOrderStateVO algoOrderState = new TargetPositionOrderStateVO();
        algoOrderState.setActualQty(-20L);
        algoOrderState.setTargetQty(-40L);

        impl.adjustPosition(algoOrder, algoOrderState);

        Mockito.verify(simpleOrderService).sendOrder(orderCaptor.capture());

        final Order order = orderCaptor.getValue();
        Assert.assertEquals(20L, order.getQuantity());
        Assert.assertEquals(Side.SELL, order.getSide());
        Assert.assertEquals(null, order.getIntId());

        Assert.assertEquals(Status.OPEN, algoOrderState.getOrderStatus());

        impl.adjustPosition(algoOrder, algoOrderState);

        Mockito.verifyNoMoreInteractions(simpleOrderService);
    }

    @Test
    public void testShortPositionDecrease() throws Exception {

        // (1) actual position at -30
        // (2) target decreased (-30 -> -10)
        // (3) order +20
        // (4) target reset to -10
        // (5) no actions

        TargetPositionOrder algoOrder = new TargetPositionOrder();
        algoOrder.setSecurity(security);
        algoOrder.setStrategy(strategy);
        algoOrder.setTarget(-10L);

        TargetPositionOrderStateVO algoOrderState = new TargetPositionOrderStateVO();
        algoOrderState.setActualQty(-30L);
        algoOrderState.setTargetQty(-10L);

        impl.adjustPosition(algoOrder, algoOrderState);

        Mockito.verify(simpleOrderService).sendOrder(orderCaptor.capture());

        final Order order = orderCaptor.getValue();
        Assert.assertEquals(20L, order.getQuantity());
        Assert.assertEquals(Side.BUY, order.getSide());
        Assert.assertEquals(null, order.getIntId());

        Assert.assertEquals(Status.OPEN, algoOrderState.getOrderStatus());

        impl.adjustPosition(algoOrder, algoOrderState);

        Mockito.verifyNoMoreInteractions(simpleOrderService);
    }

    @Test
    public void testPositionMutationOrderAck() throws Exception {

        // (1) actual position at +20
        // (2) target increased (+20 -> +40)
        // (3) order +20
        // (4) order acknowledged
        // (5) target reset to +40
        // (6) no actions

        TargetPositionOrder algoOrder = new TargetPositionOrder();
        algoOrder.setSecurity(security);
        algoOrder.setStrategy(strategy);
        algoOrder.setTarget(40L);

        TargetPositionOrderStateVO algoOrderState = new TargetPositionOrderStateVO();
        algoOrderState.setActualQty(20L);
        algoOrderState.setTargetQty(40L);

        impl.adjustPosition(algoOrder, algoOrderState);

        Mockito.verify(simpleOrderService).sendOrder(orderCaptor.capture());

        final Order order = orderCaptor.getValue();
        Assert.assertEquals(20L, order.getQuantity());
        Assert.assertEquals(Side.BUY, order.getSide());
        Assert.assertEquals(null, order.getIntId());

        Assert.assertEquals(Status.OPEN, algoOrderState.getOrderStatus());
        Assert.assertEquals(null, algoOrderState.getIntId());

        Mockito.reset(simpleOrderService);

        final MarketOrder workingOrder = MarketOrder.Factory.newInstance();
        workingOrder.setQuantity(20L);
        workingOrder.setSide(Side.BUY);
        workingOrder.setIntId("int-id-1");
        workingOrder.setStrategy(strategy);
        workingOrder.setSecurity(security);

        Mockito.when(orderExecutionService.getOrderByIntId("int-id-1")).thenReturn(workingOrder);
        Mockito.when(orderExecutionService.getStatusByIntId("int-id-1")).thenReturn(
                new OrderStatusVO(0L, new Date(), Status.SUBMITTED, 0L, 20L, 0L, "int-id-1", 0L, 0L));

        OrderStatus ack = OrderStatus.Factory.newInstance();
        ack.setStatus(Status.SUBMITTED);
        ack.setIntId("int-id-1");
        ack.setOrder(workingOrder);
        impl.handleChildOrderStatus(algoOrder, algoOrderState, ack);

        Assert.assertEquals(Status.SUBMITTED, algoOrderState.getOrderStatus());
        Assert.assertEquals("int-id-1", algoOrderState.getIntId());

        impl.adjustPosition(algoOrder, algoOrderState);

        Mockito.verify(simpleOrderService, Mockito.never()).sendOrder(Mockito.any());
        Mockito.verify(simpleOrderService, Mockito.never()).modifyOrder(Mockito.any());
    }

    @Test
    public void testPositionMutationWorkingOrderModified() throws Exception {

        // (1) actual position at +20
        // (2) target increased (+20 -> +40)
        // (3) order +20
        // (4) order acknowledged
        // (5) order filled +10
        // (6) actual position at +30
        // (7) target reset to +40
        // (8) no actions
        // (9) target reset to +60
        // (10) working order modified to +30

        TargetPositionOrder algoOrder = new TargetPositionOrder();
        algoOrder.setSecurity(security);
        algoOrder.setStrategy(strategy);
        algoOrder.setTarget(40L);

        TargetPositionOrderStateVO algoOrderState = new TargetPositionOrderStateVO();
        algoOrderState.setActualQty(20L);
        algoOrderState.setTargetQty(40L);

        impl.adjustPosition(algoOrder, algoOrderState);

        Assert.assertEquals(Status.OPEN, algoOrderState.getOrderStatus());

        Mockito.reset(simpleOrderService);

        final MarketOrder workingOrder = MarketOrder.Factory.newInstance();
        workingOrder.setQuantity(20L);
        workingOrder.setSide(Side.BUY);
        workingOrder.setIntId("int-id-1");
        workingOrder.setStrategy(strategy);
        workingOrder.setSecurity(security);

        Mockito.when(orderExecutionService.getOrderByIntId("int-id-1")).thenReturn(workingOrder);
        Mockito.when(orderExecutionService.getStatusByIntId("int-id-1")).thenReturn(
                new OrderStatusVO(0L, new Date(), Status.PARTIALLY_EXECUTED, 10L, 10L, 10L, "int-id-1", 0L, 0L));

        OrderStatus ack = OrderStatus.Factory.newInstance();
        ack.setStatus(Status.SUBMITTED);
        ack.setIntId("int-id-1");
        ack.setOrder(workingOrder);
        impl.handleChildOrderStatus(algoOrder, algoOrderState, ack);

        OrderStatus partialExec = OrderStatus.Factory.newInstance();
        partialExec.setStatus(Status.PARTIALLY_EXECUTED);
        partialExec.setIntId("int-id-1");
        partialExec.setOrder(workingOrder);
        partialExec.setFilledQuantity(10L);
        partialExec.setRemainingQuantity(10L);
        impl.handleChildOrderStatus(algoOrder, algoOrderState, partialExec);

        Assert.assertEquals(Status.PARTIALLY_EXECUTED, algoOrderState.getOrderStatus());

        Fill fill = new Fill();
        fill.setSide(Side.BUY);
        fill.setQuantity(10L);
        fill.setOrder(workingOrder);

        impl.handleChildFill(algoOrder, algoOrderState, fill);

        Assert.assertEquals(30L, algoOrderState.getActualQty());

        impl.adjustPosition(algoOrder, algoOrderState);

        Mockito.verify(simpleOrderService, Mockito.never()).sendOrder(Mockito.any());
        Mockito.verify(simpleOrderService, Mockito.never()).modifyOrder(Mockito.any());

        Mockito.reset(simpleOrderService);

        algoOrder.setTarget(60L);
        algoOrderState.setTargetQty(60L);

        impl.adjustPosition(algoOrder, algoOrderState);

        Mockito.verify(simpleOrderService, Mockito.never()).sendOrder(Mockito.any());
        Mockito.verify(simpleOrderService).modifyOrder(orderCaptor.capture());

        final SimpleOrder order = orderCaptor.getValue();
        Assert.assertEquals(30L, order.getQuantity());
        Assert.assertEquals(Side.BUY, order.getSide());
        Assert.assertEquals("int-id-1", order.getIntId());
    }

    @Test
    public void testPositionMutationWorkingOrderCancelledNewOrderSent() throws Exception {

        // (1) actual position at +20
        // (2) target increased (+20 -> +40)
        // (3) order2 +20
        // (4) order2 acknowledged
        // (5) order2 filled +10
        // (6) actual position at +30
        // (7) target reset to +40
        // (8) no actions
        // (9) target reset to -20
        // (10) working order2 at +10 canceled
        // (11) order2 -50

        TargetPositionOrder algoOrder = new TargetPositionOrder();
        algoOrder.setSecurity(security);
        algoOrder.setStrategy(strategy);
        algoOrder.setTarget(40L);

        TargetPositionOrderStateVO algoOrderState = new TargetPositionOrderStateVO();
        algoOrderState.setActualQty(20L);
        algoOrderState.setTargetQty(40L);

        impl.adjustPosition(algoOrder, algoOrderState);

        Assert.assertEquals(Status.OPEN, algoOrderState.getOrderStatus());

        Mockito.reset(simpleOrderService);

        final MarketOrder workingOrder = MarketOrder.Factory.newInstance();
        workingOrder.setQuantity(20L);
        workingOrder.setSide(Side.BUY);
        workingOrder.setIntId("int-id-1");
        workingOrder.setStrategy(strategy);
        workingOrder.setSecurity(security);

        Mockito.when(orderExecutionService.getOrderByIntId("int-id-1")).thenReturn(workingOrder);
        Mockito.when(orderExecutionService.getStatusByIntId("int-id-1")).thenReturn(
                new OrderStatusVO(0L, new Date(), Status.PARTIALLY_EXECUTED, 10L, 10L, 10L, "int-id-1", 0L, 0L));

        OrderStatus ack = OrderStatus.Factory.newInstance();
        ack.setStatus(Status.SUBMITTED);
        ack.setIntId("int-id-1");
        ack.setOrder(workingOrder);
        impl.handleChildOrderStatus(algoOrder, algoOrderState, ack);

        OrderStatus partialExec = OrderStatus.Factory.newInstance();
        partialExec.setStatus(Status.PARTIALLY_EXECUTED);
        partialExec.setIntId("int-id-1");
        partialExec.setOrder(workingOrder);
        partialExec.setFilledQuantity(10L);
        partialExec.setRemainingQuantity(10L);
        impl.handleChildOrderStatus(algoOrder, algoOrderState, partialExec);

        Assert.assertEquals(Status.PARTIALLY_EXECUTED, algoOrderState.getOrderStatus());

        Fill fill = new Fill();
        fill.setSide(Side.BUY);
        fill.setQuantity(10L);
        fill.setOrder(workingOrder);

        impl.handleChildFill(algoOrder, algoOrderState, fill);

        Assert.assertEquals(30L, algoOrderState.getActualQty());

        impl.adjustPosition(algoOrder, algoOrderState);

        Mockito.verify(simpleOrderService, Mockito.never()).sendOrder(Mockito.any());
        Mockito.verify(simpleOrderService, Mockito.never()).modifyOrder(Mockito.any());

        Mockito.reset(simpleOrderService);

        algoOrder.setTarget(-20L);
        algoOrderState.setTargetQty(-20L);

        impl.adjustPosition(algoOrder, algoOrderState);

        Mockito.verify(simpleOrderService, Mockito.never()).modifyOrder(Mockito.any());
        Mockito.verify(simpleOrderService).cancelOrder(orderCaptor.capture());

        final SimpleOrder order1 = orderCaptor.getValue();
        Assert.assertEquals(20L, order1.getQuantity());
        Assert.assertEquals(Side.BUY, order1.getSide());
        Assert.assertEquals("int-id-1", order1.getIntId());

        Mockito.verify(simpleOrderService).sendOrder(orderCaptor.capture());

        final SimpleOrder order2 = orderCaptor.getValue();
        Assert.assertEquals(50L, order2.getQuantity());
        Assert.assertEquals(Side.SELL, order2.getSide());
        Assert.assertEquals(null, order2.getIntId());
    }

    @Test
    public void testPositionMutationWorkingOrderCancelledNoNewOrder() throws Exception {

        // (1) actual position at +20
        // (2) target increased (+20 -> +40)
        // (3) order +20
        // (4) order acknowledged
        // (5) order filled +10
        // (6) actual position at +30
        // (7) target reset to +40
        // (8) no actions
        // (9) target set to +30
        // (10) working order at +10 canceled
        // (11) no order sent or modified

        TargetPositionOrder algoOrder = new TargetPositionOrder();
        algoOrder.setSecurity(security);
        algoOrder.setStrategy(strategy);
        algoOrder.setTarget(40L);

        TargetPositionOrderStateVO algoOrderState = new TargetPositionOrderStateVO();
        algoOrderState.setActualQty(20L);
        algoOrderState.setTargetQty(40L);

        impl.adjustPosition(algoOrder, algoOrderState);

        Assert.assertEquals(Status.OPEN, algoOrderState.getOrderStatus());

        Mockito.reset(simpleOrderService);

        final MarketOrder workingOrder = MarketOrder.Factory.newInstance();
        workingOrder.setQuantity(20L);
        workingOrder.setSide(Side.BUY);
        workingOrder.setIntId("int-id-1");
        workingOrder.setStrategy(strategy);
        workingOrder.setSecurity(security);

        Mockito.when(orderExecutionService.getOrderByIntId("int-id-1")).thenReturn(workingOrder);
        Mockito.when(orderExecutionService.getStatusByIntId("int-id-1")).thenReturn(
                new OrderStatusVO(0L, new Date(), Status.PARTIALLY_EXECUTED, 10L, 10L, 10L, "int-id-1", 0L, 0L));

        OrderStatus ack = OrderStatus.Factory.newInstance();
        ack.setStatus(Status.SUBMITTED);
        ack.setIntId("int-id-1");
        ack.setOrder(workingOrder);
        impl.handleChildOrderStatus(algoOrder, algoOrderState, ack);

        OrderStatus partialExec = OrderStatus.Factory.newInstance();
        partialExec.setStatus(Status.PARTIALLY_EXECUTED);
        partialExec.setIntId("int-id-1");
        partialExec.setOrder(workingOrder);
        partialExec.setFilledQuantity(10L);
        partialExec.setRemainingQuantity(10L);
        impl.handleChildOrderStatus(algoOrder, algoOrderState, partialExec);

        Assert.assertEquals(Status.PARTIALLY_EXECUTED, algoOrderState.getOrderStatus());

        Fill fill = new Fill();
        fill.setSide(Side.BUY);
        fill.setQuantity(10L);
        fill.setOrder(workingOrder);

        impl.handleChildFill(algoOrder, algoOrderState, fill);

        Assert.assertEquals(30L, algoOrderState.getActualQty());

        impl.adjustPosition(algoOrder, algoOrderState);

        Mockito.verify(simpleOrderService, Mockito.never()).sendOrder(Mockito.any());
        Mockito.verify(simpleOrderService, Mockito.never()).modifyOrder(Mockito.any());

        Mockito.reset(simpleOrderService);

        algoOrderState.setTargetQty(30L);

        impl.adjustPosition(algoOrder, algoOrderState);

        Mockito.verify(simpleOrderService, Mockito.never()).modifyOrder(Mockito.any());
        Mockito.verify(simpleOrderService).cancelOrder(orderCaptor.capture());

        final SimpleOrder order1 = orderCaptor.getValue();
        Assert.assertEquals(20L, order1.getQuantity());
        Assert.assertEquals(Side.BUY, order1.getSide());
        Assert.assertEquals("int-id-1", order1.getIntId());

        Mockito.verify(simpleOrderService, Mockito.never()).sendOrder((Mockito.any()));
    }

    @Test
    public void testLongPositionCloseWithWorkingOrder() throws Exception {

        TargetPositionOrder algoOrder = new TargetPositionOrder();
        algoOrder.setSecurity(security);
        algoOrder.setStrategy(strategy);
        algoOrder.setTarget(20L);

        TargetPositionOrderStateVO algoOrderState = new TargetPositionOrderStateVO();
        algoOrderState.setActualQty(20L);
        algoOrderState.setTargetQty(20L);

        impl.adjustPosition(algoOrder, algoOrderState);

        final MarketOrder workingOrder = MarketOrder.Factory.newInstance();
        workingOrder.setQuantity(20L);
        workingOrder.setSide(Side.SELL);
        workingOrder.setIntId("int-id-1");
        workingOrder.setStrategy(strategy);
        workingOrder.setSecurity(security);

        Mockito.when(orderExecutionService.getOrderByIntId("int-id-1")).thenReturn(workingOrder);

        OrderStatus ack = OrderStatus.Factory.newInstance();
        ack.setStatus(Status.SUBMITTED);
        ack.setIntId("int-id-1");
        ack.setOrder(workingOrder);
        impl.handleChildOrderStatus(algoOrder, algoOrderState, ack);

        Mockito.when(orderExecutionService.getStatusByIntId("int-id-1")).thenReturn(
                new OrderStatusVO(0L, new Date(), Status.SUBMITTED, 0L, 20L, 0L, "int-id-1", 0L, 0L));

        algoOrder.setTarget(0L);
        algoOrderState.setTargetQty(0L);

        Mockito.verify(simpleOrderService, Mockito.never()).sendOrder((Mockito.any()));
        Mockito.verify(simpleOrderService, Mockito.never()).modifyOrder(Mockito.any());
        Mockito.verify(simpleOrderService, Mockito.never()).cancelOrder(Mockito.any());
    }

    @Test
    public void testOrderExecutedExtraFill() throws Exception {

        TargetPositionOrder algoOrder = new TargetPositionOrder();
        algoOrder.setSecurity(security);
        algoOrder.setStrategy(strategy);
        algoOrder.setTarget(20L);

        TargetPositionOrderStateVO algoOrderState = new TargetPositionOrderStateVO();
        algoOrderState.setActualQty(0L);
        algoOrderState.setTargetQty(20L);

        impl.adjustPosition(algoOrder, algoOrderState);

        algoOrderState.setOrderStatus(Status.SUBMITTED);
        algoOrderState.setIntId("int-id-1");

        final MarketOrder workingOrder = MarketOrder.Factory.newInstance();
        workingOrder.setQuantity(20L);
        workingOrder.setSide(Side.SELL);
        workingOrder.setIntId("int-id-1");
        workingOrder.setStrategy(strategy);
        workingOrder.setSecurity(security);

        Mockito.when(orderExecutionService.getOrderByIntId("int-id-1")).thenReturn(workingOrder);

        OrderStatus fullExec = OrderStatus.Factory.newInstance();
        fullExec.setStatus(Status.EXECUTED);
        fullExec.setFilledQuantity(20L);
        fullExec.setRemainingQuantity(0L);
        fullExec.setIntId("int-id-1");
        fullExec.setOrder(workingOrder);
        impl.handleChildOrderStatus(algoOrder, algoOrderState, fullExec);

        Assert.assertEquals(Status.EXECUTED, algoOrderState.getOrderStatus());

        Mockito.reset(simpleOrderService);

        Fill fill = new Fill();
        fill.setSide(Side.BUY);
        fill.setQuantity(20L);
        fill.setOrder(workingOrder);

        impl.handleChildFill(algoOrder, algoOrderState, fill);

        Assert.assertEquals(20L, algoOrderState.getActualQty());

        Mockito.verify(simpleOrderService, Mockito.never()).sendOrder((Mockito.any()));
        Mockito.verify(simpleOrderService, Mockito.never()).modifyOrder(Mockito.any());
        Mockito.verify(simpleOrderService, Mockito.never()).cancelOrder(Mockito.any());

        Mockito.reset(simpleOrderService);

        Fill extraFill = new Fill();
        extraFill.setSide(Side.SELL);
        extraFill.setQuantity(5L);

        impl.handleChildFill(algoOrder, algoOrderState, extraFill);

        Assert.assertEquals(15L, algoOrderState.getActualQty());

        Mockito.verify(simpleOrderService).sendOrder(orderCaptor.capture());

        final Order order = orderCaptor.getValue();
        Assert.assertEquals(5L, order.getQuantity());
        Assert.assertEquals(Side.BUY, order.getSide());
        Assert.assertEquals(null, order.getIntId());

        Mockito.verify(simpleOrderService, Mockito.never()).modifyOrder(Mockito.any());
        Mockito.verify(simpleOrderService, Mockito.never()).cancelOrder(Mockito.any());
    }

}