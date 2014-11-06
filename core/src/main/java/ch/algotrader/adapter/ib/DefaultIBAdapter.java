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
package ch.algotrader.adapter.ib;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import ch.algotrader.enumeration.ConnectionState;

/**
 * Adapter class for IBSessions (IB Connections).
 * This class an its public methods are available through JMX.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@ManagedResource(objectName = "ch.algotrader.adapter.ib:name=IBAdapter")
public class DefaultIBAdapter implements ApplicationContextAware, IBAdapter {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * (re)connects all IBSessions
     */
    @Override
    @ManagedOperation
    @ManagedOperationParameters({})
    public void connect() {

        for (IBSession session : getSessions()) {
            session.connect();
        }
    }

    /**
     * disconnects all IBSessions
     */
    @Override
    @ManagedOperation
    @ManagedOperationParameters({})
    public void disconnect() {

        for (IBSession session : getSessions()) {
            session.disconnect();
        }
    }

    /**
     * Sets the Log Level on all IBSessions
     */
    @ManagedOperation
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "logLevel", description = "<html> <p> logLevel: </p> <ul>     <li> 1 (SYSTEM) </li> <li> 2 (ERROR) </li> <li> 3 (WARNING) </li> <li> 4 (INFORMATION) </li> <li> 5 (DETAIL) </li> </ul>  </html>") })
    public void setLogLevel(int logLevel) {

        for (IBSession session : getSessions()) {
            session.setServerLogLevel(logLevel);
        }
    }

    /**
     * Returns a Map with {@code sessionId} and {@code connectionState} of all IBSessions
     */
    @Override
    @ManagedAttribute
    public Map<Integer, ConnectionState> getConnectionStates() {

        Map<Integer, ConnectionState> connectionStates = new HashMap<Integer, ConnectionState>();
        for (IBSession session : getSessions()) {
            connectionStates.put(session.getClientId(), session.getLifecycle().getConnectionState());
        }
        return connectionStates;
    }

    private Collection<IBSession> getSessions() {
        return this.applicationContext.getBeansOfType(ch.algotrader.adapter.ib.IBSession.class).values();
    }
}
