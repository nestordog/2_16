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

import ch.algotrader.entity.trade.AlgoOrder;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.OrderStatus;

/**
 * Internal Algo order service intended to initiate algo order operations
 * such as submission of a new order, modification or cancellation of
 * an existing order, order validation, as well as handle child order status
 * updates and fills.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public interface AlgoOrderService extends GenericOrderService<AlgoOrder> {

    /**
     * Handles child order status.
     */
    void handleChildOrderStatus(OrderStatus order);

    /**
     * Handles child order status.
     */
    void handleChildFill(Fill fill);

}