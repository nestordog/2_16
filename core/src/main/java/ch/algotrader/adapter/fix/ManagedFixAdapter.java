/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.adapter.fix;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import ch.algotrader.adapter.ExternalSessionStateHolder;
import ch.algotrader.enumeration.ConnectionState;
import ch.algotrader.adapter.OrderIdGenerator;
import ch.algotrader.service.LookupService;
import ch.algotrader.util.collection.IntegerMap;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SocketInitiator;

/**
 * Manageable implementation of {@link FixAdapter}.
 * This class an its public methods are available through JMX.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
@ManagedResource(objectName = "ch.algotrader.adapter.fix:name=FixAdapter")
public class ManagedFixAdapter extends DefaultFixAdapter implements ApplicationContextAware, InitializingBean {

    private final Map<String, ExternalSessionStateHolder> fixSessionStateHolderMap;

    private volatile ApplicationContext applicationContext;

    public ManagedFixAdapter(
            final SocketInitiator socketInitiator,
            final LookupService lookupService,
            final FixEventScheduler eventScheduler,
            final OrderIdGenerator orderIdGenerator) {
        super(socketInitiator, lookupService, eventScheduler, orderIdGenerator);
        this.fixSessionStateHolderMap = new ConcurrentHashMap<>();
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {

        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, ExternalSessionStateHolder> map = applicationContext.getBeansOfType(ExternalSessionStateHolder.class);
        for (Map.Entry<String, ExternalSessionStateHolder> entry : map.entrySet()) {
            ExternalSessionStateHolder fixSessionStateHolder = entry.getValue();
            fixSessionStateHolderMap.put(fixSessionStateHolder.getName(), fixSessionStateHolder);
        }
    }

    /**
     * returns the connection state of all application factories
     */
    @ManagedAttribute
    public Map<String, ConnectionState> getApplicationFactoryConnectionStates() {

        Map<String, ConnectionState> connectionStates = new HashMap<>(fixSessionStateHolderMap.size());
        for (Map.Entry<String, ExternalSessionStateHolder> entry : fixSessionStateHolderMap.entrySet()) {
            ExternalSessionStateHolder fixSessionStateHolder = entry.getValue();
            connectionStates.put(fixSessionStateHolder.getName(), fixSessionStateHolder.getConnectionState());
        }
        return connectionStates;
    }


    /**
     * returns the logon state of all active sessions
     */
    @ManagedAttribute
    public Map<String, Boolean> getSessionLogonStates() {

        Map<String, Boolean> logonStates = new HashMap<>();
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
