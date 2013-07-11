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
package ch.algotrader.cache;

import java.util.Properties;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.event.CacheEventListenerAdapter;

import org.apache.log4j.Logger;

import ch.algotrader.ServiceLocator;
import ch.algotrader.util.MyLogger;

/**
 * EhCache CacheEventListenerFactory that creates a {@link CacheEventListener} which notifies when the UpdateTimestampsCache is being updated in the 2nd level cache.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class QueryCacheEventListenerFactory extends net.sf.ehcache.event.CacheEventListenerFactory {

    private static Logger logger = MyLogger.getLogger(QueryCacheEventListenerFactory.class.getName());

    @Override
    public CacheEventListener createCacheEventListener(Properties properties) {

        return new CacheEventListenerAdapter() {

            @Override
            public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException {

                CacheManagerImpl cacheManager = ServiceLocator.instance().getService("cacheManager", CacheManagerImpl.class);
                cacheManager.getQueryCache().detach((String) element.getKey());
            }

            @Override
            public void notifyElementRemoved(Ehcache cache, Element element) throws CacheException {

                // do nothing
            }

            @Override
            public void notifyElementExpired(Ehcache cache, Element element) {

                // do nothing
            }

            @Override
            public void notifyElementEvicted(Ehcache cache, Element element) {

                // do nothing
            }
        };
    }
}
