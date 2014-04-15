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
package ch.algotrader.adapter.dc;

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
import quickfix.field.OrdStatus;
import quickfix.fix44.ExecutionReport;

/**
 * DukasCopy specific FIX order message handler.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class DCFixOrderMessageHandler extends AbstractFix44OrderMessageHandler {

    @Override
    protected boolean discardReport(final ExecutionReport executionReport) throws FieldNotFound {

        return false;
    }

    @Override
    protected boolean isOrderRejected(final ExecutionReport executionReport) throws FieldNotFound {

        OrdStatus ordStatus = executionReport.getOrdStatus();
        return ordStatus.getValue() == OrdStatus.REJECTED;
    }

    @Override
    protected OrderStatus createStatus(final ExecutionReport executionReport, final Order order) throws FieldNotFound {

        // get the other fields
        Status status = getStatus(executionReport.getOrdStatus(), executionReport.getCumQty());
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
        if (!order.getIntId().equals(intId)) {
            orderStatus.setIntId(intId);
        }

        // Note: store OrderID sind DukasCopy requires it for cancels and replaces
        if (order.getExtId() == null) {
            orderStatus.setExtId(executionReport.getOrderID().getValue());
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
            BigDecimal price = RoundUtil.getBigDecimal(executionReport.getAvgPx().getValue(), order.getSecurity().getSecurityFamily().getScale());
            String extId = executionReport.getExecID().getValue();

            // assemble the fill
            Fill fill = Fill.Factory.newInstance();
            fill.setDateTime(new Date());
            fill.setExtDateTime(extDateTime);
            fill.setSide(side);
            fill.setQuantity(quantity);
            fill.setPrice(price);
            fill.setExtId(extId);

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
        } else if (ordStatus.getValue() == OrdStatus.CANCELED || ordStatus.getValue() == OrdStatus.REJECTED) {
            return Status.CANCELED;
        } else {
            throw new IllegalArgumentException("unknown orderStatus " + ordStatus.getValue());
        }
    }

}
