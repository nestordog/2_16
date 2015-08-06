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

import java.util.Collection;
import java.util.List;

import ch.algotrader.dao.ReadWriteDao;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.vo.client.OrderStatusVO;

/**
 * DAO for {@link ch.algotrader.entity.trade.OrderStatus} objects.
 *
 * @see ch.algotrader.entity.trade.OrderStatus
 */
public interface OrderStatusDao extends ReadWriteDao<OrderStatus> {

    /**
     * Finds the most current OrderStatus(OPEN, SUBMITTED or PARTIALLY_EXECUTED) per Order
     * @return List<OrderStatus>
     */
    List<OrderStatus> findPending();

    /**
     * Finds all OrderStati of currently open Orders.
     * @return Collection<OrderStatusVO>
     */
    Collection<OrderStatusVO> findAllOrderStati();

    /**
     * Finds the current OrderStatus of the Order with the specified {@code intId}
     * @param intId
     * @return OrderStatusVO
     */
    OrderStatusVO findOrderStatusByIntId(String intId);

    /**
     * Returns all OrderStati for open Orders of the specified Strategy.
     * @param strategyName
     * @return Collection<OrderStatusVO>
     */
    Collection<OrderStatusVO> findOrderStatiByStrategy(String strategyName);

    // spring-dao merge-point
}
