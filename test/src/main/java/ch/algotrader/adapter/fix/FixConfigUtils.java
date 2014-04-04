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
