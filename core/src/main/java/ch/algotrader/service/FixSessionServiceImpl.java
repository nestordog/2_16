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

import ch.algotrader.adapter.fix.FixSessionStateHolder;
import ch.algotrader.enumeration.ConnectionState;

/**
 * Service that exposes properties of FIX sessions
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class FixSessionServiceImpl implements FixSessionService, ApplicationContextAware, InitializingBean {

    private final Map<String, FixSessionStateHolder> fixSessionStateHolderMap;

    private volatile ApplicationContext applicationContext;

    public FixSessionServiceImpl() {
        this.fixSessionStateHolderMap = new ConcurrentHashMap<>();
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {

        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, FixSessionStateHolder> map = applicationContext.getBeansOfType(FixSessionStateHolder.class);
        for (Map.Entry<String, FixSessionStateHolder> entry : map.entrySet()) {
            FixSessionStateHolder fixSessionStateHolder = entry.getValue();
            fixSessionStateHolderMap.put(fixSessionStateHolder.getName(), fixSessionStateHolder);
        }
    }

    @Override
    public Map<String, ConnectionState> getAllSessionState() {

        Map<String, ConnectionState> connectionStates = new HashMap<>(fixSessionStateHolderMap.size());
        for (Map.Entry<String, FixSessionStateHolder> entry : fixSessionStateHolderMap.entrySet()) {
            FixSessionStateHolder fixSessionStateHolder = entry.getValue();
            connectionStates.put(fixSessionStateHolder.getName(), fixSessionStateHolder.getConnectionState());
        }
        return connectionStates;
    }

    @Override
    public ConnectionState getSessionState(final String name) {

        if (name == null) {
            return null;
        }
        FixSessionStateHolder fixSessionStateHolder = fixSessionStateHolderMap.get(name);
        return fixSessionStateHolder != null ? fixSessionStateHolder.getConnectionState() : null;
    }

}
