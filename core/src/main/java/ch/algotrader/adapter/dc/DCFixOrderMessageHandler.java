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
package ch.algotrader.adapter.dc;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.adapter.fix.FixUtil;
import ch.algotrader.adapter.fix.fix44.AbstractFix44OrderMessageHandler;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.esper.Engine;
import ch.algotrader.service.OrderExecutionService;
import ch.algotrader.service.TransactionService;
import ch.algotrader.util.PriceUtil;
import quickfix.FieldNotFound;
import quickfix.field.AvgPx;
import quickfix.field.CumQty;
import quickfix.field.ExecType;
import quickfix.field.MsgSeqNum;
import quickfix.field.OrdStatus;
import quickfix.field.TransactTime;
import quickfix.fix44.ExecutionReport;

/**
 * DukasCopy specific FIX order message handler.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class DCFixOrderMessageHandler extends AbstractFix44OrderMessageHandler {

    private static final Logger LOGGER = LogManager.getLogger(DCFixOrderMessageHandler.class);

    public DCFixOrderMessageHandler(final OrderExecutionService orderExecutionService, final TransactionService transactionService, final Engine serverEngine) {
        super(orderExecutionService, transactionService, serverEngine);
    }

    @Override
    protected boolean discardReport(final ExecutionReport executionReport) throws FieldNotFound {

        return false;
    }

    @Override
    protected void handleExternal(final ExecutionReport executionReport) throws FieldNotFound {
    }

    @Override
    protected void handleUnknown(final ExecutionReport executionReport) throws FieldNotFound {

        if (LOGGER.isErrorEnabled() && executionReport.isSetClOrdID()) {
            String orderIntId = executionReport.getClOrdID().getValue();
            LOGGER.error("Cannot find open order with IntID {}", orderIntId);
        }
    }

    @Override
    protected boolean isOrderRejected(final ExecutionReport executionReport) throws FieldNotFound {

        OrdStatus ordStatus = executionReport.getOrdStatus();
        return ordStatus.getValue() == OrdStatus.REJECTED;
    }

    @Override
    protected boolean isOrderReplaced(ExecutionReport executionReport) throws FieldNotFound {

        ExecType execType = executionReport.getExecType();
        return execType.getValue() == ExecType.REPLACE;
    }

    @Override
    protected OrderStatus createStatus(final ExecutionReport executionReport, final Order order) throws FieldNotFound {

        // get the other fields
        Status status = getStatus(executionReport.getOrdStatus(), executionReport.getCumQty());
        long filledQuantity = (long) executionReport.getCumQty().getValue();
        long remainingQuantity = (long) (executionReport.getOrderQty().getValue() - executionReport.getCumQty().getValue());
        long lastQuantity = executionReport.isSetLastQty() ? (long) executionReport.getLastQty().getValue() : 0L;

        // Note: store OrderID since DukasCopy requires it for cancels and replaces
        String intId = order.getIntId() != null ? order.getIntId(): executionReport.getClOrdID().getValue();
        String extId = executionReport.getOrderID().getValue();

        // assemble the orderStatus
        OrderStatus orderStatus = OrderStatus.Factory.newInstance();
        orderStatus.setStatus(status);
        orderStatus.setExtId(extId);
        orderStatus.setIntId(intId);
        orderStatus.setSequenceNumber(executionReport.getHeader().getInt(MsgSeqNum.FIELD));
        orderStatus.setFilledQuantity(filledQuantity);
        orderStatus.setRemainingQuantity(remainingQuantity);
        orderStatus.setLastQuantity(lastQuantity);
        orderStatus.setOrder(order);
        if (executionReport.isSetField(TransactTime.FIELD)) {

            orderStatus.setExtDateTime(executionReport.getTransactTime().getValue());
        }
        if (executionReport.isSetField(AvgPx.FIELD)) {

            double avgPrice = executionReport.getAvgPx().getValue();
            if (avgPrice != 0.0) {
                orderStatus.setAvgPrice(PriceUtil.normalizePrice(order, avgPrice));
            }
        }

        return orderStatus;
    }

    @Override
    protected Fill createFill(final ExecutionReport executionReport, final Order order) throws FieldNotFound {

        // only create fills if status is FILLED (Note: DukasCopy does nut use PARTIALLY_FILLED)
        if (executionReport.getOrdStatus().getValue() == OrdStatus.FILLED) {

            // get the fields
            Date extDateTime = executionReport.getTransactTime().getValue();
            Side side = FixUtil.getSide(executionReport.getSide());
            long quantity = (long) executionReport.getCumQty().getValue();

            // Note: DukasCopy does not use LastPx it only uses AvgPx
            BigDecimal price = PriceUtil.normalizePrice(order, executionReport.getAvgPx().getValue());
            String extId = executionReport.getOrderID().getValue();

            // assemble the fill
            Fill fill = new Fill();
            fill.setDateTime(new Date());
            fill.setExtDateTime(extDateTime);
            fill.setExtId(extId);
            fill.setSequenceNumber(executionReport.getHeader().getInt(MsgSeqNum.FIELD));
            fill.setSide(side);
            fill.setQuantity(quantity);
            fill.setPrice(price);
            fill.setOrder(order);

            return fill;
        } else {

            return null;
        }
    }

    private static Status getStatus(OrdStatus ordStatus, CumQty cumQty) {

        // Note: DukasCopy uses CALCULATED instead of NEW
        if (ordStatus.getValue() == OrdStatus.CALCULATED || ordStatus.getValue() == OrdStatus.PENDING_NEW) {
            if (cumQty.getValue() == 0) {
                return Status.SUBMITTED;
            } else {
                return Status.PARTIALLY_EXECUTED;
            }
        } else if (ordStatus.getValue() == OrdStatus.FILLED) {
            return Status.EXECUTED;
        } else if (ordStatus.getValue() == OrdStatus.CANCELED) {
            return Status.CANCELED;
        } else if (ordStatus.getValue() == OrdStatus.REJECTED) {
            return Status.REJECTED;
        } else {
            throw new IllegalArgumentException("unknown orderStatus " + ordStatus.getValue());
        }
    }

}
