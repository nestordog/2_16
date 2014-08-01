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
package ch.algotrader.adapter.cnx;

import ch.algotrader.adapter.fix.fix44.GenericFix44OrderMessageHandler;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.enumeration.Status;
import quickfix.FieldNotFound;
import quickfix.field.ClOrdID;
import quickfix.field.ExecType;
import quickfix.field.OrderID;
import quickfix.fix44.ExecutionReport;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class CNXFixOrderMessageHandler extends GenericFix44OrderMessageHandler {

    @Override
    protected OrderStatus createStatus(final ExecutionReport executionReport, final Order order) throws FieldNotFound {
        OrderStatus orderStatus = super.createStatus(executionReport, order);
        if (orderStatus.getStatus() == Status.SUBMITTED) {

            // Peculiarities of Currenex....

            // Need to keep the original OrderID to be able to cancel the order
            OrderID orderID = executionReport.getOrderID();
            order.setExtId(orderID.getValue());

            // Need to update order's IntId with ClOrdID if replaced
            ExecType execType = executionReport.getExecType();
            if (execType.getValue() == ExecType.REPLACE) {

                ClOrdID clOrdID = executionReport.getClOrdID();
                order.setIntId(clOrdID.getValue());
                orderStatus.setIntId(clOrdID.getValue());

            }
        }
        return orderStatus;
    }
}
