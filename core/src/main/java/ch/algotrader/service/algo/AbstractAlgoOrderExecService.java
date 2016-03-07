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
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.Validate;

import ch.algotrader.entity.Account;
import ch.algotrader.entity.security.Security;
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

    protected final Optional<S> getAlgoOrderState(final AlgoOrder algoOrder) {
        String intId = algoOrder.getIntId();
        if (intId == null) {
            throw new ServiceException("Order intId is null");
        }
        return getAlgoOrderState(intId);
    }

    protected final Optional<S> getAlgoOrderState(final String intId) {
        S algoOrderState = this.algoOrderStates.get(intId);
        return Optional.ofNullable(algoOrderState);
    }

    protected final void removeAlgoOrderState(final AlgoOrder algoOrder) {
        this.algoOrderStates.remove(algoOrder.getIntId());
    }

    @Override
    public final void validateOrder(final T algoOrder) throws OrderValidationException {

        Validate.notNull(algoOrder, "AlgoOrder is null");

        String intId = algoOrder.getIntId();
        S algoOrderState = intId != null ? this.algoOrderStates.get(intId) : null;
        if (algoOrderState == null) {
            algoOrderState = handleValidateOrder(algoOrder);
        }

        // validate order specific properties
        algoOrder.validate();

        // check that the security is tradeable
        Security security = algoOrder.getSecurity();
        if (!security.getSecurityFamily().isTradeable()) {
            throw new OrderValidationException(security + " is not tradeable: " + algoOrder);
        }

        validateSideAndQuantity(algoOrder, algoOrderState);

        if (intId != null) {
            this.algoOrderStates.putIfAbsent(intId, algoOrderState);
        }
    }

    protected abstract S handleValidateOrder(final T algoOrder) throws OrderValidationException;

    protected void validateSideAndQuantity(final T algoOrder, final S algoOrderState) throws OrderValidationException {

        // validate general properties
        if (algoOrder.getSide() == null) {
            throw new OrderValidationException("Missing order side: " + algoOrder);
        }
        if (algoOrder.getQuantity() <= 0) {
            throw new OrderValidationException("Order quantity cannot be zero or negative: " + algoOrder);
        }
    }

    @Override
    public final void sendOrder(final T algoOrder) {

        Validate.notNull(algoOrder, "AlgoOrder is null");

        String intId = algoOrder.getIntId();
        if (intId == null) {
            throw new ServiceException("Order intId is null");
        }
        handleSendOrder(algoOrder, getAlgoOrderState(algoOrder).orElseThrow(() -> new ServiceException("Unknown order " + intId)));
    }

    protected abstract void handleSendOrder(final T algoOrder, final S algoOrderState);

    @Override
    public final void modifyOrder(final T algoOrder) {

        Validate.notNull(algoOrder, "AlgoOrder is null");

        String intId = algoOrder.getIntId();
        handleModifyOrder(algoOrder, getAlgoOrderState(algoOrder).orElseThrow(() -> new ServiceException("Unknown order " + intId)));
    }

    protected abstract void handleModifyOrder(final T algoOrder, final S algoOrderState);

    @Override
    public final void cancelOrder(final T algoOrder) {

        Validate.notNull(algoOrder, "AlgoOrder is null");

        String intId = algoOrder.getIntId();
        handleCancelOrder(algoOrder, getAlgoOrderState(algoOrder).orElseThrow(() -> new ServiceException("Unknown order " + intId)));

        List<Order> openChildOrders = this.orderExecutionService.getOpenOrdersByParentIntId(intId);
        for (Order childOrder : openChildOrders) {
            if (childOrder instanceof SimpleOrder) {
                this.simpleOrderService.cancelOrder((SimpleOrder) childOrder);
            }
        }

        OrderStatusVO executionStatus = this.orderExecutionService.getStatusByIntId(intId);
        if (executionStatus != null) {
            OrderStatus algoOrderStatus = OrderStatus.Factory.newInstance();
            algoOrderStatus.setIntId(intId);
            algoOrderStatus.setStatus(Status.CANCELED);
            algoOrderStatus.setFilledQuantity(executionStatus.getFilledQuantity());
            algoOrderStatus.setRemainingQuantity(executionStatus.getRemainingQuantity());
            algoOrderStatus.setOrder(algoOrder);

            this.orderExecutionService.handleOrderStatus(algoOrderStatus);
        }

        this.algoOrderStates.remove(intId);
    }

    protected abstract void handleCancelOrder(final T order, final S algoOrderState);

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

        OrderStatus algoOrderStatus = createAlgoOrderStatusOnChildStatus(algoOrder, orderStatus);
        if (algoOrderStatus != null) {

            this.orderExecutionService.handleOrderStatus(algoOrderStatus);
            handleOrderStatus(algoOrder, algoOrderState, algoOrderStatus);
        }

        handleChildOrderStatus(algoOrder, algoOrderState, orderStatus);

        if (algoOrderStatus != null && algoOrderStatus.getStatus() == Status.REJECTED) {
            this.algoOrderStates.remove(intId);
        }
    }

    @Override
    public final void onChildFill(final T algoOrder, final Fill fill) {

        String intId = algoOrder.getIntId();
        S algoOrderState = this.algoOrderStates.get(intId);
        if (algoOrderState == null) {
            return;
        }

        handleChildFill(algoOrder, algoOrderState, fill);

        OrderStatus algoOrderStatus = createAlgoOrderStatusOnFill(algoOrder, algoOrderState, fill);
        if (algoOrderStatus != null) {

            this.orderExecutionService.handleOrderStatus(algoOrderStatus);
            handleOrderStatus(algoOrder, algoOrderState, algoOrderStatus);

            if (algoOrderStatus.getStatus() == Status.EXECUTED) {
                this.algoOrderStates.remove(intId);
            }
        }
    }

    protected OrderStatus createAlgoOrderStatusOnChildStatus(final T algoOrder, final OrderStatus orderStatus) {

        if (orderStatus.getStatus() == Status.SUBMITTED) {
            String intId = algoOrder.getIntId();
            OrderStatusVO execStatus = this.orderExecutionService.getStatusByIntId(intId);
            if (execStatus != null && execStatus.getStatus() == Status.OPEN) {
                OrderStatus algoOrderStatus = OrderStatus.Factory.newInstance();
                algoOrderStatus.setStatus(Status.SUBMITTED);
                algoOrderStatus.setIntId(intId);
                algoOrderStatus.setExtDateTime(orderStatus.getExtDateTime());
                algoOrderStatus.setFilledQuantity(0L);
                algoOrderStatus.setRemainingQuantity(execStatus.getRemainingQuantity());
                algoOrderStatus.setOrder(algoOrder);
                return algoOrderStatus;
            }
        }
        return null;
    }

    protected OrderStatus createAlgoOrderStatusOnFill(final T algoOrder, final S algoOrderState, final Fill fill) {

        OrderStatus algoOrderStatus = null;
        String intId = algoOrder.getIntId();
        OrderStatusVO execStatus = this.orderExecutionService.getStatusByIntId(intId);
        if (execStatus != null) {
            algoOrderStatus = OrderStatus.Factory.newInstance();
            algoOrderStatus.setStatus(execStatus.getRemainingQuantity() - fill.getQuantity() > 0 ? Status.PARTIALLY_EXECUTED : Status.EXECUTED);
            algoOrderStatus.setIntId(intId);
            algoOrderStatus.setDateTime(algoOrderStatus.getExtDateTime());
            algoOrderStatus.setFilledQuantity(execStatus.getFilledQuantity() + fill.getQuantity());
            algoOrderStatus.setRemainingQuantity(execStatus.getRemainingQuantity() - fill.getQuantity());
            algoOrderStatus.setOrder(algoOrder);

        }
        return algoOrderStatus;
    }

    protected void handleChildFill(final T algoOrder, final S algoOrderState, final Fill fill) {
    }

    protected void handleOrderStatus(T algoOrder, final S algoOrderState, final OrderStatus orderStatus) {
    }

    protected void handleChildOrderStatus(final T algoOrder, final S algoOrderState, final OrderStatus orderStatus) {
    }

}
