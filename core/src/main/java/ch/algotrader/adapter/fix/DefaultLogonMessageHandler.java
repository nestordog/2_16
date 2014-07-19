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

import quickfix.ConfigError;
import quickfix.FieldConvertError;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.field.Password;
import quickfix.field.Username;
import quickfix.fix44.Logon;

/**
 * Default outgoing message handler that automatically adds user credentials to the outgoing
 * logon message.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class DefaultLogonMessageHandler {

    private final SessionSettings settings;

    public DefaultLogonMessageHandler(final SessionSettings settings) {
        Validate.notNull(settings, "SessionSettings is null");
        this.settings = settings;
    }

    public void onMessage(Logon logon, SessionID sessionID) throws ConfigError, FieldConvertError {

        String username = this.settings.getString(sessionID, "Username");
        if (username != null) {
            logon.set(new Username(username));

            String password = this.settings.getString(sessionID, "Password");
            if (password != null) {
                logon.set(new Password(password));
            }
        }
    }
}
