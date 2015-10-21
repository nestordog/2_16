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

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.collection.spi.PersistentCollection;

import ch.algotrader.entity.BaseEntityI;

/**
 * Cache for Entities based on a HashMap.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
class EntityCache {

    private static final Logger LOGGER = LogManager.getLogger(EntityCache.class);

    private Map<EntityCacheKey, Map<String, Object>> entries = new HashMap<EntityCacheKey, Map<String, Object>>();

    /**
     * attaches an object to the cache
     */
    void attach(EntityCacheKey cacheKey, String key, Object value) {

        if (!(value instanceof BaseEntityI) && !(value instanceof PersistentCollection)) {
            throw new IllegalArgumentException("object is neither of type BaseEntityI nor PersistentCollection " + value);
        }

        Map<String, Object> entry = this.entries.get(cacheKey);

        if (entry == null) {
            entry = new HashMap<>();
            this.entries.put(cacheKey, entry);
        }

        entry.put(key, value);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("attached {}: {}", cacheKey, key);
        }
    }

    /**
     * checks if an object exists in the cache
     */
    boolean exists(EntityCacheKey cacheKey, String key) {

        Map<String, Object> entry = this.entries.get(cacheKey);
        if (entry == null) {
            return false;
        } else {
            return entry.containsKey(key);
        }
    }

    /**
     * returns an object from the cache or null if the object was not cached
     */
    Object find(EntityCacheKey cacheKey, String key) {

        Map<String, Object> entry = this.entries.get(cacheKey);
        if (entry == null) {
            return null;
        } else {
            return entry.get(key);
        }
    }

    /**
     * detaches an object from the cache
     */
    void detach(EntityCacheKey cacheKey) {

        this.entries.remove(cacheKey);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("detached {}", cacheKey);
        }
    }

    public void clear() {
        this.entries.clear();
    }

    public int size() {
        return this.entries.size();
    }
}
