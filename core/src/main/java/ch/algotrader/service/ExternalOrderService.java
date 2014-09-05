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

import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.enumeration.OrderServiceType;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface ExternalOrderService {

    /**
     * Validates an Order.
     */
    public void validateOrder(SimpleOrder order);

    /**
     * Sends an Order.
     */
    public void sendOrder(SimpleOrder order);

    /**
     * Cancels an Order.
     */
    public void cancelOrder(SimpleOrder order);

    /**
     * Modifies an Order.
     */
    public void modifyOrder(SimpleOrder order);

    /**
     * Returns the {@link OrderServiceType} associated with this ExternalOrderService.
     */
    public OrderServiceType getOrderServiceType();

}
