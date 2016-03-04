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

import java.util.Objects;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.entity.Position;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.OrderStatusVO;
import ch.algotrader.entity.trade.OrderValidationException;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.entity.trade.algo.AlgoOrder;
import ch.algotrader.entity.trade.algo.TargetPositionOrder;
import ch.algotrader.entity.trade.algo.TargetPositionOrderStateVO;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.OrderExecutionService;
import ch.algotrader.service.ServiceException;
import ch.algotrader.service.SimpleOrderService;
import ch.algotrader.util.BeanUtil;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class TargetPositionOrderService extends AbstractAlgoOrderExecService<TargetPositionOrder, TargetPositionOrderStateVO> {

    private static final Logger LOGGER = LogManager.getLogger(TargetPositionOrderService.class);

    private final OrderExecutionService orderExecutionService;
    private final LookupService lookupService;
    private final SimpleOrderService simpleOrderService;

    public TargetPositionOrderService(
            final OrderExecutionService orderExecutionService,
            final LookupService lookupService,
            final SimpleOrderService simpleOrderService) {

        super(orderExecutionService, simpleOrderService);

        Validate.notNull(lookupService, "LookupService is null");

        this.orderExecutionService = orderExecutionService;
        this.lookupService = lookupService;
        this.simpleOrderService = simpleOrderService;
    }

    @Override
    public Class<? extends AlgoOrder> getAlgoOrderType() {
        return TargetPositionOrder.class;
    }

    @Override
    protected TargetPositionOrderStateVO createAlgoOrderState(final TargetPositionOrder algoOrder) {

        return new TargetPositionOrderStateVO();
    }

    @Override
    protected void handleValidateOrder(final TargetPositionOrder order, final TargetPositionOrderStateVO algoOrderState) throws OrderValidationException {

        synchronized (algoOrderState) {

            long target = order.getTarget();
            if (order.getQuantity() != 0) {
                throw new OrderValidationException("Quantity cannot be set for target position order");
            }
            algoOrderState.setTargetQty(target);
        }
    }

    @Override
    public void handleSendOrder(final TargetPositionOrder order, final TargetPositionOrderStateVO algoOrderState) {

        Security security = order.getSecurity();
        Strategy strategy = order.getStrategy();
        Position position = this.lookupService.getPositionBySecurityAndStrategy(security.getId(), strategy.getName());
        long actualQty = position != null ? position.getQuantity() : 0L;
        long targetQty = order.getTarget();

        if (actualQty != targetQty) {

            synchronized (algoOrderState) {
                algoOrderState.setActualQty(actualQty);
                adjustPosition(order, algoOrderState);
            }

        } else if (!order.isKeepAlive()) {

            OrderStatus algoOrderStatus = OrderStatus.Factory.newInstance();
            algoOrderStatus.setStatus(Status.EXECUTED);
            algoOrderStatus.setIntId(order.getIntId());
            algoOrderStatus.setDateTime(algoOrderStatus.getExtDateTime());
            algoOrderStatus.setFilledQuantity(0L);
            algoOrderStatus.setOrder(order);

            this.orderExecutionService.handleOrderStatus(algoOrderStatus);
            removeAlgoOrderState(order);
        }
    }

    @Override
    protected void handleModifyOrder(final TargetPositionOrder order, final TargetPositionOrderStateVO algoOrderState) {

        synchronized (algoOrderState) {
            long targetQty = order.getTarget();
            algoOrderState.setTargetQty(targetQty);

            // Make adjustments if there is no open (unacknowledged) order
            if (algoOrderState.getOrderStatus() != Status.OPEN) {

                adjustPosition(order, algoOrderState);
            }
        }
    }

    @Override
    protected void handleCancelOrder(final TargetPositionOrder order, final TargetPositionOrderStateVO algoOrderState) {

        synchronized (algoOrderState) {
            algoOrderState.setOrderStatus(Status.CANCELED);
            algoOrderState.setIntId(null);
        }

    }

    void adjustPosition(final TargetPositionOrder order, final TargetPositionOrderStateVO algoOrderState) {

        long targetQty = algoOrderState.getTargetQty();
        long actualQty = algoOrderState.getActualQty();
        long delta = targetQty - actualQty;

        String workingOrderId = algoOrderState.getIntId();
        Status orderStatus = algoOrderState.getOrderStatus();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{} {}: target position = {}; actual position = {}; working order = {}",
                    order.getIntId(), order.getSecurity(), targetQty, actualQty, (workingOrderId != null ? workingOrderId + " (" + orderStatus + ")" : null));
        }

        if (orderStatus == Status.OPEN) {
            return;
        }

        if (workingOrderId != null) {

            OrderStatusVO execStatus = this.orderExecutionService.getStatusByIntId(workingOrderId);
            if (execStatus != null) {

                SimpleOrder workingOrder = (SimpleOrder) this.orderExecutionService.getOrderByIntId(workingOrderId);
                Side orderSide = workingOrder.getSide();

                Side targetSide = null;
                if (delta < 0) {
                    targetSide = Side.SELL;
                } if (delta > 0) {
                    targetSide = Side.BUY;
                }
                if (delta == 0 || targetSide != orderSide) {

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("{} {}: cancelling working order {}", order.getIntId(), order.getSecurity(), workingOrderId);
                    }
                    this.simpleOrderService.cancelOrder(workingOrder);
                } else {

                    if (execStatus.getRemainingQuantity() != Math.abs(delta)) {

                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("{} {}: modifying working order {}", order.getIntId(), order.getSecurity(), workingOrderId);
                        }

                        SimpleOrder modifiedOrder;
                        try {
                            modifiedOrder = BeanUtil.clone(workingOrder);
                            modifiedOrder.setId(0);
                            modifiedOrder.setQuantity(Math.abs(delta));
                            modifiedOrder.setParentOrder(order);
                        } catch (ReflectiveOperationException ex) {
                            throw new ServiceException(ex);
                        }
                        this.simpleOrderService.modifyOrder(modifiedOrder);
                    }
                    delta = 0;
                }
            }
        }

        if (delta != 0) {

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{} {}: order {}", order.getIntId(), order.getSecurity(), delta);
            }
            Side orderSide = delta < 0 ? Side.SELL : Side.BUY;

            MarketOrder marketOrder = MarketOrder.Factory.newInstance();
            marketOrder.setQuantity(Math.abs(delta));
            marketOrder.setSide(orderSide);
            marketOrder.setSecurity(order.getSecurity());
            marketOrder.setStrategy(order.getStrategy());
            marketOrder.setAccount(order.getAccount());
            marketOrder.setParentOrder(order);

            algoOrderState.setOrderStatus(Status.OPEN);

            this.simpleOrderService.sendOrder(marketOrder);
        }
    }

    @Override
    protected void handleChildOrderStatus(final TargetPositionOrder order, final TargetPositionOrderStateVO algoOrderState, final OrderStatus orderStatus) {

        synchronized (algoOrderState) {

            String intId = orderStatus.getIntId();
            Status status = orderStatus.getStatus();
            if (status == Status.SUBMITTED) {

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("{} {}: order {} acknowledged", order.getIntId(), order.getSecurity(), intId);
                }

                // Order accepted by the broker
                // Track this order as the actual working order
                algoOrderState.setIntId(orderStatus.getIntId());
                algoOrderState.setOrderStatus(status);
            } else if (Objects.equals(intId, algoOrderState.getIntId())) {

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("{} {}: order {} {}", order.getIntId(), order.getSecurity(), intId, status);
                }

                // Update working order status
                algoOrderState.setOrderStatus(status);
            }
        }
    }

    @Override
    protected OrderStatus createAlgoOrderStatusOnFill(final TargetPositionOrder algoOrder, final TargetPositionOrderStateVO algoOrderState, final Fill fill) {

        synchronized (algoOrderState) {

            if (!algoOrder.isKeepAlive()) {

                long targetQty = algoOrderState.getTargetQty();
                long actualQty = algoOrderState.getActualQty();

                OrderStatus algoOrderStatus = OrderStatus.Factory.newInstance();
                algoOrderStatus.setStatus(actualQty != targetQty ? Status.PARTIALLY_EXECUTED : Status.EXECUTED);
                algoOrderStatus.setIntId(algoOrder.getIntId());
                algoOrderStatus.setDateTime(algoOrderStatus.getExtDateTime());
                algoOrderStatus.setFilledQuantity(fill.getQuantity());
                algoOrderStatus.setOrder(algoOrder);
                return algoOrderStatus;
            } else {
                return null;
            }
        }
    }

    @Override
    protected void handleChildFill(final TargetPositionOrder order, final TargetPositionOrderStateVO algoOrderState, final Fill fill) {

        synchronized (algoOrderState) {

            long targetQty = algoOrderState.getTargetQty();
            long actualQty = algoOrderState.getActualQty();
            Side side = fill.getSide();
            long delta = side == Side.BUY ? fill.getQuantity() : -fill.getQuantity();
            actualQty += delta;
            algoOrderState.setActualQty(actualQty);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{} {}: position update {}", order.getIntId(), order.getSecurity(), delta);
            }

            Status status = algoOrderState.getOrderStatus();

            // If the last child order has been fully executed
            // re-adjust the actual position if not equal to the target
            if (status == Status.EXECUTED && targetQty != actualQty) {

                adjustPosition(order, algoOrderState);
            }
        }
    }
}
