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
package ch.algotrader.wiring.server;

import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

import ch.algotrader.cache.CacheManager;
import ch.algotrader.cache.CacheManagerImpl;
import ch.algotrader.dao.GenericDao;
import ch.algotrader.dao.GenericDaoImpl;
import ch.algotrader.ehcache.CollectionCacheEventListener;
import ch.algotrader.ehcache.EntityCacheEventListener;
import ch.algotrader.ehcache.QueryCacheEventListener;
import ch.algotrader.event.dispatch.EventDispatcher;
import net.sf.ehcache.Cache;
import net.sf.ehcache.event.RegisteredEventListeners;

/**
 * Cache configuration.
 */
@Configuration
public class CacheWiring {

    @Bean(name = "genericDao")
    public GenericDao createGenericDao(
            final SessionFactory sessionFactory,
            final TransactionTemplate transactionTemplate) throws Exception {

        return new GenericDaoImpl(sessionFactory, transactionTemplate);
    }

    @Bean(name = "cacheManager")
    public CacheManager createCacheManager(final GenericDao genericDao, final EventDispatcher eventDispatcher) throws ClassNotFoundException {

        // register CacheEventListeners
        net.sf.ehcache.CacheManager ehCacheManager = net.sf.ehcache.CacheManager.getInstance();

        for (String cacheName : ehCacheManager.getCacheNames()) {
            Cache cache = ehCacheManager.getCache(cacheName);
            RegisteredEventListeners eventListeners = ehCacheManager.getCache(cacheName).getCacheEventNotificationService();

            String name = cache.getName();
            if (name.startsWith("ch.algotrader.entity")) {
                if (name.endsWith("Impl")) {
                    Class<?> entityClass = Class.forName(name);
                    eventListeners.registerListener(new EntityCacheEventListener(eventDispatcher, entityClass));
                } else {
                    int lastDot = name.lastIndexOf(".");
                    String entityName = name.substring(0, lastDot);
                    String roleName = name.substring(lastDot + 1);
                    Class<?> entityClass = Class.forName(entityName);
                    eventListeners.registerListener(new CollectionCacheEventListener(eventDispatcher, entityClass, roleName));
                }
            } else if (name.equals("org.hibernate.cache.spi.UpdateTimestampsCache")) {
                eventListeners.registerListener(new QueryCacheEventListener(eventDispatcher));
            }
        }

        // create the CacheManager
        return new CacheManagerImpl(genericDao);
    }

}
