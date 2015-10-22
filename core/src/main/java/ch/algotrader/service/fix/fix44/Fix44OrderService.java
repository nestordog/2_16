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
package ch.algotrader.service.fix.fix44;

import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.service.fix.FixOrderService;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public interface Fix44OrderService extends FixOrderService {

    /**
     * Called before sending the Order so that Broker specific Tags can be set.
     */
    public void prepareSendOrder(SimpleOrder order, NewOrderSingle newOrder);

    /**
     * Called before modifying the Order so that Broker specific Tags can be set.
     */
    public void prepareModifyOrder(SimpleOrder order, OrderCancelReplaceRequest replaceRequest);

    /**
     * Called before canceling the Order so that Broker specific Tags can be set.
     */
    public void prepareCancelOrder(SimpleOrder order, OrderCancelRequest cancelRequest);

}
