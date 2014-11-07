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

import java.util.List;

import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;

/**
 * Order event persistence strategy.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public interface OrderPersistenceService {

    /**
     * Persists the given {@link ch.algotrader.entity.trade.Order} instance.
     */
    void persistOrder(Order order);

    /**
     * Persists the given {@link ch.algotrader.entity.trade.OrderStatus} instance.
     */
    void persistOrderStatus(OrderStatus orderStatus);

    /**
     * Loads pending orders. An order is considered pending if the status of the last
     * {@link ch.algotrader.entity.trade.OrderStatus} event associated with the order is either
     * {@link ch.algotrader.enumeration.Status#OPEN},
     * {@link ch.algotrader.enumeration.Status#SUBMITTED} or
     * {@link ch.algotrader.enumeration.Status#PARTIALLY_EXECUTED}.
     */
    List<Order> loadPendingOrders();

}
