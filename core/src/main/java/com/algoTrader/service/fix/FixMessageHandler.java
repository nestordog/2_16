package com.algoTrader.service.fix;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.log4j.Logger;

import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.field.ExecType;
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.OrderCancelReject;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.trade.Fill;
import com.algoTrader.entity.trade.Order;
import com.algoTrader.entity.trade.OrderStatus;
import com.algoTrader.enumeration.Side;
import com.algoTrader.enumeration.Status;
import com.algoTrader.esper.EsperManager;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;

public class FixMessageHandler {

    private static Logger logger = MyLogger.getLogger(FixMessageHandler.class.getName());

    public void onMessage(ExecutionReport executionReport, SessionID sessionID) {

        try {

            Integer number = Integer.parseInt(executionReport.getClOrdID().getValue());

            // ignore PENDING_NEW, PENDING_CANCEL and PENDING_REPLACE
            if (executionReport.getExecType().getValue() == ExecType.PENDING_NEW || executionReport.getExecType().getValue() == ExecType.PENDING_REPLACE
                    || executionReport.getExecType().getValue() == ExecType.PENDING_CANCEL) {
                return;
            }

            if (executionReport.getExecType().getValue() == ExecType.REJECTED) {
                logger.error("order " + number + " has been rejected, reason: " + executionReport.getText().getValue());
            }

            if (executionReport.getExecType().getValue() == ExecType.CANCELED) {

                // check if there are errors
                if (!executionReport.isSetExecRestatementReason()) {

                    // get the number from OrigClOrdID (if it exists)
                    if (executionReport.isSetOrigClOrdID()) {
                        number = Integer.parseInt(executionReport.getOrigClOrdID().getValue());
                    }

                    // if the field ExecRestatementReason exists, there is something wrong
                } else {
                    logger.error("order " + number + " has been canceled, reason: " + executionReport.getText().getValue());
                }
            }

            // get the order from the OpenOrderWindow
            Order order = ServiceLocator.instance().getLookupService().getOpenOrderByNumber(number);
            if (order == null) {
                logger.error("order could not be found " + number + " for execution " + executionReport);
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

            EsperManager.sendEvent(StrategyImpl.BASE, orderStatus);

            // only create fills if status is PARTIALLY_FILLED or FILLED
            if (executionReport.getExecType().getValue() == ExecType.PARTIAL_FILL || executionReport.getExecType().getValue() == ExecType.FILL) {

                // get the fields
                Date dateTime = DateUtil.getCurrentEPTime();
                Date extDateTime = executionReport.getTransactTime().getValue();
                Side side = FixUtil.getSide(executionReport.getSide());
                long quantity = (long) executionReport.getLastShares().getValue();
                BigDecimal price = RoundUtil.getBigDecimal(executionReport.getLastPx().getValue(), order.getSecurity().getSecurityFamily().getScale());
                String extId = executionReport.getExecID().getValue();

                // assemble the fill
                Fill fill = Fill.Factory.newInstance();
                fill.setDateTime(dateTime);
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
            if ("Too late to cancel".equals(orderCancelReject.getText().getValue())) {
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
