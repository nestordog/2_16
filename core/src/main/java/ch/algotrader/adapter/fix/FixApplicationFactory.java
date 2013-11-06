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

/**
 * Creates a {@link FixApplication} for the specified {@code sessionId}.
 * The associated {@code messageHandler} is created based on the session specific class specified in the fix.cfg
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class FixApplicationFactory {

    private static final String SETTING_HANDLER_CLASS_NAME = "HandlerClassName";

    private final SessionSettings settings;

    public FixApplicationFactory(SessionSettings settings) {
        this.settings = settings;
    }

    public Application create(SessionID sessionID) throws ConfigError {

        String className = null;
        try {
            if (this.settings.isSetting(sessionID, SETTING_HANDLER_CLASS_NAME)) {
                className = this.settings.getString(sessionID, SETTING_HANDLER_CLASS_NAME);
            } else {
                throw new IllegalStateException(SETTING_HANDLER_CLASS_NAME + " not defined");
            }
            Object messageHandler = Class.forName(className).getConstructor(SessionSettings.class).newInstance(this.settings);
            return new FixApplication(messageHandler);
        } catch (ClassNotFoundException e) {
            throw new ConfigError(SETTING_HANDLER_CLASS_NAME + "=" + className + " class not found");
        } catch (Exception ex) {
            throw new ConfigError(SETTING_HANDLER_CLASS_NAME + " failed", ex);
        }
    }
}
