/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.service.sim;

import java.math.BigDecimal;

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
import ch.algotrader.util.DateUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class SimulationOrderServiceImpl extends SimulationOrderServiceBase {

    @Override
    protected void handleSendOrder(SimpleOrder order) throws Exception {

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
        getTransactionService().propagateFill(fill);

        // create the transaction
        getTransactionService().createTransaction(fill);

        // create and OrderStatus
        OrderStatus orderStatus = OrderStatus.Factory.newInstance();
        orderStatus.setStatus(Status.EXECUTED);
        orderStatus.setFilledQuantity(order.getQuantity());
        orderStatus.setRemainingQuantity(0);
        orderStatus.setOrder(order);

        // send the orderStatus to base
        EngineLocator.instance().getBaseEngine().sendEvent(orderStatus);

        // propagate the OrderStatus to the strategy
        getOrderService().propagateOrderStatus(orderStatus);
    }

    @Override
    protected BigDecimal handleGetPrice(SimpleOrder order) {

        if (order instanceof LimitOrderI) {

            // limit orders are executed at their limit price
            return ((LimitOrderI) order).getLimit();

        } else {

            Security security = order.getSecurity();

            // all other orders are executed the the market
            return security.getCurrentMarketDataEvent().getMarketValue(Side.BUY.equals(order.getSide()) ? Direction.SHORT : Direction.LONG)
                    .setScale(security.getSecurityFamily().getScale(), BigDecimal.ROUND_HALF_UP);
        }
    }

    @Override
    protected BigDecimal handleGetExecutionCommission(SimpleOrder order) throws Exception {
        return null;
    }

    @Override
    protected BigDecimal handleGetClearingCommission(SimpleOrder order) throws Exception {
        return null;
    }

    @Override
    protected BigDecimal handleGetFee(SimpleOrder order) throws Exception {
        return null;
    }

    @Override
    protected void handleValidateOrder(SimpleOrder order) throws Exception {
        // do nothing
    }

    @Override
    protected void handleCancelOrder(SimpleOrder order) throws Exception {
        throw new UnsupportedOperationException("cancel order not supported in simulation");
    }

    @Override
    protected void handleModifyOrder(SimpleOrder order) throws Exception {
        throw new UnsupportedOperationException("modify order not supported in simulation");
    }

    @Override
    protected OrderServiceType handleGetOrderServiceType() throws Exception {
        return OrderServiceType.SIMULATION;
    }

}
