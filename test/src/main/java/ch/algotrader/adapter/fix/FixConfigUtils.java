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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.junit.Assert;

import quickfix.ConfigError;
import quickfix.SessionID;
import quickfix.SessionSettings;

public class FixConfigUtils {

    public static SessionSettings loadSettings(final String resource) throws IOException, ConfigError {
        ClassLoader cl = FixConfigUtils.class.getClassLoader();
        InputStream instream = cl.getResourceAsStream(resource);
        Assert.assertNotNull(instream);
        try {
            return new SessionSettings(instream);
        } finally {
            instream.close();
        }
    }

    public static SessionSettings loadSettings() throws IOException, ConfigError {
        return loadSettings("fix.cfg");
    }

    public static SessionID getSessionID(final SessionSettings sessionSettings, final String quilifier) {
        for (Iterator<SessionID> it = sessionSettings.sectionIterator(); it.hasNext(); ) {
            SessionID sessionID = it.next();
            if (sessionID.getSessionQualifier().equals(quilifier)) {
                return sessionID;
            }
        }
        return null;
    }

}
