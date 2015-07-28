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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.cache.spi.CacheKey;

/**
* EhCache CacheEventListener which notifies on Entities being updated in the 2nd level cache.
*
* @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
*
* @version $Revision$ $Date$
*/
public class EntityCacheEventListener extends CacheEventListenerAdapter {

    static final Logger LOGGER = LogManager.getLogger(EntityCacheEventListener.class);

    private CacheManagerImpl cacheManager;

    public EntityCacheEventListener(CacheManagerImpl cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException {

        CacheKey hibernateCacheKey = (CacheKey) element.getObjectKey();
        String entityOrRoleName = hibernateCacheKey.getEntityOrRoleName();
        if (entityOrRoleName.endsWith("Impl")) {
            updateEntity(hibernateCacheKey);
        } else {
            updateCollection(hibernateCacheKey);
        }
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

    private void updateEntity(CacheKey hibernateCacheKey) {

        String entityOrRoleName = hibernateCacheKey.getEntityOrRoleName();

        try {
            EntityCacheKey cacheKey = new EntityCacheKey(entityOrRoleName, hibernateCacheKey.getKey());
            this.cacheManager.update(cacheKey, CacheManagerImpl.ROOT);
        } catch (ClassNotFoundException e) {
            LOGGER.error("entityOrRoleName could not be found {}", entityOrRoleName);
        }
    }

    private void updateCollection(CacheKey hibernateCacheKey) {

        String entityOrRoleName = hibernateCacheKey.getEntityOrRoleName();
        int lastDot = entityOrRoleName.lastIndexOf(".");
        String entityName = entityOrRoleName.substring(0, lastDot);
        String key = entityOrRoleName.substring(lastDot + 1);

        try {
            EntityCacheKey cacheKey = new EntityCacheKey(entityName, hibernateCacheKey.getKey());
            this.cacheManager.update(cacheKey, key);
        } catch (ClassNotFoundException e) {
            LOGGER.error("entityOrRoleName could not be found {}", entityOrRoleName);
        }
    }
}