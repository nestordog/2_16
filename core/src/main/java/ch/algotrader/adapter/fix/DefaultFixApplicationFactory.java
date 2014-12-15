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

import org.apache.commons.lang.Validate;

import quickfix.Application;
import quickfix.ConfigError;
import quickfix.SessionID;
import quickfix.SessionSettings;

/**
 * Creates a {@link ch.algotrader.adapter.fix.DefaultFixApplication} for the specified {@code sessionId}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class DefaultFixApplicationFactory implements FixApplicationFactory {

    private final Object incomingMessageHandler;
    private final Object outgoingMessageHandler;
    private final FixSessionLifecycle lifecycleHandler;

    public DefaultFixApplicationFactory(final Object incomingMessageHandler, final Object outgoingMessageHandler, final FixSessionLifecycle lifecycleHandler) {
        Validate.notNull(incomingMessageHandler, "IncomingMessageHandler may not be null");
        Validate.notNull(lifecycleHandler, "FixSessionLifecycle may not be null");

        this.incomingMessageHandler = incomingMessageHandler;
        this.outgoingMessageHandler = outgoingMessageHandler;
        this.lifecycleHandler = lifecycleHandler;
    }

    public DefaultFixApplicationFactory(final Object incomingMessageHandler, final FixSessionLifecycle lifecycleHandler) {
        this(incomingMessageHandler, null, lifecycleHandler);
    }

    @Override
    public String getName() {
        return lifecycleHandler.getName();
    }

    @Override
    public String toString() {
        return lifecycleHandler.getName() + " FIX application factory";
    }

    protected Application createApplication(SessionID sessionID, Object incomingMessageHandler, Object outgoingMessageHandler, FixSessionLifecycle lifecycleHandler) {

        return new DefaultFixApplication(sessionID, incomingMessageHandler, outgoingMessageHandler, lifecycleHandler);
    }

    @Override
    public Application create(SessionID sessionID, SessionSettings settings) throws ConfigError {

        return createApplication(sessionID, incomingMessageHandler, outgoingMessageHandler, lifecycleHandler);
    }
}
