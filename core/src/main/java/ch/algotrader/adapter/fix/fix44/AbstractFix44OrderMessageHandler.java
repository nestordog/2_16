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
package ch.algotrader.adapter.fix.fix44;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.enumeration.Status;
import ch.algotrader.esper.Engine;
import ch.algotrader.service.OrderExecutionService;
import ch.algotrader.service.TransactionService;
import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.field.ExecType;
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
 */
public abstract class AbstractFix44OrderMessageHandler extends AbstractFix44MessageHandler {

    private static final Logger LOGGER = LogManager.getLogger(AbstractFix44OrderMessageHandler.class);

    private final OrderExecutionService orderExecutionService;
    private final TransactionService transactionService;
    private final Engine serverEngine;

    protected AbstractFix44OrderMessageHandler(final OrderExecutionService orderExecutionService, final TransactionService transactionService, final Engine serverEngine) {
        this.orderExecutionService = orderExecutionService;
        this.transactionService = transactionService;
        this.serverEngine = serverEngine;
    }

    protected abstract boolean discardReport(ExecutionReport executionReport) throws FieldNotFound;

    protected abstract void handleExternal(ExecutionReport executionReport) throws FieldNotFound;

    protected abstract void handleUnknown(ExecutionReport executionReport) throws FieldNotFound;

    protected abstract void handleRestated(ExecutionReport executionReport, Order order) throws FieldNotFound;

    protected abstract boolean isOrderRejected(ExecutionReport executionReport) throws FieldNotFound;

    protected abstract boolean isOrderReplaced(ExecutionReport executionReport) throws FieldNotFound;

    protected abstract boolean isOrderRestated(ExecutionReport executionReport) throws FieldNotFound;

    protected abstract OrderStatus createStatus(ExecutionReport executionReport, Order order) throws FieldNotFound;

    protected abstract Fill createFill(ExecutionReport executionReport, Order order) throws FieldNotFound;

    protected abstract String getDefaultBroker();

    public void onMessage(final ExecutionReport executionReport, final SessionID sessionID) throws FieldNotFound {

        if (discardReport(executionReport)) {

            return;
        }

        String orderIntId;
        ExecType execType = executionReport.getExecType();
        if (execType.getValue() == ExecType.CANCELED) {
            if (executionReport.isSetOrigClOrdID()) {
                orderIntId = executionReport.getOrigClOrdID().getValue();
            } else {
                String orderExtId = executionReport.getOrderID().getValue();
                orderIntId = this.orderExecutionService.lookupIntId(orderExtId);
            }
        } else {
            if (executionReport.isSetClOrdID()) {
                orderIntId = executionReport.getClOrdID().getValue();
            } else {
                String orderExtId = executionReport.getOrderID().getValue();
                orderIntId = this.orderExecutionService.lookupIntId(orderExtId);
            }
        }
        if (orderIntId == null) {

            handleUnknown(executionReport);
            return;
        }

        Order order = this.orderExecutionService.getOpenOrderByIntId(orderIntId);
        if (order == null) {

            handleUnknown(executionReport);
            return;
        }

        if (isOrderRejected(executionReport)) {

            String statusText = getStatusText(executionReport);

            if (LOGGER.isErrorEnabled ()) {

                String intId = executionReport.getClOrdID().getValue();
                StringBuilder buf = new StringBuilder();
                buf.append("Order with IntID ").append(intId).append(" has been rejected");
                if (statusText != null) {

                    buf.append("; reason given: ").append(statusText);
                }
                LOGGER.error(buf.toString());
            }

            OrderStatus orderStatus = OrderStatus.Factory.newInstance();
            orderStatus.setStatus(Status.REJECTED);
            orderStatus.setIntId(orderIntId);
            orderStatus.setSequenceNumber(executionReport.getHeader().getInt(MsgSeqNum.FIELD));
            orderStatus.setOrder(order);
            if (executionReport.isSetField(TransactTime.FIELD)) {

                orderStatus.setExtDateTime(executionReport.getTransactTime().getValue());
            }
            if (statusText != null) {

                orderStatus.setReason(statusText);
            }

            this.serverEngine.sendEvent(orderStatus);
            this.orderExecutionService.handleOrderStatus(orderStatus);

            return;
        }

        if (isOrderRestated(executionReport)) {

            handleRestated(executionReport, order);
            return;
        }

        if (isOrderReplaced(executionReport)) {

            // Send status report for replaced order
            String oldIntId = executionReport.getOrigClOrdID().getValue();
            Order oldOrder = this.orderExecutionService.getOpenOrderByIntId(oldIntId);

            if (oldOrder != null) {

                OrderStatus orderStatus = createStatus(executionReport, oldOrder);
                orderStatus.setStatus(Status.CANCELED);
                orderStatus.setExtId(null);
                this.serverEngine.sendEvent(orderStatus);
                this.orderExecutionService.handleOrderStatus(orderStatus);
            }
        }

        OrderStatus orderStatus = createStatus(executionReport, order);

        this.serverEngine.sendEvent(orderStatus);
        this.orderExecutionService.handleOrderStatus(orderStatus);

        Fill fill = createFill(executionReport, order);
        if (fill != null) {

            this.serverEngine.sendEvent(fill);
            this.transactionService.createTransaction(fill);
            this.orderExecutionService.handleFill(fill);
        }
    }

    /**
     * This method can be overridden to provide a custom translation of status (error) codes
     * to human readable status (error) messages.
     */
    protected String getStatusText(final ExecutionReport executionReport) throws FieldNotFound {
        if (executionReport.isSetText()) {

            return executionReport.getText().getValue();
        } else {

            return null;
        }
    }

    public void onMessage(final OrderCancelReject reject, final SessionID sessionID) throws FieldNotFound {

        if (LOGGER.isWarnEnabled()) {

            StringBuilder buf = new StringBuilder();
            buf.append("Order cancel/replace has been rejected");
            String clOrdID = reject.isSetClOrdID() ? reject.getClOrdID().getValue() : null;
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

        if (reject.isSetClOrdID()) {
            String orderIntId = reject.getClOrdID().getValue();

            Order order = this.orderExecutionService.getOpenOrderByIntId(orderIntId);
            if (order != null) {

                OrderStatus orderStatus = OrderStatus.Factory.newInstance();
                orderStatus.setStatus(Status.REJECTED);
                orderStatus.setIntId(orderIntId);
                orderStatus.setSequenceNumber(reject.getHeader().getInt(MsgSeqNum.FIELD));
                orderStatus.setOrder(order);
                if (reject.isSetField(TransactTime.FIELD)) {

                    orderStatus.setExtDateTime(reject.getTransactTime().getValue());
                }
                if (reject.isSetField(Text.FIELD)) {

                    orderStatus.setReason(reject.getText().getValue());
                }

                this.serverEngine.sendEvent(orderStatus);
                this.orderExecutionService.handleOrderStatus(orderStatus);
            }
        }

    }

}
