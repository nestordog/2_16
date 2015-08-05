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
package ch.algotrader.adapter.ib;

import java.util.concurrent.BlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.field.ClOrdID;
import quickfix.field.ExecType;
import quickfix.field.FAConfigurationAction;
import quickfix.field.FARequestID;
import quickfix.field.OrdStatus;
import quickfix.field.OrigClOrdID;
import quickfix.field.Text;
import quickfix.field.XMLContent;
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.IBFAModification;
import quickfix.fix42.OrderCancelReject;
import ch.algotrader.adapter.fix.fix42.GenericFix42OrderMessageHandler;
import ch.algotrader.esper.Engine;
import ch.algotrader.service.OrderService;

/**
 * IB specific Fix42MessageHandler.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBFixOrderMessageHandler extends GenericFix42OrderMessageHandler {

    private static final Logger LOGGER = LogManager.getLogger(IBFixOrderMessageHandler.class);

    private final BlockingQueue<IBCustomMessage> allocationMessageQueue;

    public IBFixOrderMessageHandler(final OrderService orderService, final BlockingQueue<IBCustomMessage> allocationMessageQueue, final Engine serverEngine) {
        super(orderService, serverEngine);
        this.allocationMessageQueue = allocationMessageQueue;
    }

    @Override
    public void onMessage(ExecutionReport executionReport, SessionID sessionID) throws FieldNotFound {

        // ignore FA transfer execution reports
        if (executionReport.getExecID().getValue().startsWith("F-") || executionReport.getExecID().getValue().startsWith("U+")) {
            return;
        }

        // ignore FA ExecType=NEW / OrdStatus=FILLED (since they arrive after ExecType=FILL)
        if (executionReport.getExecType().getValue() == ExecType.NEW && executionReport.getOrdStatus().getValue() == OrdStatus.FILLED) {
            return;
        }

        super.onMessage(executionReport, sessionID);
    }

    /**
     * updates IB Group definitions
     */
    public void onMessage(IBFAModification faModification, SessionID sessionID) throws FieldNotFound {

        String fARequestID = faModification.get(new FARequestID()).getValue();
        String xmlContent = faModification.get(new XMLContent()).getValue();
        FAConfigurationAction fAConfigurationAction = faModification.get(new FAConfigurationAction());

        if (fAConfigurationAction.valueEquals(FAConfigurationAction.GET_GROUPS)) {
            IBCustomMessage message = new IBCustomMessage(fARequestID, IBCustomMessage.Type.GET_GROUPS, xmlContent);
            this.allocationMessageQueue.add(message);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void onMessage(OrderCancelReject orderCancelReject, SessionID sessionID) throws FieldNotFound {

        Text text = orderCancelReject.getText();
        ClOrdID clOrdID = orderCancelReject.getClOrdID();
        OrigClOrdID origClOrdID = orderCancelReject.getOrigClOrdID();
        if ("Too late to cancel".equals(text.getValue()) || "Cannot cancel the filled order".equals(text.getValue())) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("cannot cancel, order has already been executed, clOrdID: {} origOrdID: {}", clOrdID.getValue(), origClOrdID.getValue());
            }
        } else {
            super.onMessage(orderCancelReject, sessionID);
        }
    }
}
