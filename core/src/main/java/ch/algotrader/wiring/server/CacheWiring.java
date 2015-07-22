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

import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

import ch.algotrader.cache.CacheManager;
import ch.algotrader.cache.CacheManagerImpl;
import ch.algotrader.cache.CacheManagerMBean;
import ch.algotrader.hibernate.GenericDao;

/**
 * Cache configuration.
 */
@Configuration
public class CacheWiring {

    @Bean(name = "genericDao")
    public GenericDao createGenericDao(
            final SessionFactory sessionFactory,
            final TransactionTemplate transactionTemplate) throws Exception {

        return new GenericDao(sessionFactory, transactionTemplate);
    }

    @Bean(name = "cacheManager")
    public CacheManager createCacheManager(final GenericDao genericDao) {

        return new CacheManagerImpl(genericDao);
    }

    @Bean(name = "cacheManagerMBean")
    public CacheManagerMBean createCacheManagerMBean(final CacheManagerImpl cacheManager) {

        return new CacheManagerMBean(cacheManager);
    }
}
