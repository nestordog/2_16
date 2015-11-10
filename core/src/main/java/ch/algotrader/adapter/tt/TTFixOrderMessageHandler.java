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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.adapter.fix.DropCopyAllocationVO;
import ch.algotrader.adapter.fix.DropCopyAllocator;
import ch.algotrader.adapter.fix.fix42.GenericFix42OrderMessageHandler;
import ch.algotrader.entity.trade.ExternalFill;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.enumeration.Status;
import ch.algotrader.esper.Engine;
import ch.algotrader.ordermgmt.OrderRegistry;
import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.field.ExecTransType;
import quickfix.field.ExecType;
import quickfix.field.MsgSeqNum;
import quickfix.field.MsgType;
import quickfix.field.SendingTime;
import quickfix.fix42.BusinessMessageReject;
import quickfix.fix42.ExecutionReport;

/**
 * Trading Technology specific FIX/4.2 order message handler.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class TTFixOrderMessageHandler extends GenericFix42OrderMessageHandler {

    private static final Logger LOGGER = LogManager.getLogger(TTFixOrderMessageHandler.class);

    private final OrderRegistry orderRegistry;
    private final Engine serverEngine;
    private final DropCopyAllocator dropCopyAllocator;

    public TTFixOrderMessageHandler(final OrderRegistry orderRegistry, final Engine serverEngine, final DropCopyAllocator dropCopyAllocator) {
        super(orderRegistry, serverEngine);
        this.orderRegistry = orderRegistry;
        this.serverEngine = serverEngine;
        this.dropCopyAllocator = dropCopyAllocator;
    }

    @Override
    protected boolean discardReport(final ExecutionReport executionReport) throws FieldNotFound {

        if (executionReport.isSetExecTransType()) {
            ExecTransType execTransType = executionReport.getExecTransType();
            if (execTransType.getValue() == ExecTransType.STATUS) {
                if (LOGGER.isInfoEnabled()) {
                    String orderIntId = executionReport.getClOrdID().getValue();
                    LOGGER.info("Working order at the TT side: {}", orderIntId);
                }
                return true;
            }
        }
        return super.discardReport(executionReport);
    }

    @Override
    protected void handleExternal(final ExecutionReport executionReport) throws FieldNotFound {

        if (this.dropCopyAllocator == null) {
            super.handleExternal(executionReport);
        } else {
            handleExternalReport(executionReport);
        }
    }

    @Override
    protected void handleUnknown(final ExecutionReport executionReport) throws FieldNotFound {

        if (this.dropCopyAllocator == null) {
            super.handleUnknown(executionReport);
        } else {
            handleExternalReport(executionReport);
        }
    }

    private void handleExternalReport(final ExecutionReport executionReport) throws FieldNotFound {

        String extId = executionReport.getOrderID().getValue();
        Status status = getStatus(executionReport.getExecType().getValue());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Received order status {} for external order {}", status, extId);
        }

        if (status != Status.PARTIALLY_EXECUTED && status != Status.EXECUTED) {
            return;
        }

        if (executionReport.isSetExecTransType() && executionReport.getExecTransType().getValue() != ExecTransType.NEW) {
            return;
        }

        String ttid = executionReport.getSecurityID().getValue();
        String extAccount = executionReport.getAccount().getValue();

        DropCopyAllocationVO allocation = this.dropCopyAllocator.allocate(ttid, extAccount);
        if (allocation == null) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Unable to allocate an external (drop-copy) fill: " +
                        "extID = {}, TT security id = {}, external account = {}", extId, ttid, extAccount);
            }
            return;
        }

        ExternalFill fill = createFill(executionReport, allocation);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(fill);
        }

        if (fill != null) {
            this.serverEngine.sendEvent(fill);
        }
    }

    @Override
    public void onMessage(final BusinessMessageReject reject, final SessionID sessionID) throws FieldNotFound {

        String refMsgType = reject.getRefMsgType().getValue();
        if (refMsgType.equalsIgnoreCase(MsgType.ORDER_SINGLE) && reject.isSetBusinessRejectRefID()) {
            String intId = reject.getBusinessRejectRefID().getValue();
            Order order = this.orderRegistry.getOpenOrderByIntId(intId);
            if (order != null) {
                if (LOGGER.isErrorEnabled()) {

                    StringBuilder buf = new StringBuilder();
                    buf.append("Order with IntID ").append(intId).append(" has been rejected");
                    if (reject.isSetText()) {

                        buf.append("; reason given: ").append(reject.getText().getValue());
                    }
                    LOGGER.error(buf.toString());
                }

                OrderStatus orderStatus = OrderStatus.Factory.newInstance();
                orderStatus.setStatus(Status.REJECTED);
                orderStatus.setIntId(intId);
                orderStatus.setSequenceNumber(reject.getHeader().getInt(MsgSeqNum.FIELD));
                orderStatus.setOrder(order);
                orderStatus.setExtDateTime(reject.getHeader().getUtcTimeStamp(SendingTime.FIELD));
                if (reject.isSetBusinessRejectReason()) {

                    orderStatus.setReason(reject.getText().getValue());
                }

                this.serverEngine.sendEvent(orderStatus);
                return;
            }
        }
        super.onMessage(reject, sessionID);
    }

    private Status getStatus(final char execType) {

        switch (execType) {
            case ExecType.NEW:
                return Status.SUBMITTED;
            case ExecType.PARTIAL_FILL:
                return Status.PARTIALLY_EXECUTED;
            case ExecType.FILL:
                return Status.EXECUTED;
            case ExecType.CANCELED:
            case ExecType.DONE_FOR_DAY:
            case ExecType.EXPIRED:
                return Status.CANCELED;
            case ExecType.REJECTED:
                return Status.REJECTED;
            case ExecType.REPLACE:
                return Status.SUBMITTED;
            default:
                throw new IllegalArgumentException("Unexpected execType: " + execType);
        }
    }

}
