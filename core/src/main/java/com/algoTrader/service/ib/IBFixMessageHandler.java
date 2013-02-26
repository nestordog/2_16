package com.algoTrader.service.ib;

import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.field.FAConfigurationAction;
import quickfix.field.FARequestID;
import quickfix.field.XMLContent;
import quickfix.fix42.IBFAModification;

import com.algoTrader.ServiceLocator;
import com.algoTrader.service.fix.Fix42MessageHandler;

public class IBFixMessageHandler extends Fix42MessageHandler {

    public IBFixMessageHandler(SessionSettings settings) {
        super(settings);
    }

    public void onMessage(IBFAModification faModification, SessionID sessionID) throws Exception {

        IBFixAccountService accountService = ServiceLocator.instance().getService("iBFixAccountService", IBFixAccountService.class);

        String fARequestID = faModification.get(new FARequestID()).getValue();
        String xmlContent = faModification.get(new XMLContent()).getValue();
        FAConfigurationAction fAConfigurationAction = faModification.get(new FAConfigurationAction());

        if (fAConfigurationAction.valueEquals(FAConfigurationAction.GET_GROUPS)) {
            accountService.updateGroups(fARequestID, xmlContent);
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
