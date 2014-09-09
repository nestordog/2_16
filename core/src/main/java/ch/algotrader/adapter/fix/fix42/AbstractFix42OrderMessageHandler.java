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
package ch.algotrader.adapter.fix.fix42;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.enumeration.Status;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.service.LookupService;
import ch.algotrader.util.MyLogger;
import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.field.ClOrdID;
import quickfix.field.ExecTransType;
import quickfix.field.OrigClOrdID;
import quickfix.field.Text;
import quickfix.field.TransactTime;
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.OrderCancelReject;

/**
 * Abstract FIX/4.2 order message handler implementing generic functionality common to all broker specific
 * interfaces..
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class AbstractFix42OrderMessageHandler extends AbstractFix42MessageHandler {

    private static Logger LOGGER = MyLogger.getLogger(AbstractFix42OrderMessageHandler.class.getName());

    private LookupService lookupService;

    public void setLookupService(final LookupService lookupService) {
        this.lookupService = lookupService;
    }

    public LookupService getLookupService() {
        return this.lookupService;
    }

    protected abstract boolean discardReport(ExecutionReport executionReport) throws FieldNotFound;

    protected abstract boolean isOrderRejected(ExecutionReport executionReport) throws FieldNotFound;

    protected abstract OrderStatus createStatus(ExecutionReport executionReport, Order order) throws FieldNotFound;

    protected abstract Fill createFill(ExecutionReport executionReport, Order order) throws FieldNotFound;

    public void onMessage(ExecutionReport executionReport, SessionID sessionID) throws FieldNotFound {

        if (discardReport(executionReport)) {

            return;
        }

        String intId = executionReport.getClOrdID().getValue();

        // check ExecTransType
        if (executionReport.isSetExecTransType() && executionReport.getExecTransType().getValue() != ExecTransType.NEW) {
            throw new UnsupportedOperationException("order " + intId + " has received an unsupported ExecTransType of: " + executionReport.getExecTransType().getValue());
        }

        // get the order from the OpenOrderWindow
        Order order = lookupService.getOpenOrderByRootIntId(intId);

        if (order == null) {

            LOGGER.error("order with intId " + intId + " could not be found for execution " + executionReport);
            return;
        }

        if (isOrderRejected(executionReport)) {

            if (LOGGER.isEnabledFor(Level.ERROR)) {

                StringBuilder buf = new StringBuilder();
                buf.append("Order with int ID ").append(intId).append(" has been rejected");
                if (executionReport.isSetField(Text.FIELD)) {

                    buf.append("; reason given: ").append(executionReport.getText().getValue());
                }
                LOGGER.error(buf.toString());
            }

            OrderStatus orderStatus = OrderStatus.Factory.newInstance();
            orderStatus.setStatus(Status.REJECTED);
            orderStatus.setIntId(intId);
            orderStatus.setOrder(order);
            if (executionReport.isSetField(TransactTime.FIELD)) {

                orderStatus.setExtDateTime(executionReport.getTransactTime().getValue());
            }
            if (executionReport.isSetField(Text.FIELD)) {

                orderStatus.setReason(executionReport.getText().getValue());
            }

            EngineLocator.instance().getBaseEngine().sendEvent(orderStatus);

            return;
        }

        OrderStatus orderStatus = createStatus(executionReport, order);
        orderStatus.setOrder(order);

        EngineLocator.instance().getBaseEngine().sendEvent(orderStatus);

        Fill fill = createFill(executionReport, order);
        if (fill != null) {

            // associate the fill with the order
            fill.setOrder(order);

            EngineLocator.instance().getBaseEngine().sendEvent(fill);
        }
    }

    public void onMessage(final OrderCancelReject reject, final SessionID sessionID) throws FieldNotFound {

        if (LOGGER.isEnabledFor(Level.ERROR)) {

            StringBuilder buf = new StringBuilder();
            buf.append("Order cancel/replace has been rejected");
            if (reject.isSetField(ClOrdID.FIELD)) {
                String clOrdID = reject.getClOrdID().getValue();
                buf.append(" [order ID: ").append(clOrdID).append("]");
            }
            if (reject.isSetField(OrigClOrdID.FIELD)) {
                String origClOrdID = reject.getOrigClOrdID().getValue();
                buf.append(" [original order ID: ").append(origClOrdID).append("]");
            }
            if (reject.isSetField(Text.FIELD)) {
                String text = reject.getText().getValue();
                buf.append(": ").append(text);
            }
            LOGGER.error(buf.toString());
        }
    }

}
