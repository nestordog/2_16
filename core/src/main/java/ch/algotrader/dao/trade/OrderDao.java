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
package ch.algotrader.dao.trade;

import java.math.BigDecimal;
import java.util.List;

import ch.algotrader.dao.ReadWriteDao;
import ch.algotrader.entity.trade.Order;

/**
 * DAO for {@link ch.algotrader.entity.trade.Order} objects.
 *
 * @see ch.algotrader.entity.trade.Order
 */
public interface OrderDao extends ReadWriteDao<Order> {

    /**
      * Finds last order id for the given session qualifier.
      * @param sessionQualifier session qualifier
      * @return BigDecimal
      */
    BigDecimal findLastIntOrderIdBySessionQualifier(String sessionQualifier);

    /**
     * Finds last order id for the given order service type.
     * @param orderServiceType order service type
     * @return BigDecimal
     */
    BigDecimal findLastIntOrderIdByServiceType(String orderServiceType);

    /**
     * Finds all orders of the current day in descending {@code dateTime} order.
     */
    List<Order> getDailyOrders();

    /**
     * Finds all orders of the current day of a specific Strategy in descending {@code dateTime} order.
     */
    List<Order> getDailyOrdersByStrategy(String strategyName);

    /**
     * Finds orders that have not been acknowledged yet, i.e. have not received any OrderStatus yet.
     * @return List<Long>
     */
    List<Long> findUnacknowledgedOrderIds();

    /**
     * Finds an order by its {@code intId}
     * @param intId
     * @return Order
     */
    Order findByIntId(String intId);

    /**
     * Finds multiple Orders by their {@code ids}.
     * @param ids
     * @return List<Order>
     */
    List<Order> findByIds(List<Long> ids);

}
