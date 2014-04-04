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

import org.apache.commons.lang.Validate;

import quickfix.SessionID;

/**
 * Implementation of {@link quickfix.Application}
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class DefaultFixApplication extends AbstractFixApplication {

    private final FixSessionLifecycle lifecycleHandler;

    public DefaultFixApplication(SessionID sessionID, Object incomingMessageHandler, Object outgoingMessageHandler, FixSessionLifecycle lifecycleHandler) {
        super(sessionID, incomingMessageHandler, outgoingMessageHandler);

        Validate.notNull(sessionID, "Session ID may not be null");
        this.lifecycleHandler = lifecycleHandler;
    }

    public DefaultFixApplication(SessionID sessionID, Object incomingMessageHandler, FixSessionLifecycle lifecycleHandler) {
        this(sessionID, incomingMessageHandler, null, lifecycleHandler);
    }

    @Override
    public void onCreate() {

        lifecycleHandler.create();
    }

    @Override
    public void onLogon() {

        lifecycleHandler.logon();
    }

    @Override
    public void onLogout() {

        lifecycleHandler.logoff();
    }

}
