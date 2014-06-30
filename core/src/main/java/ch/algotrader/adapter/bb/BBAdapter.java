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

import java.util.HashMap;
import java.util.Map;

import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import ch.algotrader.config.BBConfig;

import com.bloomberglp.blpapi.SessionOptions;

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

    private Map<String, BBSession> sessions = new HashMap<String, BBSession>();
    private final BBConfig bbConfig;

    public BBAdapter(BBConfig bbConfig) {
        this.bbConfig = bbConfig;
    }

    /**
     * Returns an asynchronous market data session using the {@link BBMarketDataMessageHandler}
     */
    public BBSession getMarketDataSession() throws Exception {

        return getSession("mktdata", new BBMarketDataMessageHandler());
    }

    /**
     * Returns a synchronous reference data session
     */
    public BBSession getReferenceDataSession() throws Exception {

        return getSession("refdata", null);
    }

    private BBSession getSession(String serviceName, BBMessageHandler messageHandler) throws Exception {

        // stop eventual session
        if (this.sessions.containsKey(serviceName)) {
            this.sessions.get(serviceName).stop();
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

        // start the session
        if (!session.start()) {
            throw new IllegalStateException("Failed to start session");
        }

        // open the service
        if (!session.openService("//blp/" + serviceName)) {
            session.stop();
            throw new IllegalStateException("Failed to open service " + serviceName);
        }

        this.sessions.put(serviceName, session);

        return session;
    }

    /**
     * starts all Sessions
     */
    @ManagedOperation
    @ManagedOperationParameters({})
    public void start() throws Exception {

        for (BBSession session : this.sessions.values()) {
            session.start();
        }
    }

    /**
     * stops all Sessions
     */
    @ManagedOperation
    @ManagedOperationParameters({})
    public void stop() throws Exception {

        for (BBSession session : this.sessions.values()) {
            session.stop();
        }
    }
}
