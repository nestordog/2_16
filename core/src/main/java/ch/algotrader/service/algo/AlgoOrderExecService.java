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
package ch.algotrader.service.algo;

import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.algo.AlgoOrder;
import ch.algotrader.service.GenericOrderService;

/**
 * Algo execution service.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public interface AlgoOrderExecService<T extends AlgoOrder> extends GenericOrderService<T> {

    /**
     * Returns the algo order type associated with this AlgoOrderExecService.
     */
    Class<? extends AlgoOrder> getAlgoOrderType();

    /**
     * Handles order status updates of the child order
     */
    void handleChildOrderStatus(T order, OrderStatus orderStatus);

    /**
     * Handles fill of the child order
     */
    void handleChildFill(T order, Fill fill);

}
