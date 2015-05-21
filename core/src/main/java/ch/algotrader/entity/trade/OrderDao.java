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
package ch.algotrader.entity.trade;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import ch.algotrader.hibernate.ReadWriteDao;

/**
 * DAO for {@link ch.algotrader.entity.trade.Order} objects.
 *
 * @see ch.algotrader.entity.trade.Order
 */
public interface OrderDao extends ReadWriteDao<Order> {

    /**
      * Finds one Tick-Id per hour of the defined Security that is just before the specified number
      * of {@code minutes} and after the specified {@code minDate}.
      * @param sessionQualifier
      * @return BigDecimal
      */
    BigDecimal findLastIntOrderId(String sessionQualifier);

    /**
     * Finds orders that have not been acknowledged yet, i.e. have not received any OrderStatus yet.
     * @return List<Integer>
     */
    List<Integer> findUnacknowledgedOrderIds();

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
    List<Order> findByIds(List<Integer> ids);

    /**
     * Finds all open orders by querying the OpenOrderWindow
     * @return Collection<Order>
     */
    Collection<Order> findAllOpenOrders();

    /**
     * Finds all open orders for the specified Strategy by querying the OpenOrderWindow
     * @param strategyName
     * @return Collection<Order>
     */
    Collection<Order> findOpenOrdersByStrategy(String strategyName);

    /**
     * Finds all open orders for the specified Strategy and Security by querying the OpenOrderWindow
     * @param strategyName
     * @param securityId
     * @return Collection<Order>
     */
    Collection<Order> findOpenOrdersByStrategyAndSecurity(String strategyName, int securityId);

    /**
     * Finds an open order by its {@code intId} by querying the OpenOrderWindow
     * @param intId
     * @return Order
     */
    Order findOpenOrderByIntId(String intId);

    /**
     * Finds an open order by its {@code rootIntId} by querying the OpenOrderWindow
     * @param intId
     * @return Order
     */
    Order findOpenOrderByRootIntId(String intId);

    /**
     * Finds an open order by its {@code extId} by querying the OpenOrderWindow
     * @param extId
     * @return Order
     */
    Order findOpenOrderByExtId(String extId);

    /**
     * Finds an open order by the {@code intId} of its parent order by querying the OpenOrderWindow
     * @param parentIntId
     * @return Collection<Order>
     */
    Collection<Order> findOpenOrdersByParentIntId(String parentIntId);

    // spring-dao merge-point
}
