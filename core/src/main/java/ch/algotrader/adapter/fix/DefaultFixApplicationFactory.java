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
package ch.algotrader.adapter.fix;

import quickfix.Application;
import quickfix.ConfigError;
import quickfix.SessionID;
import quickfix.SessionSettings;
import ch.algotrader.enumeration.ConnectionState;

/**
 * Creates a {@link ch.algotrader.adapter.fix.DefaultFixApplication} for the specified {@code sessionId}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class DefaultFixApplicationFactory implements FixApplicationFactory {

    private Object incomingMessageHandler;
    private Object outgoingMessageHandler;
    private FixSessionLifecycle lifecycleHandler;
    private String name;

    public void setIncomingMessageHandler(Object incomingMessageHandler) {
        this.incomingMessageHandler = incomingMessageHandler;
    }

    public void setOutgoingMessageHandler(Object outgoingMessageHandler) {
        this.outgoingMessageHandler = outgoingMessageHandler;
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

        return new DefaultFixApplication(sessionID, incomingMessageHandler, outgoingMessageHandler, lifecycleHandler);
    }
}
