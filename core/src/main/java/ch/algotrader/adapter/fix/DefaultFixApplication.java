/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.adapter.fix;

import org.apache.commons.lang.Validate;

import ch.algotrader.adapter.ExternalSessionStateHolder;
import quickfix.SessionID;

/**
 * Implementation of {@link quickfix.Application}
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class DefaultFixApplication extends AbstractFixApplication {

    private final ExternalSessionStateHolder stateHolder;

    public DefaultFixApplication(SessionID sessionID, Object incomingMessageHandler, Object outgoingMessageHandler, ExternalSessionStateHolder stateHolder) {
        super(sessionID, incomingMessageHandler, outgoingMessageHandler);
        Validate.notNull(sessionID, "FixSessionStateHolder may not be null");
        this.stateHolder = stateHolder;
    }

    public DefaultFixApplication(SessionID sessionID, Object incomingMessageHandler, ExternalSessionStateHolder stateHolder) {
        this(sessionID, incomingMessageHandler, null, stateHolder);
    }

    @Override
    public void onCreate() {

        stateHolder.onCreate();
    }

    @Override
    public void onLogon() {

        stateHolder.onLogon();
    }

    @Override
    public void onLogout() {

        stateHolder.onLogoff();
    }

}
