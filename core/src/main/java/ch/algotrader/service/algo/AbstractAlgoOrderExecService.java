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

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.Validate;

import ch.algotrader.entity.Account;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.OrderStatusVO;
import ch.algotrader.entity.trade.OrderValidationException;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.entity.trade.algo.AlgoOrder;
import ch.algotrader.entity.trade.algo.AlgoOrderStateVO;
import ch.algotrader.enumeration.Status;
import ch.algotrader.service.OrderExecutionService;
import ch.algotrader.service.ServiceException;
import ch.algotrader.service.SimpleOrderService;

/**
 * Default algo order execution service.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public abstract class AbstractAlgoOrderExecService<T extends AlgoOrder, S extends AlgoOrderStateVO> implements AlgoOrderExecService<T> {

    private final OrderExecutionService orderExecutionService;
    private final SimpleOrderService simpleOrderService;
    private final ConcurrentMap<String, S> algoOrderStates;

    public AbstractAlgoOrderExecService(final OrderExecutionService orderExecutionService, final SimpleOrderService simpleOrderService) {

        Validate.notNull(orderExecutionService, "OrderExecutionService is null");
        Validate.notNull(simpleOrderService, "OrderExecutionService is null");

        this.orderExecutionService = orderExecutionService;
        this.simpleOrderService = simpleOrderService;
        this.algoOrderStates = new ConcurrentHashMap<>();
    }

    protected abstract S createAlgoOrderState(final T algoOrder);

    protected S getAlgoOrderState(final AlgoOrder algoOrder) {
        String intId = algoOrder.getIntId();
        if (intId == null) {
            throw new ServiceException("Order intId is null");
        }
        return getAlgoOrderState(intId);
    }

    protected S getAlgoOrderState(final String intId) {
        S algoOrderState = this.algoOrderStates.get(intId);
        if (algoOrderState == null) {
            throw new ServiceException("Unexpected algoOrder intId: " + intId);
        }
        return algoOrderState;
    }

    protected void removeAlgoOrderState(final AlgoOrder algoOrder) {
        this.algoOrderStates.remove(algoOrder.getIntId());
    }

    @Override
    public final void validateOrder(final T algoOrder) throws OrderValidationException {

        Validate.notNull(algoOrder, "AlgoOrder is null");

        String intId = algoOrder.getIntId();
        if (intId == null) {
            throw new ServiceException("Order intId is null");
        }
        S algoOrderState = this.algoOrderStates.get(intId);
        if (algoOrderState == null) {
            algoOrderState = createAlgoOrderState(algoOrder);
        }
        handleValidateOrder(algoOrder, algoOrderState);
        this.algoOrderStates.putIfAbsent(intId, algoOrderState);
    }

    protected void handleValidateOrder(final T algoOrder, final S algoOrderState) throws OrderValidationException {
    }

    @Override
    public final void sendOrder(final T algoOrder) {

        Validate.notNull(algoOrder, "AlgoOrder is null");

        String intId = algoOrder.getIntId();
        if (intId == null) {
            throw new ServiceException("Order intId is null");
        }
        handleSendOrder(algoOrder, getAlgoOrderState(algoOrder));
    }

    protected abstract void handleSendOrder(final T algoOrder, final S algoOrderState);

    @Override
    public final void modifyOrder(final T algoOrder) {

        Validate.notNull(algoOrder, "AlgoOrder is null");

        handleModifyOrder(algoOrder, getAlgoOrderState(algoOrder));
    }

    protected abstract void handleModifyOrder(final T algoOrder, final S algoOrderState);

    @Override
    public final void cancelOrder(final T algoOrder) {

        Validate.notNull(algoOrder, "AlgoOrder is null");

        handleCancelOrder(algoOrder, getAlgoOrderState(algoOrder));

        String algoOrderId = algoOrder.getIntId();
        OrderStatusVO executionStatus = this.orderExecutionService.getStatusByIntId(algoOrderId);
        if (executionStatus != null) {
            OrderStatus algoOrderStatus = OrderStatus.Factory.newInstance();
            algoOrderStatus.setIntId(algoOrderId);
            algoOrderStatus.setStatus(Status.CANCELED);
            algoOrderStatus.setFilledQuantity(executionStatus.getFilledQuantity());
            algoOrderStatus.setRemainingQuantity(executionStatus.getRemainingQuantity());
            algoOrderStatus.setOrder(algoOrder);

            this.orderExecutionService.handleOrderStatus(algoOrderStatus);
        }

        this.algoOrderStates.remove(algoOrderId);
    }

    protected void handleCancelOrder(final T order, final S algoOrderState) {

        String algoIntId = order.getIntId();
        List<Order> openChildOrders = this.orderExecutionService.getOpenOrdersByParentIntId(algoIntId);
        for (Order childOrder: openChildOrders) {
            if (childOrder instanceof SimpleOrder) {
                this.simpleOrderService.cancelOrder((SimpleOrder) childOrder);
            }
        }
    }

    @Override
    public String getNextOrderId(final Account account) {
        throw new UnsupportedOperationException("getNextOrderId not supported");
    }

    @Override
    public final void onChildOrderStatus(final T algoOrder, final OrderStatus orderStatus) {

        String intId = algoOrder.getIntId();
        S algoOrderState = this.algoOrderStates.get(intId);
        if (algoOrderState == null) {
            return;
        }
        if (orderStatus.getStatus() == Status.SUBMITTED) {

            OrderStatusVO execStatus = this.orderExecutionService.getStatusByIntId(intId);
            if (execStatus != null && execStatus.getStatus() == Status.OPEN) {
                OrderStatus algoOrderStatus = OrderStatus.Factory.newInstance();
                algoOrderStatus.setStatus(Status.SUBMITTED);
                algoOrderStatus.setIntId(intId);
                algoOrderStatus.setExtDateTime(algoOrderStatus.getExtDateTime());
                algoOrderStatus.setFilledQuantity(0L);
                algoOrderStatus.setRemainingQuantity(execStatus.getRemainingQuantity());
                algoOrderStatus.setOrder(algoOrder);

                this.orderExecutionService.handleOrderStatus(algoOrderStatus);

                handleOrderStatus(algoOrder, algoOrderState, algoOrderStatus);
            }
        }

        handleChildOrderStatus(algoOrder, algoOrderState, orderStatus);
    }

    @Override
    public final void onChildFill(final T algoOrder, final Fill fill) {

        String intId = algoOrder.getIntId();
        S algoOrderState = this.algoOrderStates.get(intId);
        if (algoOrderState == null) {
            return;
        }
        OrderStatusVO execStatus = this.orderExecutionService.getStatusByIntId(intId);
        if (execStatus != null) {
            OrderStatus algoOrderStatus = OrderStatus.Factory.newInstance();
            algoOrderStatus.setStatus(execStatus.getRemainingQuantity() - fill.getQuantity() > 0 ? Status.PARTIALLY_EXECUTED : Status.EXECUTED);
            algoOrderStatus.setIntId(intId);
            algoOrderStatus.setDateTime(algoOrderStatus.getExtDateTime());
            algoOrderStatus.setFilledQuantity(execStatus.getFilledQuantity() + fill.getQuantity());
            algoOrderStatus.setRemainingQuantity(execStatus.getRemainingQuantity() - fill.getQuantity());
            algoOrderStatus.setOrder(algoOrder);

            this.orderExecutionService.handleOrderStatus(algoOrderStatus);

            handleOrderStatus(algoOrder, algoOrderState, algoOrderStatus);

            if (algoOrderStatus.getStatus() == Status.EXECUTED) {
                this.algoOrderStates.remove(intId);
            }
        }

        handleChildFill(algoOrder, algoOrderState, fill);
    }

    protected void handleOrderStatus(T algoOrder, final S algoOrderState, final OrderStatus orderStatus) {
    }

    protected void handleChildFill(final T algoOrder, final S algoOrderState, final Fill fill) {
    }

    protected void handleChildOrderStatus(final T algoOrder, final S algoOrderState, final OrderStatus orderStatus) {
    }

}
