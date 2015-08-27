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
package ch.algotrader.ordermgmt;

import java.util.List;

import ch.algotrader.entity.trade.ExecutionStatusVO;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderDetailsVO;
import ch.algotrader.enumeration.Status;

/**
* Registry of active orders.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*/
public interface OpenOrderRegistry {

    /**
     * Adds the order to the registry.
     */
    void add(Order order);

    /**
     * Removes the order from the registry.
     */
    Order remove(String intId);

    /**
     * Returns the order with the given {@code IntId} or {@code null} if no order with the given {@code IntId}
     * can be found in the registry.
     */
    Order getByIntId(String intId);

    /**
     * Returns execution status of the order with the given {@code IntId} or {@code null} if no order
     * with the given {@code IntId} can be found in the registry.
     */
    ExecutionStatusVO getStatusByIntId(String intId);

    /**
     * Returns full details of the order with the given {@code IntId} or {@code null} if no order
     * with the given {@code IntId} can be found in the registry.
     */
    OrderDetailsVO getDetailsByIntId(String intId);

    /**
     * Updates execution status of the order with the given {@code IntId}.
     */
    void updateExecutionStatus(String intId, Status status, long filledQuantity, long remainingQuantity);

    /**
     * Returns all orders tracked by th registry.
     */
    List<Order> getAllOrders();

    /**
     * Returns full details of orders tracked by th registry.
     */
    List<OrderDetailsVO> getAllOrderDetails();

    /**
     * Returns full details of orders tracked by th registry for the given strategy.
     */
    List<OrderDetailsVO> getOrderDetailsForStrategy(String strategyName);

    /**
     * Returns all child orders of the parent order with the given {@code IntId}.
     */
    List<Order> findByParentIntId(String parentIntId);

}
