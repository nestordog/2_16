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
package ch.algotrader.adapter.fix.fix44;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.enumeration.Status;
import ch.algotrader.esper.Engine;
import ch.algotrader.service.LookupService;
import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.field.MsgSeqNum;
import quickfix.field.OrdStatus;
import quickfix.field.Text;
import quickfix.field.TransactTime;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.OrderCancelReject;

/**
 * Abstract FIX44 order message handler implementing generic functionality common to all broker specific
 * interfaces..
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class AbstractFix44OrderMessageHandler extends AbstractFix44MessageHandler {

    private static Logger LOGGER = Logger.getLogger(AbstractFix44OrderMessageHandler.class.getName());

    private final LookupService lookupService;
    private final Engine serverEngine;

    protected AbstractFix44OrderMessageHandler(final LookupService lookupService, final Engine serverEngine) {
        this.lookupService = lookupService;
        this.serverEngine = serverEngine;
    }

    protected abstract boolean discardReport(ExecutionReport executionReport) throws FieldNotFound;

    protected abstract boolean isOrderRejected(ExecutionReport executionReport) throws FieldNotFound;

    protected abstract OrderStatus createStatus(ExecutionReport executionReport, Order order) throws FieldNotFound;

    protected abstract Fill createFill(ExecutionReport executionReport, Order order) throws FieldNotFound;

    public void onMessage(final ExecutionReport executionReport, final SessionID sessionID) throws FieldNotFound {

        if (discardReport(executionReport)) {

            return;
        }

        String intId = executionReport.getClOrdID().getValue();

        // get the order from the OpenOrderWindow
        Order order = this.lookupService.getOpenOrderByRootIntId(intId);
        if (order == null) {

            if (LOGGER.isEnabledFor(Level.ERROR)) {

                LOGGER.error("Order with int ID " + intId + " matching the execution report could not be found");
            }
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
            orderStatus.setSequenceNumber(executionReport.getHeader().getInt(MsgSeqNum.FIELD));
            orderStatus.setOrder(order);
            if (executionReport.isSetField(TransactTime.FIELD)) {

                orderStatus.setExtDateTime(executionReport.getTransactTime().getValue());
            }
            if (executionReport.isSetField(Text.FIELD)) {

                orderStatus.setReason(executionReport.getText().getValue());
            }

            this.serverEngine.sendEvent(orderStatus);

            return;
        }

        OrderStatus orderStatus = createStatus(executionReport, order);
        orderStatus.setOrder(order);

        this.serverEngine.sendEvent(orderStatus);

        Fill fill = createFill(executionReport, order);
        if (fill != null) {

            // associate the fill with the order
            fill.setOrder(order);

            this.serverEngine.sendEvent(fill);
        }
    }

    public void onMessage(final OrderCancelReject reject, final SessionID sessionID) throws FieldNotFound {

        if (LOGGER.isEnabledFor(Level.WARN)) {

            StringBuilder buf = new StringBuilder();
            buf.append("Order cancel/replace has been rejected");
            String clOrdID = reject.getClOrdID().getValue();
            buf.append(" [order ID: ").append(clOrdID).append("]");
            String origClOrdID = reject.getOrigClOrdID().getValue();
            buf.append(" [original order ID: ").append(origClOrdID).append("]");
            if (reject.isSetField(Text.FIELD)) {
                String text = reject.getText().getValue();
                buf.append(": ").append(text);
            }

            // warning only if order was already filled
            OrdStatus ordStatus = reject.getOrdStatus();
            if (ordStatus.getValue() == OrdStatus.FILLED) {
                LOGGER.warn(buf.toString());
            } else {
                LOGGER.error(buf.toString());
            }
        }

        String intId = reject.getClOrdID().getValue();

        // get the order from the OpenOrderWindow
        Order order = this.lookupService.getOpenOrderByRootIntId(intId);
        if (order == null) {

            if (LOGGER.isEnabledFor(Level.ERROR)) {

                LOGGER.error("Order with int ID " + intId + " matching the execution report could not be found");
            }
            return;
        }

        OrderStatus orderStatus = OrderStatus.Factory.newInstance();
        orderStatus.setStatus(Status.REJECTED);
        orderStatus.setIntId(intId);
        orderStatus.setSequenceNumber(reject.getHeader().getInt(MsgSeqNum.FIELD));
        orderStatus.setOrder(order);
        if (reject.isSetField(TransactTime.FIELD)) {

            orderStatus.setExtDateTime(reject.getTransactTime().getValue());
        }
        if (reject.isSetField(Text.FIELD)) {

            orderStatus.setReason(reject.getText().getValue());
        }

        this.serverEngine.sendEvent(orderStatus);
    }

}
