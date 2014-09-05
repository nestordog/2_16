/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
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
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.service.sim;

import java.math.BigDecimal;

import org.apache.commons.lang.Validate;

import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.LimitOrderI;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.enumeration.Direction;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.service.ExternalOrderServiceImpl;
import ch.algotrader.service.OrderService;
import ch.algotrader.service.TransactionService;
import ch.algotrader.util.DateUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class SimulationOrderServiceImpl extends ExternalOrderServiceImpl implements SimulationOrderService {

    private final TransactionService transactionService;

    private final OrderService orderService;

    public SimulationOrderServiceImpl(final TransactionService transactionService,
            final OrderService orderService) {

        Validate.notNull(transactionService, "TransactionService is null");
        Validate.notNull(orderService, "OrderService is null");

        this.transactionService = transactionService;
        this.orderService = orderService;
    }

    @Override
    public void sendOrder(SimpleOrder order) {

        Validate.notNull(order, "Order is null");

        try {
            // create one fill per order
            Fill fill = Fill.Factory.newInstance();
            fill.setDateTime(DateUtil.getCurrentEPTime());
            fill.setExtDateTime(DateUtil.getCurrentEPTime());
            fill.setSide(order.getSide());
            fill.setQuantity(order.getQuantity());
            fill.setPrice(getPrice(order));
            fill.setOrder(order);
            fill.setExecutionCommission(getExecutionCommission(order));
            fill.setClearingCommission(getClearingCommission(order));
            fill.setFee(getFee(order));

            // propagate the fill
            this.transactionService.propagateFill(fill);

            // create the transaction
            this.transactionService.createTransaction(fill);

            // create and OrderStatus
            OrderStatus orderStatus = OrderStatus.Factory.newInstance();
            orderStatus.setStatus(Status.EXECUTED);
            orderStatus.setFilledQuantity(order.getQuantity());
            orderStatus.setRemainingQuantity(0);
            orderStatus.setOrder(order);

            // send the orderStatus to base
            EngineLocator.instance().getBaseEngine().sendEvent(orderStatus);

            // propagate the OrderStatus to the strategy
            this.orderService.propagateOrderStatus(orderStatus);
        } catch (Exception ex) {
            throw new SimulationOrderServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getPrice(final SimpleOrder order) {

        Validate.notNull(order, "Order is null");

        try {
            if (order instanceof LimitOrderI) {

                // limit orders are executed at their limit price
                return ((LimitOrderI) order).getLimit();

            } else {

                Security security = order.getSecurity();

                // all other orders are executed the the market
                return security.getCurrentMarketDataEvent().getMarketValue(Side.BUY.equals(order.getSide()) ? Direction.SHORT : Direction.LONG)
                        .setScale(security.getSecurityFamily().getScale(), BigDecimal.ROUND_HALF_UP);
            }
        } catch (Exception ex) {
            throw new SimulationOrderServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getExecutionCommission(final SimpleOrder order) {

        Validate.notNull(order, "Order is null");

        try {
            return null;
        } catch (Exception ex) {
            throw new SimulationOrderServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getClearingCommission(final SimpleOrder order) {

        Validate.notNull(order, "Order is null");

        try {
            return null;
        } catch (Exception ex) {
            throw new SimulationOrderServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getFee(final SimpleOrder order) {

        Validate.notNull(order, "Order is null");

        try {
            return null;
        } catch (Exception ex) {
            throw new SimulationOrderServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    public void validateOrder(SimpleOrder order) {

        // do nothing
    }

    @Override
    public void cancelOrder(SimpleOrder order) {

        throw new UnsupportedOperationException("cancel order not supported in simulation");
    }

    @Override
    public void modifyOrder(SimpleOrder order) {

        throw new UnsupportedOperationException("modify order not supported in simulation");
    }

    @Override
    public OrderServiceType getOrderServiceType() {

        return OrderServiceType.SIMULATION;
    }
}
