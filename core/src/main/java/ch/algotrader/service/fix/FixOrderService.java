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
package ch.algotrader.service.fix;

import quickfix.Message;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.service.ExternalOrderService;
import ch.algotrader.service.InitializingServiceI;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface FixOrderService extends ExternalOrderService, InitializingServiceI {

    /**
     * Sends an Order to the external Broker and propagates the Order to the Base Esper Engine.
     */
    public void sendOrder(Order order, Message message, boolean propagate);

}
