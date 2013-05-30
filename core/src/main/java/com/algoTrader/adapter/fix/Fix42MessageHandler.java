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
package com.algoTrader.adapter.fix;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.log4j.Logger;

import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.field.ExecTransType;
import quickfix.field.ExecType;
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.OrderCancelReject;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.strategy.StrategyImpl;
import com.algoTrader.entity.trade.Fill;
import com.algoTrader.entity.trade.Order;
import com.algoTrader.entity.trade.OrderStatus;
import com.algoTrader.enumeration.Side;
import com.algoTrader.enumeration.Status;
import com.algoTrader.esper.EsperManager;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;

/**
 * Generic Fix42MessageHandler. Needs to be overwritten by specific broker interfaces.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class Fix42MessageHandler {

    private static Logger logger = MyLogger.getLogger(Fix42MessageHandler.class.getName());

    public Fix42MessageHandler(SessionSettings settings) {
        // do nothing
    }

    public void onMessage(ExecutionReport executionReport, SessionID sessionID) {

        try {

            // ignore PENDING_NEW, PENDING_CANCEL and PENDING_REPLACE
            if (executionReport.getExecType().getValue() == ExecType.PENDING_NEW || executionReport.getExecType().getValue() == ExecType.PENDING_REPLACE
                    || executionReport.getExecType().getValue() == ExecType.PENDING_CANCEL) {
                return;
            }

            String intId = executionReport.getClOrdID().getValue();

            // check ExecTransType
            if (executionReport.isSetExecTransType() && executionReport.getExecTransType().getValue() != ExecTransType.NEW) {
                throw new UnsupportedOperationException("order " + intId + " has received an ussupported ExecTransType of: " + executionReport.getExecTransType().getValue());
            }

            if (executionReport.getExecType().getValue() == ExecType.REJECTED) {
                logger.error("order " + intId + " has been rejected, reason: " + executionReport.getText().getValue());
            }

            // get the order from the OpenOrderWindow
            Order order = ServiceLocator.instance().getLookupService().getOpenOrderByRootIntId(intId);
            if (order == null) {
                logger.error("order with intId " + intId + " could not be found for execution " + executionReport);
                return;
            }

            // get the other fields
            Status status = FixUtil.getStatus(executionReport.getExecType(), executionReport.getCumQty());
            long filledQuantity = (long) executionReport.getCumQty().getValue();
            long remainingQuantity = (long) (executionReport.getOrderQty().getValue() - executionReport.getCumQty().getValue());

            // assemble the orderStatus
            OrderStatus orderStatus = OrderStatus.Factory.newInstance();
            orderStatus.setStatus(status);
            orderStatus.setFilledQuantity(filledQuantity);
            orderStatus.setRemainingQuantity(remainingQuantity);
            orderStatus.setOrd(order);

            // update intId in case it has changed
            if (!order.getIntId().equals(intId)) {
                orderStatus.setIntId(intId);
            }

            EsperManager.sendEvent(StrategyImpl.BASE, orderStatus);

            // only create fills if status is PARTIALLY_FILLED or FILLED
            if (executionReport.getExecType().getValue() == ExecType.PARTIAL_FILL || executionReport.getExecType().getValue() == ExecType.FILL) {

                // get the fields
                Date extDateTime = executionReport.getTransactTime().getValue();
                Side side = FixUtil.getSide(executionReport.getSide());
                long quantity = (long) executionReport.getLastShares().getValue();
                BigDecimal price = RoundUtil.getBigDecimal(executionReport.getLastPx().getValue(), order.getSecurity().getSecurityFamily().getScale());
                String extId = executionReport.getExecID().getValue();

                // assemble the fill
                Fill fill = Fill.Factory.newInstance();
                fill.setDateTime(new Date());
                fill.setExtDateTime(extDateTime);
                fill.setSide(side);
                fill.setQuantity(quantity);
                fill.setPrice(price);
                fill.setExtId(extId);

                // associate the fill with the order
                order.addFills(fill);

                EsperManager.sendEvent(StrategyImpl.BASE, fill);
            }
        } catch (FieldNotFound e) {
            logger.error(e);
        }
    }

    public void onMessage(OrderCancelReject orderCancelReject, SessionID sessionID)  {

        try {
            if ("Too late to cancel".equals(orderCancelReject.getText().getValue()) || "Cannot cancel the filled order".equals(orderCancelReject.getText().getValue())) {
                logger.info("cannot cancel, order has already been executed, clOrdID: " + orderCancelReject.getClOrdID().getValue() +
                        " origOrdID: " + orderCancelReject.getOrigClOrdID().getValue());
            } else {
                logger.error("order cancel/replace has been rejected, clOrdID: " + orderCancelReject.getClOrdID().getValue() +
                        " origOrdID: " + orderCancelReject.getOrigClOrdID().getValue() +
                        " reason: " + orderCancelReject.getText().getValue());
            }
        } catch (FieldNotFound e) {
            logger.error(e);
        }
    }
}
