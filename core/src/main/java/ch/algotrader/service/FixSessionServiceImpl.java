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

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import ch.algotrader.adapter.fix.FixApplicationFactory;
import ch.algotrader.enumeration.ConnectionState;

/**
 * Service that exposes properties of FIX sessions
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class FixSessionServiceImpl implements FixSessionService, ApplicationContextAware {

    private volatile ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Map<String, ConnectionState> getAllSessionState() {

        Map<String, FixApplicationFactory> appFactoryMap = this.applicationContext.getBeansOfType(FixApplicationFactory.class);

        Map<String, ConnectionState> connectionStates = new HashMap<>(appFactoryMap.size());
        for (Map.Entry<String, FixApplicationFactory> entry : appFactoryMap.entrySet()) {
            FixApplicationFactory appFactory = entry.getValue();
            connectionStates.put(appFactory.getName(), appFactory.getConnectionState());
        }
        return connectionStates;
    }

    @Override
    public ConnectionState getSessionState(final String name) {

        if (name == null) {

            return null;
        }
        Map<String, FixApplicationFactory> appFactoryMap = this.applicationContext.getBeansOfType(FixApplicationFactory.class);
        for (Map.Entry<String, FixApplicationFactory> entry : appFactoryMap.entrySet()) {
            FixApplicationFactory appFactory = entry.getValue();
            if (name.equals(appFactory.getName())) {

                return appFactory.getConnectionState();
            }
        }
        return null;
    }

}
