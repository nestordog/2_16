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
package ch.algotrader.service;

import java.util.Map;

import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;

/**
 * Service to load persistent state of the Algotrader server.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public interface ServerStateLoaderService {

    /**
     * Loads pending orders. An order is considered pending if the status of the last
     * {@link OrderStatus} event associated with the order is either
     * {@link ch.algotrader.enumeration.Status#OPEN},
     * {@link ch.algotrader.enumeration.Status#SUBMITTED} or
     * {@link ch.algotrader.enumeration.Status#PARTIALLY_EXECUTED}
     * or there are no events associated with the order.
     */
    Map<Order, OrderStatus> loadPendingOrders();

}
