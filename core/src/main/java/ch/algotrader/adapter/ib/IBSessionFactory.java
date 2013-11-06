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
package ch.algotrader.adapter.ib;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import ch.algotrader.enumeration.ConnectionState;

/**
 * Factory class for IBSessions (IB Connections).
 * This class an its public methods are available through JMX.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@ManagedResource(objectName = "ch.algotrader.adapter.ib:name=IBSessionFactory")
public class IBSessionFactory {

    private @Value("${simulation}") boolean simulation;
    private @Value("${ib.defaultSessionId}") int defaultSessionId;

    private IBSession defaultSession;
    private Map<Integer, IBSession> sessions = new HashMap<Integer, IBSession>();

    /**
     * Gets the DefaultSession
     */
    public IBSession getDefaultSession() {

        if (this.simulation) {
            return null;
        }

        if (this.defaultSession == null) {

            this.defaultSession = new IBSession(this.defaultSessionId, new IBEsperMessageHandler(this.defaultSessionId));

            this.sessions.put(this.defaultSessionId, this.defaultSession);

            this.defaultSession.connect();
        }

        return this.defaultSession;
    }

    /**
     * Gets a new IBSession based on a {@code sessionId} and a {@code IBDefaultMessageHandler MessageHandler}
     */
    public IBSession getSession(int sessionId, IBDefaultMessageHandler messageHandler) {

        IBSession session = new IBSession(sessionId, messageHandler);
        this.sessions.put(sessionId, session);

        return session;
    }

    /**
     * (re)connects all IBSessions
     */
    @ManagedOperation
    @ManagedOperationParameters({})
    public void connect() {

        for (IBSession session : this.sessions.values()) {
            session.connect();
        }
    }

    /**
     * disconnects all IBSessions
     */
    @ManagedOperation
    @ManagedOperationParameters({})
    public void disconnect() {

        for (IBSession session : this.sessions.values()) {
            session.disconnect();
        }
    }

    /**
     * Sets the Log Level on all IBSessions
     */
    @ManagedOperation
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "logLevel", description = "<html> <head> </head> <body> <p> logLevel: </p> <ul>     <li> 1 (SYSTEM) </li> <li> 2 (ERROR) </li> <li> 3 (WARNING) </li> <li> 4 (INFORMATION) </li> <li> 5 (DETAIL) </li> </ul> </body> </html>") })
    public void setLogLevel(int logLevel) {

        for (IBSession session : this.sessions.values()) {
            session.setServerLogLevel(logLevel);
        }
    }

    /**
     * Returns a Map with {@code sessionId} and {@code connectionState} of all IBSessions
     */
    @ManagedAttribute
    public Map<Integer, ConnectionState> getConnectionStates() {

        Map<Integer, ConnectionState> connectionStates = new HashMap<Integer, ConnectionState>();
        for (Map.Entry<Integer, IBSession> entry : this.sessions.entrySet()) {
            connectionStates.put(entry.getKey(), entry.getValue().getMessageHandler().getState());
        }
        return connectionStates;
    }
}
