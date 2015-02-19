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
package ch.algotrader.adapter.fxcm;

import org.apache.commons.lang.Validate;

import ch.algotrader.adapter.fix.FixApplicationFactory;
import ch.algotrader.adapter.fix.FixSessionStateHolder;
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

    private final Object incomingMessageHandler;
    private final FixSessionStateHolder stateHolder;

    public FXCMFixApplicationFactory(final Object incomingMessageHandler, final FixSessionStateHolder stateHolder) {
        Validate.notNull(incomingMessageHandler, "IncomingMessageHandler may not be null");
        Validate.notNull(stateHolder, "FixSessionStateHolder may not be null");

        this.incomingMessageHandler = incomingMessageHandler;
        this.stateHolder = stateHolder;
    }

    @Override
    public String getName() {
        return stateHolder.getName();
    }

    @Override
    public String toString() {
        return stateHolder.getName() + " FIX application factory";
    }

    @Override
    public Application create(SessionID sessionID, SessionSettings settings) throws ConfigError {

        return new FXCMFixApplication(sessionID, incomingMessageHandler, settings, stateHolder);
    }
}
