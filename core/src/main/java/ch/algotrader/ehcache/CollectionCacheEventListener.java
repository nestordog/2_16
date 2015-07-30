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
package ch.algotrader.ehcache;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListenerAdapter;

import org.hibernate.cache.spi.CacheKey;

import ch.algotrader.cache.EntityCacheEvictionEvent;
import ch.algotrader.event.dispatch.EventDispatcher;

/**
* EhCache CacheEventListener which notifies on Entity Collections being updated in the 2nd level cache.
*
* @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
*
* @version $Revision$ $Date$
*/
public class CollectionCacheEventListener extends CacheEventListenerAdapter {

    private final EventDispatcher eventDispatcher;
    private final Class<?> entityClass;
    private final String roleName;

    public CollectionCacheEventListener(EventDispatcher eventDispatcher, Class<?> entityClass, String roleName) {
        this.eventDispatcher = eventDispatcher;
        this.entityClass = entityClass;
        this.roleName = roleName;
    }

    @Override
    public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException {

        CacheKey hibernateCacheKey = (CacheKey) element.getObjectKey();
        long id = (Long) hibernateCacheKey.getKey();

        EntityCacheEvictionEvent event = new EntityCacheEvictionEvent(this.entityClass, id, this.roleName);
        this.eventDispatcher.broadcastEventListeners(event);
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