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
    private final FixSessionStateHolder stateHolder;

    public DefaultFixApplicationFactory(final Object incomingMessageHandler, final Object outgoingMessageHandler, final FixSessionStateHolder stateHolder) {
        Validate.notNull(incomingMessageHandler, "IncomingMessageHandler may not be null");
        Validate.notNull(stateHolder, "FixSessionStateHolder may not be null");

        this.incomingMessageHandler = incomingMessageHandler;
        this.outgoingMessageHandler = outgoingMessageHandler;
        this.stateHolder = stateHolder;
    }

    public DefaultFixApplicationFactory(final Object incomingMessageHandler, final FixSessionStateHolder stateHolder) {
        this(incomingMessageHandler, null, stateHolder);
    }

    @Override
    public String getName() {
        return stateHolder.getName();
    }

    @Override
    public String toString() {
        return stateHolder.getName() + " FIX application factory";
    }

    protected Application createApplication(SessionID sessionID, Object incomingMessageHandler, Object outgoingMessageHandler, FixSessionStateHolder stateHolder) {

        return new DefaultFixApplication(sessionID, incomingMessageHandler, outgoingMessageHandler, stateHolder);
    }

    @Override
    public Application create(SessionID sessionID, SessionSettings settings) throws ConfigError {

        return createApplication(sessionID, incomingMessageHandler, outgoingMessageHandler, stateHolder);
    }
}
