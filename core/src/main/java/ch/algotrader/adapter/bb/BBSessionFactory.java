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
package ch.algotrader.adapter.bb;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bloomberglp.blpapi.SessionOptions;

/**
 * Factory class for Bloomberg Sessions.
 * This class an its public methods are available through JMX.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision: 5945 $ $Date: 2013-05-31 14:04:34 +0200 (Fr, 31 Mai 2013) $
 */
@ManagedResource(objectName = "ch.algotrader.adapter.bb:name=BBSessionFactory")
public class BBSessionFactory {

    private @Value("${bb.host}") String host;
    private @Value("${bb.port}") int port;

    private Map<String, BBSession> sessions = new HashMap<String, BBSession>();

    public BBSession getMarketDataSession() throws Exception {

        return getSession("mktdata", new BBMessageHandler());
    }

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
        sessionOptions.setServerHost(this.host);
        sessionOptions.setServerPort(this.port);

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
