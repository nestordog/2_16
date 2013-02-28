package com.algoTrader.service.ib;

import org.apache.log4j.Logger;

import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.field.ExecType;
import quickfix.field.FAConfigurationAction;
import quickfix.field.FARequestID;
import quickfix.field.OrdStatus;
import quickfix.field.XMLContent;
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.IBFAModification;

import com.algoTrader.ServiceLocator;
import com.algoTrader.service.fix.Fix42MessageHandler;
import com.algoTrader.util.MyLogger;

public class IBFixMessageHandler extends Fix42MessageHandler {

    private static Logger logger = MyLogger.getLogger(IBFixMessageHandler.class.getName());

    public IBFixMessageHandler(SessionSettings settings) {
        super(settings);
    }

    @Override
    public void onMessage(ExecutionReport executionReport, SessionID sessionID) {

        try {

            // ignore FA transfer execution reporst
            if (executionReport.getExecID().getValue().startsWith("F-") || executionReport.getExecID().getValue().startsWith("U+")) {
                return;
            }

            // ignore FA ExecType=NEW / OrdStatus=FILLED (since they arrive after ExecType=FILL)
            if (executionReport.getExecType().getValue() == ExecType.NEW && executionReport.getOrdStatus().getValue() == OrdStatus.FILLED) {
                return;
            }

            super.onMessage(executionReport, sessionID);

        } catch (FieldNotFound e) {
            logger.error(e);
        }
    }

    public void onMessage(IBFAModification faModification, SessionID sessionID) {

        try {
            IBFixAccountService accountService = ServiceLocator.instance().getService("iBFixAccountService", IBFixAccountService.class);

            String fARequestID = faModification.get(new FARequestID()).getValue();
            String xmlContent = faModification.get(new XMLContent()).getValue();
            FAConfigurationAction fAConfigurationAction = faModification.get(new FAConfigurationAction());

            if (fAConfigurationAction.valueEquals(FAConfigurationAction.GET_GROUPS)) {
                accountService.updateGroups(fARequestID, xmlContent);
            } else {
                throw new UnsupportedOperationException();
            }
        } catch (FieldNotFound e) {
            logger.error(e);
        }
    }
}
