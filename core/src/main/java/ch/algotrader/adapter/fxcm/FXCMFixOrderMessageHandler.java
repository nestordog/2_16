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
package ch.algotrader.adapter.fxcm;

import java.math.BigDecimal;
import java.util.Date;

import ch.algotrader.adapter.fix.FixUtil;
import ch.algotrader.adapter.fix.fix44.AbstractFix44OrderMessageHandler;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.util.RoundUtil;
import quickfix.FieldNotFound;
import quickfix.field.CumQty;
import quickfix.field.ExecType;
import quickfix.field.OrdStatus;
import quickfix.fix44.ExecutionReport;

/**
 * FXCM specific FIX order message handler.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class FXCMFixOrderMessageHandler extends AbstractFix44OrderMessageHandler {

    @Override
    protected boolean discardReport(final ExecutionReport executionReport) throws FieldNotFound {

        // ignore PENDING_NEW, PENDING_CANCEL and PENDING_REPLACE
        ExecType execType = executionReport.getExecType();

        if (execType.getValue() == ExecType.PENDING_NEW
                || execType.getValue() == ExecType.PENDING_REPLACE
                || execType.getValue() == ExecType.PENDING_CANCEL) {

            return true;
        } else {

            return false;
        }
    }

    @Override
    protected boolean isOrderRejected(final ExecutionReport executionReport) throws FieldNotFound {

        OrdStatus ordStatus = executionReport.getOrdStatus();
        return ordStatus.getValue() == OrdStatus.REJECTED;
    }

    @Override
    protected OrderStatus createStatus(final ExecutionReport executionReport, final Order order) throws FieldNotFound {

        // get the other fields
        Status status = getStatus(executionReport.getOrdStatus(), executionReport.getExecType(), executionReport.getCumQty());
        long filledQuantity = (long) executionReport.getCumQty().getValue();
        long remainingQuantity = (long) (executionReport.getOrderQty().getValue() - executionReport.getCumQty().getValue());

        // assemble the orderStatus
        OrderStatus orderStatus = OrderStatus.Factory.newInstance();
        orderStatus.setStatus(status);
        orderStatus.setFilledQuantity(filledQuantity);
        orderStatus.setRemainingQuantity(remainingQuantity);
        orderStatus.setOrder(order);

        String intId = executionReport.getClOrdID().getValue();
        // update intId in case it has changed
        if (!intId.equals(order.getIntId())) {
            orderStatus.setIntId(intId);
        }

        String extId = executionReport.getOrderID().getValue();
        orderStatus.setExtId(extId);

        return orderStatus;
    }

    @Override
    protected Fill createFill(final ExecutionReport executionReport, final Order order) throws FieldNotFound {

        // only create fills if status is FILLED (Note: FXCM advises to use STOPPED as the order execution confirmation)
        OrdStatus ordStatus = executionReport.getOrdStatus();
        CumQty qty = executionReport.getCumQty();
        if (ordStatus.getValue() == OrdStatus.PARTIALLY_FILLED || (ordStatus.getValue() == OrdStatus.STOPPED && (long) qty.getValue() != 0L)) {

            // get the fields
            Date extDateTime = executionReport.getTransactTime().getValue();
            Side side = FixUtil.getSide(executionReport.getSide());
            long quantity = (long) executionReport.getCumQty().getValue();

            BigDecimal price = RoundUtil.getBigDecimal(executionReport.getAvgPx().getValue(), order.getSecurity().getSecurityFamily().getScale());
            String extId = executionReport.getOrderID().getValue();

            // assemble the fill
            // please note FXCM does not provide a unique exec report attribute
            // that could be used as a unique extId
            Fill fill = Fill.Factory.newInstance();
            fill.setDateTime(new Date());
            fill.setExtDateTime(extDateTime);
            fill.setSide(side);
            fill.setQuantity(quantity);
            fill.setPrice(price);

            return fill;
        } else {

            return null;
        }
    }

    private static Status getStatus(OrdStatus ordStatus, ExecType execType, CumQty cumQty) {

        if (ordStatus.getValue() == OrdStatus.NEW || ordStatus.getValue() == OrdStatus.PENDING_NEW || ordStatus.getValue() == OrdStatus.REPLACED) {
            if (cumQty.getValue() == 0) {
                return Status.SUBMITTED;
            } else {
                return Status.PARTIALLY_EXECUTED;
            }
        } else if (ordStatus.getValue() == OrdStatus.FILLED) {
            return Status.EXECUTED;
        } else if (ordStatus.getValue() == OrdStatus.STOPPED) {
            if (execType.getValue() == ExecType.TRADE) {
                return Status.EXECUTED;
            } else {
                return Status.PARTIALLY_EXECUTED;
            }
        } else if (ordStatus.getValue() == OrdStatus.CANCELED || ordStatus.getValue() == OrdStatus.REJECTED) {
            return Status.CANCELED;
        } else {
            throw new IllegalArgumentException("unknown orderStatus " + ordStatus.getValue());
        }
    }

}
