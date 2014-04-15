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
package ch.algotrader.adapter.lmax;

import java.util.Date;

import quickfix.FieldNotFound;
import quickfix.field.CumQty;
import quickfix.field.ExecType;
import quickfix.field.OrderQty;
import quickfix.fix44.ExecutionReport;
import ch.algotrader.adapter.fix.FixUtil;
import ch.algotrader.adapter.fix.fix44.AbstractFix44OrderMessageHandler;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.util.RoundUtil;

/**
 * LMFX specific Fix44MessageHandler.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class LMAXFixOrderMessageHandler extends AbstractFix44OrderMessageHandler {

    private static final double MULTPLIER = 10000.0;

    @Override
    protected boolean discardReport(final ExecutionReport executionReport) throws FieldNotFound {

        // ignore PENDING_NEW, PENDING_CANCEL and PENDING_REPLACE
        ExecType execType = executionReport.getExecType();

        if (execType.getValue() == ExecType.PENDING_NEW || execType.getValue() == ExecType.PENDING_REPLACE || execType.getValue() == ExecType.PENDING_CANCEL) {

            return true;
        } else {

            return false;
        }
    }

    @Override
    protected boolean isOrderRejected(final ExecutionReport executionReport) throws FieldNotFound {

        ExecType execType = executionReport.getExecType();
        return execType.getValue() == ExecType.REJECTED;
    }

    @Override
    protected OrderStatus createStatus(final ExecutionReport executionReport, final Order order) throws FieldNotFound {

        ExecType execType = executionReport.getExecType();
        Status status = getStatus(execType, executionReport.getOrderQty(), executionReport.getCumQty());
        long filledQuantity = Math.round(executionReport.getCumQty().getValue() * MULTPLIER);
        long remainingQuantity = Math.round((executionReport.getOrderQty().getValue() - executionReport.getCumQty().getValue()) * MULTPLIER);
        String extId = executionReport.getExecID().getValue();

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

        orderStatus.setExtId(extId);

        return orderStatus;
    }

    @Override
    protected Fill createFill(ExecutionReport executionReport, Order order) throws FieldNotFound {

        ExecType execType = executionReport.getExecType();
        // only create fills if status is TRADE
        if (execType.getValue() == ExecType.TRADE) {

            // get the fields
            Date extDateTime = executionReport.getTransactTime().getValue();
            Side side = FixUtil.getSide(executionReport.getSide());
            long quantity = Math.round(executionReport.getLastQty().getValue() * MULTPLIER);
            double price = executionReport.getLastPx().getValue();
            String extId = executionReport.getExecID().getValue();

            // assemble the fill
            Fill fill = Fill.Factory.newInstance();
            fill.setDateTime(new Date());
            fill.setExtDateTime(extDateTime);
            fill.setSide(side);
            fill.setQuantity(quantity);
            fill.setPrice(RoundUtil.getBigDecimal(price, order.getSecurity().getSecurityFamily().getScale()));
            fill.setExtId(extId);

            return fill;
        } else {

            return null;
        }
    }

    private static Status getStatus(ExecType execType, OrderQty orderQty, CumQty cumQty) {

        if (execType.getValue() == ExecType.NEW) {
            return Status.SUBMITTED;
        } else if (execType.getValue() == ExecType.TRADE) {
            if (cumQty.getValue() == orderQty.getValue()) {
                return Status.EXECUTED;
            } else {
                return Status.PARTIALLY_EXECUTED;
            }
        } else if (execType.getValue() == ExecType.CANCELED || execType.getValue() == ExecType.REJECTED || execType.getValue() == ExecType.DONE_FOR_DAY || execType.getValue() == ExecType.EXPIRED) {
            return Status.CANCELED;
        } else if (execType.getValue() == ExecType.REPLACE) {
            if (cumQty.getValue() == 0) {
                return Status.SUBMITTED;
            } else {
                return Status.PARTIALLY_EXECUTED;
            }
        } else {
            throw new IllegalArgumentException("unknown execType " + execType.getValue());
        }
    }

}
