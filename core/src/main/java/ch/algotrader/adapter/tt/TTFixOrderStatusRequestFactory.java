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
package ch.algotrader.adapter.tt;

import ch.algotrader.adapter.fix.FixApplicationException;
import ch.algotrader.entity.trade.Order;
import quickfix.field.ClOrdID;
import quickfix.field.OrderID;
import quickfix.fix42.OrderStatusRequest;

/**
 * Trading Technologies order status request factory.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class TTFixOrderStatusRequestFactory {

    public OrderStatusRequest create(final Order order) {

        OrderStatusRequest request = new OrderStatusRequest();
        String extId = order.getExtId();
        if (extId != null) {
            request.set(new OrderID(extId));
        } else {
            String intId = order.getIntId();
            if (intId != null) {
                request.set(new ClOrdID(intId));
            } else {
                throw new FixApplicationException("Missing order identifier (intId and extId)");
            }
        }
        return request;
    }

}
