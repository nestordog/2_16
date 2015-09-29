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
package ch.algotrader.adapter.bb;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bloomberglp.blpapi.SessionOptions;

import ch.algotrader.config.BBConfig;

/**
 * Factory class for Bloomberg Sessions.
 *
 * This class adn its public methods are available through JMX.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision: 5945 $ $Date: 2013-05-31 14:04:34 +0200 (Fr, 31 Mai 2013) $
 */
@ManagedResource(objectName = "ch.algotrader.adapter.bb:name=BBAdapter")
public class BBAdapter {

    private final BBConfig bbConfig;
    private final Map<String, BBSession> sessions;

    public BBAdapter(final BBConfig bbConfig) {
        this.bbConfig = bbConfig;
        this.sessions = new ConcurrentHashMap<>();
    }

    /**
     * Returns an asynchronous market data session using the {@link BBMarketDataMessageHandler}
     */
    public BBSession createMarketDataSession(final BBMarketDataMessageHandler messageHandler) throws IOException, InterruptedException {

        return createSession("mktdata", messageHandler);
    }

    /**
     * Returns a synchronous reference data session
     */
    public BBSession createReferenceDataSession() throws IOException, InterruptedException {

        return createSession("refdata", null);
    }

    private BBSession createSession(final String serviceName, final BBMessageHandler messageHandler) throws InterruptedException, IOException {

        // stop eventual session
        BBSession previousSession = this.sessions.remove(serviceName);
        if (previousSession != null) {
            previousSession.stop();
        }

        // create the session options
        SessionOptions sessionOptions = new SessionOptions();
        sessionOptions.setServerHost(this.bbConfig.getHost());
        sessionOptions.setServerPort(this.bbConfig.getPort());

        // create the session
        BBSession session;
        if (messageHandler != null) {
            session = new BBSession(serviceName, sessionOptions, messageHandler);
        } else {
            session = new BBSession(serviceName, sessionOptions);
        }

        this.sessions.put(serviceName, session);

        return session;
    }

    /**
     * returns the state of all active sessions
     */
    @ManagedAttribute
    public Map<String, Boolean> getSessionLogonStates() {

        Map<String, Boolean> logonStates = new HashMap<>();
        for (Map.Entry<String, BBSession> entry : this.sessions.entrySet()) {
            logonStates.put(entry.getKey(), entry.getValue().isRunning());
        }
        return logonStates;
    }

    /**
     * starts all Sessions
     */
    @ManagedOperation
    @ManagedOperationParameters({})
    public void start() throws IOException, InterruptedException {

        for (BBSession session : this.sessions.values()) {
            session.start();
        }
    }

    /**
     * stops all Sessions
     */
    @ManagedOperation
    @ManagedOperationParameters({})
    public void stop() throws IOException, InterruptedException {

        for (BBSession session : this.sessions.values()) {
            session.stop();
        }
    }
}
