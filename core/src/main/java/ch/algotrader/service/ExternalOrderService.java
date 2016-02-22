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

import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.enumeration.SimpleOrderType;
import ch.algotrader.enumeration.TIF;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public interface ExternalOrderService extends GenericOrderService<SimpleOrder> {

    /**
     * Returns the order service type associated with this ExternalOrderService.
     */
    String getOrderServiceType();

    /**
     * Returns default time-in-force value.
     */
    TIF getDefaultTIF(SimpleOrderType orderType);

}
