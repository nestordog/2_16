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

import quickfix.Application;
import quickfix.ConfigError;
import quickfix.SessionID;
import quickfix.SessionSettings;
import ch.algotrader.enumeration.ConnectionState;

/**
 * FIX {@link Application} factory.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface FixApplicationFactory {

    /**
     * Creates single session FIX {@link Application} for the given {@code sessionID}
     */
    Application create(SessionID sessionID, SessionSettings settings) throws ConfigError;

    /**
     * Gets the name of this application factory
     */
    String getName();

    /**
     * Gets the current connection state
     */
    ConnectionState getConnectionState();
}
