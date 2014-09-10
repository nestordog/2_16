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
import java.util.Map;

import ch.algotrader.entity.Account;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderCompletion;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.OrderValidationException;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface OrderService {

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
    public void modifyOrder(String intId, Map properties);

    /**
     * Propagates an Order to the corresponding Strategy.
     */
    public void propagateOrder(Order order);

    /**
     * Propagates an {@link OrderStatus} to the corresponding Strategy.
     */
    public void propagateOrderStatus(OrderStatus orderStatus);

    public void propagateOrderCompletion(OrderCompletion orderCompletion);

    /**
     * Propagates an {@link OrderStatus} to the corresponding Strategy.
     */
    public void updateOrderId(int id, String intId, String extId);

    /**
     * Sends a Trade Suggestion via Email / Text Message.
     */
    public void suggestOrder(Order order);

    /**
     * Generates next order id for the given account.
     */
    public String getNextOrderId(Account account);

}
