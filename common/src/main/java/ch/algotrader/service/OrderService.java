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
package ch.algotrader.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ch.algotrader.entity.trade.ExecutionStatusVO;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderCompletionVO;
import ch.algotrader.entity.trade.OrderDetailsVO;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.OrderVO;
import ch.algotrader.entity.trade.OrderValidationException;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface OrderService {

    /**
     * Creates a new Order based on the {@link ch.algotrader.entity.trade.OrderPreference
     * OrderPreference} selected by its {@code name}.
     */
    public Order createOrderByOrderPreference(final String name);

    /**
     * Validates an Order. It is suggested to call this method by itself prior to sending an Order.
     * However {@link #sendOrder} will invoke this method again.
     */
    public void validateOrder(Order order) throws OrderValidationException;

    /**
     * Sends an Order.
     */
    public void sendOrder(Order order);

    /**
     * Sends an Order.
     */
    public void sendOrder(OrderVO order);

    /**
     * Sends multiple Orders.
     */
    public void sendOrders(Collection<Order> orders);

    /**
     * Cancels an Order.
     */
    public void cancelOrder(Order order);

    /**
     * Cancels an Order by its {@code intId}.
     */
    public void cancelOrder(String intId);

    /**
     * Cancels all Orders.
     */
    public void cancelAllOrders();

    /**
     * Modifies an Order by overwriting the current Order with the Order passed to this method.
     */
    public void modifyOrder(Order order);

    /**
     * Modifies an Order defined by its {@code intId} by overwriting the current Order with the
     * defined {@code properties}.
     */
    public void modifyOrder(String intId, Map<String, String> properties);

    /**
     * Modifies an Order by overwriting the current Order with the Order passed to this method.
     */
    public void modifyOrder(OrderVO order);

    /**
     * Propagates an {@link OrderStatus} to the corresponding Strategy.
     */
    public void propagateOrderStatus(OrderStatus orderStatus);

    /**
     * Propagates an {@link OrderCompletionVO} to the corresponding Strategy.
     */
    public void propagateOrderCompletion(OrderCompletionVO orderCompletion);

    /**
     * Generates next order intId for the given account.
     */
    public String getNextOrderId(long accountId);

    /**
     * Returns details of currently open orders.
     */
    public List<OrderDetailsVO> getOpenOrderDetails();

    /**
     * Returns details of recently executed orders.
     */
    public List<OrderDetailsVO> getRecentOrderDetails();

    /**
     * Returns execution status of the order with the given {@code IntId} or {@code null}
     * if an order with this {@code IntId} has been fully executed.
     */
    ExecutionStatusVO getStatusByIntId(String intId);

    /**
     * Gets an order (open or completed) by its {@code intId}.
     */
    public Order getOrderByIntId(String intId);

    /**
     * Evicts executed orders from the internal cache.
     */
    public void evictExecutedOrders();

    /**
     * Loads pending orders. An order is considered pending if the status of the last
     * {@link ch.algotrader.entity.trade.OrderStatus} event associated with the order is either
     * {@link ch.algotrader.enumeration.Status#OPEN},
     * {@link ch.algotrader.enumeration.Status#SUBMITTED} or
     * {@link ch.algotrader.enumeration.Status#PARTIALLY_EXECUTED}
     * or there are no events associated with the order.
     */
    Map<Order, OrderStatus> loadPendingOrders();

}
