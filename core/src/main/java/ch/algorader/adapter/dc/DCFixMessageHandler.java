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
package ch.algorader.adapter.dc;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.log4j.Logger;

import quickfix.ConfigError;
import quickfix.FieldConvertError;
import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.field.CumQty;
import quickfix.field.OrdStatus;
import quickfix.field.Password;
import quickfix.field.Username;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.Logon;
import quickfix.fix44.OrderCancelReject;
import quickfix.fix44.Reject;

import ch.algorader.adapter.fix.FixUtil;
import ch.algorader.entity.strategy.StrategyImpl;
import ch.algorader.esper.EsperManager;
import ch.algorader.util.MyLogger;
import ch.algorader.util.RoundUtil;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.trade.Fill;
import com.algoTrader.entity.trade.Order;
import com.algoTrader.entity.trade.OrderStatus;
import com.algoTrader.enumeration.Side;
import com.algoTrader.enumeration.Status;

/**
 * DC specific FixMessageHandler.
 * Since the DC Fix interface does not comply with the Fix Standard, this class does not extend a generic FixMessageHandler.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class DCFixMessageHandler {

    private static Logger logger = MyLogger.getLogger(DCFixMessageHandler.class.getName());

    private final SessionSettings settings;

    public DCFixMessageHandler(SessionSettings settings) {

        this.settings = settings;
    }

    public void onMessage(ExecutionReport executionReport, SessionID sessionID) {

        try {

            String intId = executionReport.getClOrdID().getValue();

            // NOTE: DukasCopy does not use ExecType instead it uses only OrdStatus
            if (executionReport.getOrdStatus().getValue() == OrdStatus.REJECTED) {
                logger.error("order " + intId + " has been rejected, reason: " + executionReport.getText().getValue());
            }

            // get the order from the OpenOrderWindow
            Order order = ServiceLocator.instance().getLookupService().getOpenOrderByRootIntId(intId);
            if (order == null) {
                logger.error("order with intId " + intId + " could not be found for execution " + executionReport);
                return;
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

            // update intId in case it has changed
            if (!order.getIntId().equals(intId)) {
                orderStatus.setIntId(intId);
            }

            // Note: store OrderID sind DukasCopy requires it for cancels and replaces
            if (order.getExtId() == null) {
                orderStatus.setExtId(executionReport.getOrderID().getValue());
            }

            EsperManager.sendEvent(StrategyImpl.BASE, orderStatus);

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

    public void onMessage(Logon logon, SessionID sessionID) throws ConfigError, FieldConvertError {

        logon.set(new Username(this.settings.getString(sessionID, "Username")));
        logon.set(new Password(this.settings.getString(sessionID, "Password")));
    }

    public static Status getStatus(OrdStatus ordStatus, CumQty cumQty) {

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
