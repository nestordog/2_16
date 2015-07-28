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

import net.sf.ehcache.Cache;
import net.sf.ehcache.event.RegisteredEventListeners;

import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

import ch.algotrader.cache.CacheManager;
import ch.algotrader.cache.CacheManagerImpl;
import ch.algotrader.cache.CacheManagerMBean;
import ch.algotrader.cache.EntityCacheEventListener;
import ch.algotrader.cache.QueryCacheEventListener;
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

        CacheManagerImpl cacheManager = new CacheManagerImpl(genericDao);

        // register CacheEventListeners
        net.sf.ehcache.CacheManager ehCacheManager = net.sf.ehcache.CacheManager.getInstance();

        for (String cacheName : ehCacheManager.getCacheNames()) {
            Cache cache = ehCacheManager.getCache(cacheName);
            RegisteredEventListeners cacheEventNotificationService = ehCacheManager.getCache(cacheName).getCacheEventNotificationService();
            if (cache.getName().startsWith("ch.algotrader.entity")) {
                cacheEventNotificationService.registerListener(new EntityCacheEventListener(cacheManager));
            } else if (cache.getName().equals("org.hibernate.cache.spi.UpdateTimestampsCache")) {
                cacheEventNotificationService.registerListener(new QueryCacheEventListener(cacheManager));
            }
        }

        return cacheManager;
    }

    @Bean(name = "cacheManagerMBean")
    public CacheManagerMBean createCacheManagerMBean(final CacheManagerImpl cacheManager) {

        return new CacheManagerMBean(cacheManager);
    }
}
