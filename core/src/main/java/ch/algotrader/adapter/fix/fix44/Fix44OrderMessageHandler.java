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
package ch.algotrader.adapter.fix.fix44;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.log4j.Logger;

import ch.algotrader.service.LookupService;
import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.field.ClOrdID;
import quickfix.field.CumQty;
import quickfix.field.ExecType;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.RefMsgType;
import quickfix.field.RefTagID;
import quickfix.field.Text;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.OrderCancelReject;
import ch.algotrader.adapter.fix.FixUtil;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.util.MyLogger;
import ch.algotrader.util.RoundUtil;
import quickfix.fix44.Reject;

/**
 * Generic Fix44OrderMessageHandler. Needs to be overwritten by specific broker interfaces.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class Fix44OrderMessageHandler {

    private static Logger logger = MyLogger.getLogger(Fix44OrderMessageHandler.class.getName());

    private LookupService lookupService;

    public Fix44OrderMessageHandler() {
    }

    public void setLookupService(final LookupService lookupService) {
        this.lookupService = lookupService;
    }

    public LookupService getLookupService() {
        return lookupService;
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
                return;
            }

            // get the order from the OpenOrderWindow
            Order order = getLookupService().getOpenOrderByRootIntId(intId);
            if (order == null) {
                logger.error("order with intId " + intId + " could not be found for execution " + executionReport);
                return;
            }

            // get the other fields
            Status status = getStatus(execType, executionReport.getOrderQty(), executionReport.getCumQty());
            long filledQuantity = (long) executionReport.getCumQty().getValue();
            long remainingQuantity = (long) (executionReport.getOrderQty().getValue() - executionReport.getCumQty().getValue());
            String extId = executionReport.getExecID().getValue();

            // assemble the orderStatus
            OrderStatus orderStatus = OrderStatus.Factory.newInstance();
            orderStatus.setStatus(status);
            orderStatus.setFilledQuantity(filledQuantity);
            orderStatus.setRemainingQuantity(remainingQuantity);
            orderStatus.setOrder(order);
            orderStatus.setExtId(extId);

            // update intId in case it has changed
            if (!intId.equals(order.getIntId())) {
                orderStatus.setIntId(intId);
            }

            processOrderStatus(executionReport, order, orderStatus);

            EngineLocator.instance().getBaseEngine().sendEvent(orderStatus);

            // only create fills if status is TRADE
            if (execType.getValue() == ExecType.TRADE) {

                // get the fields
                Date extDateTime = executionReport.getTransactTime().getValue();
                Side side = FixUtil.getSide(executionReport.getSide());
                long quantity = (long) executionReport.getLastQty().getValue();
                double price = executionReport.getLastPx().getValue();

                // assemble the fill
                Fill fill = Fill.Factory.newInstance();
                fill.setDateTime(new Date());
                fill.setExtDateTime(extDateTime);
                fill.setSide(side);
                fill.setQuantity(quantity);
                fill.setPrice(RoundUtil.getBigDecimal(price, order.getSecurity().getSecurityFamily().getScale()));
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

    public void onMessage(final Reject reject, final SessionID sessionID)  {

        try {
            StringBuilder buf = new StringBuilder();
            buf.append("Message rejected as invalid");
            if (reject.isSetField(RefMsgType.FIELD) && reject.isSetField(RefTagID.FIELD)) {
                String msgType = reject.getRefMsgType().getValue();
                int tagId = reject.getRefTagID().getValue();
                buf.append(" [message type: ").append(msgType).append("; tag id: ").append(tagId).append("]");
            }
            if (reject.isSetField(Text.FIELD)) {
                String text = reject.getText().getValue();
                buf.append(": ").append(text);
            }
            logger.error(buf.toString());
        } catch (FieldNotFound e) {
            logger.error(e);
        }
    }

    /**
     * process an OrderStatus based on an {@link ExecutionReport} and {@link Order} before it is propagated
     */
    protected void processOrderStatus(ExecutionReport executionReport, Order order, OrderStatus orderStatus) throws FieldNotFound {
        // do nothing (can be overwritten by subclasses)
    }

    /**
     * process a Fill based on an {@link ExecutionReport} and {@link Order} before it is propagated
     */
    protected void processFill(ExecutionReport executionReport, Order order, Fill fill) throws FieldNotFound {
        // do nothing (can be overwritten by subclasses)
    }

    private Status getStatus(ExecType execType, OrderQty orderQty, CumQty cumQty) {

        if (execType.getValue() == ExecType.NEW) {
            return Status.SUBMITTED;
        } else if (execType.getValue() == ExecType.TRADE) {
            if (cumQty.getValue() == orderQty.getValue()) {
                return Status.EXECUTED;
            } else {
                return Status.PARTIALLY_EXECUTED;
            }
        } else if (execType.getValue() == ExecType.CANCELED || execType.getValue() == ExecType.REJECTED
                || execType.getValue() == ExecType.DONE_FOR_DAY || execType.getValue() == ExecType.EXPIRED) {
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
