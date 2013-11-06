/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.adapter.rt;

import quickfix.FieldNotFound;
import quickfix.SessionSettings;
import quickfix.fix44.ExecutionReport;
import ch.algotrader.adapter.fix.Fix44MessageHandler;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;

/**
 * RealTick specific Fix44MessageHandler.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class RTFixMessageHandler extends Fix44MessageHandler {

    public RTFixMessageHandler(SessionSettings settings) {
        super(settings);
    }

    @Override
    protected void processOrderStatus(ExecutionReport executionReport, Order order, OrderStatus orderStatus) throws FieldNotFound {

        // Note: store OrderID sind RealTick requires it for cancels and replaces
        if (executionReport.getOrderID() != null && (order.getExtId() == null || !order.getExtId().equals(executionReport.getOrderID()))) {
            orderStatus.setExtId(executionReport.getOrderID().getValue());
        }
    }
}
