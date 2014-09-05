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
package ch.algotrader.service.fix.fix42;

import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelReplaceRequest;
import quickfix.fix42.OrderCancelRequest;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.service.fix.FixOrderService;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface Fix42OrderService extends FixOrderService {

    /**
     * Called before sending the Order so that Broker specific Tags can be set.
     */
    public void sendOrder(SimpleOrder order, NewOrderSingle newOrder);

    /**
     * Called before modifying the Order so that Broker specific Tags can be set.
     */
    public void modifyOrder(SimpleOrder order, OrderCancelReplaceRequest replaceRequest);

    /**
     * Called before canceling the Order so that Broker specific Tags can be set.
     */
    public void cancelOrder(SimpleOrder order, OrderCancelRequest cancelRequest);

}
