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
package ch.algotrader.adapter.tt;

import quickfix.ConfigError;
import quickfix.FieldConvertError;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.field.RawData;
import quickfix.fix42.Logon;

public class TTLogonMessageHandler {

    private final SessionSettings settings;

    public TTLogonMessageHandler(final SessionSettings settings) {
        this.settings = settings;
    }

    public void onMessage(final Logon logon, final SessionID sessionID) throws ConfigError, FieldConvertError {
        String password = this.settings.getString(sessionID, "Password");
        if (password != null) {
            logon.set(new RawData(password));
        }
    }
}
