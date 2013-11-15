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
package ch.algotrader.adapter.fix;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.log4j.Logger;

import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.field.ClOrdID;
import quickfix.field.ExecType;
import quickfix.field.OrigClOrdID;
import quickfix.field.Text;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.OrderCancelReject;
import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.util.MyLogger;
import ch.algotrader.util.RoundUtil;

/**
 * Generic Fix44MessageHandler. Needs to be overwritten by specific broker interfaces.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class Fix44MessageHandler {

    private static Logger logger = MyLogger.getLogger(Fix44MessageHandler.class.getName());

    public Fix44MessageHandler(SessionSettings settings) {
        // do nothing
    }

    public void onMessage(final ExecutionReport executionReport, final SessionID sessionID) {

        try {

            // ignore PENDING_NEW, PENDING_CANCEL and PENDING_REPLACE
            ExecType execType = executionReport.getExecType();
            if (execType.getValue() == ExecType.PENDING_NEW
                    || execType.getValue() == ExecType.PENDING_REPLACE
                    || execType.getValue() == ExecType.PENDING_CANCEL) {
                return;
            }

            String intId = executionReport.getClOrdID().getValue();

            if (execType.getValue() == ExecType.REJECTED) {
                logger.error("order " + intId + " has been rejected, reason: " + executionReport.getText().getValue());
            }

            // get the order from the OpenOrderWindow
            Order order = ServiceLocator.instance().getLookupService().getOpenOrderByRootIntId(intId);
            if (order == null) {
                logger.error("order with intId " + intId + " could not be found for execution " + executionReport);
                return;
            }

            // get the other fields
            Status status = FixUtil.getStatus(execType, executionReport.getCumQty());
            long filledQuantity = (long) executionReport.getCumQty().getValue();
            long remainingQuantity = (long) (executionReport.getOrderQty().getValue() - executionReport.getCumQty().getValue());

            // assemble the orderStatus
            OrderStatus orderStatus = OrderStatus.Factory.newInstance();
            orderStatus.setStatus(status);
            orderStatus.setFilledQuantity(filledQuantity);
            orderStatus.setRemainingQuantity(remainingQuantity);
            orderStatus.setOrder(order);

            // update intId in case it has changed
            if (!order.getIntId().equals(intId)) {
                orderStatus.setIntId(intId);
            }

            processOrderStatus(executionReport, order, orderStatus);

            EngineLocator.instance().getBaseEngine().sendEvent(orderStatus);

            // only create fills if status is PARTIALLY_FILLED or FILLED
            if (execType.getValue() == ExecType.PARTIAL_FILL || execType.getValue() == ExecType.FILL) {

                // get the fields
                Date extDateTime = executionReport.getTransactTime().getValue();
                Side side = FixUtil.getSide(executionReport.getSide());
                long quantity = (long) executionReport.getLastQty().getValue();
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

                processFill(executionReport, order, fill);

                // associate the fill with the order
                order.addFills(fill);

                EngineLocator.instance().getBaseEngine().sendEvent(fill);
            }
        } catch (FieldNotFound e) {
            logger.error(e);
        }
    }

    public void onMessage(final OrderCancelReject orderCancelReject, final SessionID sessionID)  {

        try {
            Text text = orderCancelReject.getText();
            ClOrdID clOrdID = orderCancelReject.getClOrdID();
            OrigClOrdID origClOrdID = orderCancelReject.getOrigClOrdID();
            logger.error("order cancel/replace has been rejected, clOrdID: " + clOrdID.getValue() + " origOrdID: " + origClOrdID.getValue() + " reason: " + text.getValue());
        } catch (FieldNotFound e) {
            logger.error(e);
        }
    }

    protected void processOrderStatus(ExecutionReport executionReport, Order order, OrderStatus orderStatus) throws FieldNotFound {
        // do nothing (can be overwritten by subclasses)
    }

    protected void processFill(ExecutionReport executionReport, Order order, Fill fill) throws FieldNotFound {
        // do nothing (can be overwritten by subclasses)
    }
}
