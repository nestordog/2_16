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
package ch.algotrader.cache;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListenerAdapter;

/**
 * EhCache CacheEventListener which notifies when the UpdateTimestampsCache is being updated in the 2nd level cache.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class QueryCacheEventListener extends CacheEventListenerAdapter {

    private CacheManagerImpl cacheManager;

    public QueryCacheEventListener(CacheManagerImpl cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException {

        this.cacheManager.getQueryCache().detach((String) element.getKey());
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
}