package com.algoTrader.service.dc;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.log4j.Logger;

import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.field.CumQty;
import quickfix.field.OrdStatus;
import quickfix.field.OrderID;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.Logon;
import quickfix.fix44.OrderCancelReject;
import quickfix.fix44.Reject;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.trade.Fill;
import com.algoTrader.entity.trade.Order;
import com.algoTrader.entity.trade.OrderStatus;
import com.algoTrader.enumeration.Side;
import com.algoTrader.enumeration.Status;
import com.algoTrader.esper.EsperManager;
import com.algoTrader.service.fix.FixUtil;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;

public class DCFixMessageHandler {

    private static Logger logger = MyLogger.getLogger(DCFixMessageHandler.class.getName());

    private String username = ServiceLocator.instance().getConfiguration().getString("dc.username");
    private String password = ServiceLocator.instance().getConfiguration().getString("dc.password");

    public void onMessage(ExecutionReport executionReport, SessionID sessionID) {

        try {

            Integer clOrdID = Integer.parseInt(executionReport.getClOrdID().getValue());

            // NOTE: DukasCopy does not use ExecType instead it uses only OrdStatus
            if (executionReport.getOrdStatus().getValue() == OrdStatus.REJECTED) {
                logger.error("order " + clOrdID + " has been rejected, reason: " + executionReport.getText().getValue());
            }

            // get the order from the OpenOrderWindow
            Order order = ServiceLocator.instance().getLookupService().getOpenOrderByIntId(clOrdID);
            if (order == null) {
                logger.error("order could not be found " + clOrdID + " for execution " + executionReport);
                return;
            }

            // Note: DukasCopy requires OrderID for cancels and replaces
            if (order.getExtId() == null && executionReport.isSet(new OrderID())) {
                order.setExtId(executionReport.getOrderID().getValue());
                EsperManager.sendEvent(StrategyImpl.BASE, order);
            }

            // get the other fields
            Status status = getStatus(executionReport.getOrdStatus(), executionReport.getCumQty());
            long filledQuantity = (long) executionReport.getCumQty().getValue();
            long remainingQuantity = (long) (executionReport.getOrderQty().getValue() - executionReport.getCumQty().getValue());

            // assemble the orderStatus
            OrderStatus orderStatus = OrderStatus.Factory.newInstance();
            orderStatus.setStatus(status);
            orderStatus.setFilledQuantity(filledQuantity);
            orderStatus.setRemainingQuantity(remainingQuantity);
            orderStatus.setOrd(order);

            EsperManager.sendEvent(StrategyImpl.BASE, orderStatus);

            // only create fills if status is FILLED (Note: DukasCopy does nut use PARTIALLY_FILLED)
            if (executionReport.getOrdStatus().getValue() == OrdStatus.FILLED) {

                // get the fields
                Date dateTime = DateUtil.getCurrentEPTime();
                Date extDateTime = executionReport.getTransactTime().getValue();
                Side side = FixUtil.getSide(executionReport.getSide());
                long quantity = (long) executionReport.getCumQty().getValue();

                // Note: DukasCopy does not use LastPx it only uses AvgPx
                BigDecimal price = RoundUtil.getBigDecimal(executionReport.getAvgPx().getValue(), order.getSecurity().getSecurityFamily().getScale());
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
            logger.error("order cancel/replace has been rejected, clOrdID: " + orderCancelReject.getClOrdID().getValue() +
                    " origOrdID: " + orderCancelReject.getOrigClOrdID().getValue() +
                    " reason: " + orderCancelReject.getText().getValue());
        } catch (FieldNotFound e) {
            logger.error(e);
        }
    }

    public void onMessage(Reject reject, SessionID sessionID) {

        try {
            logger.error("message number: " + reject.getRefSeqNum() +
                    " type: " + reject.getRefMsgType() +
                    " has been rejected, tag: " + reject.getRefTagID() + " " + reject.getText());
        } catch (FieldNotFound e) {
            logger.error(e);
        }
    }

    public void onMessage(Logon logon, SessionID sessionID) {

        logon.setString(553, this.username);
        logon.setString(554, this.password);
    }

    public static Status getStatus(OrdStatus ordStatus, CumQty cumQty) {

        // Note: DukasCopy uses CALCULATED instead of NEW
        if (ordStatus.getValue() == OrdStatus.CALCULATED || ordStatus.getValue() == OrdStatus.PENDING_NEW) {
            return Status.SUBMITTED;
        } else if (ordStatus.getValue() == OrdStatus.FILLED) {
            return Status.EXECUTED;
        } else if (ordStatus.getValue() == OrdStatus.CANCELED || ordStatus.getValue() == OrdStatus.REJECTED) {
            return Status.CANCELED;
        } else if (ordStatus.getValue() == OrdStatus.REPLACED) {
            if (cumQty.getValue() == 0) {
                return Status.SUBMITTED;
            } else {
                return Status.PARTIALLY_EXECUTED;
            }
        } else {
            throw new IllegalArgumentException("unknown orderStatus " + ordStatus.getValue());
        }
    }
}
