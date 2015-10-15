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
package ch.algotrader.wiring.server;

import javax.management.MBeanServer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jmx.support.MBeanServerFactoryBean;

import ch.algotrader.cache.CacheManagerImpl;
import ch.algotrader.cache.CacheManagerMBean;

/**
 * Core JMX configuration.
 */
@Profile(value = "live")
@Configuration
public class CoreJMXWiring {

    @Bean(name = "mbeanServer")
    public MBeanServer createMBeanServer() {

        MBeanServerFactoryBean mBeanServerFactoryBean = new MBeanServerFactoryBean();
        mBeanServerFactoryBean.setLocateExistingServerIfPossible(true);

        return mBeanServerFactoryBean.getObject();
    }

    @Bean(name = "cacheManagerMBean")
    public CacheManagerMBean createCacheManagerMBean(final CacheManagerImpl cacheManager) {

        return new CacheManagerMBean(cacheManager);
    }

}
