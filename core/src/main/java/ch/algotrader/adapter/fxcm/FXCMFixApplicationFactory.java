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
package ch.algotrader.adapter.fxcm;

import ch.algotrader.adapter.fix.FixApplicationFactory;
import ch.algotrader.adapter.fix.FixSessionLifecycle;
import ch.algotrader.enumeration.ConnectionState;
import quickfix.Application;
import quickfix.ConfigError;
import quickfix.SessionID;
import quickfix.SessionSettings;

/**
 * Creates a {@link ch.algotrader.adapter.fxcm.FXCMFixApplication} for the specified {@code sessionId}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class FXCMFixApplicationFactory implements FixApplicationFactory {

    private Object incomingMessageHandler;
    private FixSessionLifecycle lifecycleHandler;
    private String name;

    public void setIncomingMessageHandler(Object incomingMessageHandler) {
        this.incomingMessageHandler = incomingMessageHandler;
    }

    public void setLifecycleHandler(FixSessionLifecycle lifecycleHanlder) {
        this.lifecycleHandler = lifecycleHanlder;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ConnectionState getConnectionState() {
        return lifecycleHandler.getConnectionState();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Application create(SessionID sessionID, SessionSettings settings) throws ConfigError {

        return new FXCMFixApplication(sessionID, incomingMessageHandler, settings, lifecycleHandler);
    }
}
