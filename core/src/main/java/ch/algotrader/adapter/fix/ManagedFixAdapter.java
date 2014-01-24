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

import quickfix.Session;
import quickfix.SessionID;
import ch.algotrader.enumeration.ConnectionState;
import ch.algotrader.util.collection.IntegerMap;

/**
 * Managed implementation of {@link FixAdapter}.
 * This class an its public methods are available through JMX.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@ManagedResource(objectName = "ch.algotrader.adapter.fix:name=FixAdapter")
public class ManagedFixAdapter extends DefaultFixAdapter implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * returns the connection state of all application factories
     */
    @ManagedAttribute
    public Map<String, ConnectionState> getApplicationFactoryConnectionStates() {

        Collection<FixApplicationFactory> applicationFactories = applicationContext.getBeansOfType(FixApplicationFactory.class).values();

        Map<String, ConnectionState> connectionStates = new HashMap<String, ConnectionState>();
        for (FixApplicationFactory applicationFactory : applicationFactories) {
            connectionStates.put(applicationFactory.getName(), applicationFactory.getConnectionState());
        }
        return connectionStates;
    }


    /**
     * returns the logon state of all active sessions
     */
    @ManagedAttribute
    public Map<String, Boolean> getSessionLogonStates() {

        Map<String, Boolean> logonStates = new HashMap<String, Boolean>();
        for (SessionID sessionId : getSocketInitiator().getSessions()) {
            Session session = Session.lookupSession(sessionId);
            logonStates.put(sessionId.getSessionQualifier(), session.isLoggedOn());
        }
        return logonStates;
    }

    /**
     *  gets the current orderIds for all active sessions
     */
    @ManagedAttribute
    public IntegerMap<String> getOrderIds() {

        return getOrderIdGenerator().getOrderIds();
    }

    /**
     * sets the orderId for the defined session (will be incremented by 1 for the next order)
     */
    @ManagedOperation
    @ManagedOperationParameters({
        @ManagedOperationParameter(name = "sessionQualifier", description = ""),
        @ManagedOperationParameter(name = "orderId", description = "orderId (will be incremented by 1 for the next order)")
    })
    public void setOrderId(String sessionQualifier, int orderId) {

        getOrderIdGenerator().setOrderId(sessionQualifier, orderId);
    }

}
