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
package ch.algotrader.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import ch.algotrader.adapter.fix.FixSessionLifecycle;
import ch.algotrader.enumeration.ConnectionState;

/**
 * Service that exposes properties of FIX sessions
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class FixSessionServiceImpl implements FixSessionService, ApplicationContextAware, InitializingBean {

    private final Map<String, FixSessionLifecycle> fixSessionLifecycleMap;

    private volatile ApplicationContext applicationContext;

    public FixSessionServiceImpl() {
        this.fixSessionLifecycleMap = new ConcurrentHashMap<>();
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {

        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, FixSessionLifecycle> map = applicationContext.getBeansOfType(FixSessionLifecycle.class);
        for (Map.Entry<String, FixSessionLifecycle> entry : map.entrySet()) {
            FixSessionLifecycle sessionLifecycle = entry.getValue();
            fixSessionLifecycleMap.put(sessionLifecycle.getName(), sessionLifecycle);
        }
    }

    @Override
    public Map<String, ConnectionState> getAllSessionState() {

        Map<String, ConnectionState> connectionStates = new HashMap<>(fixSessionLifecycleMap.size());
        for (Map.Entry<String, FixSessionLifecycle> entry : fixSessionLifecycleMap.entrySet()) {
            FixSessionLifecycle sessionLifecycle = entry.getValue();
            connectionStates.put(sessionLifecycle.getName(), sessionLifecycle.getConnectionState());
        }
        return connectionStates;
    }

    @Override
    public ConnectionState getSessionState(final String name) {

        if (name == null) {
            return null;
        }
        FixSessionLifecycle sessionLifecycle = fixSessionLifecycleMap.get(name);
        return sessionLifecycle != null ? sessionLifecycle.getConnectionState() : null;
    }

}
