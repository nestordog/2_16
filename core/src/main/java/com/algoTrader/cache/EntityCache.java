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
package com.algoTrader.cache;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.algoTrader.util.MyLogger;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
class EntityCache extends HashMap<EntityCacheKey, Map<String, Object>> {

    private static final long serialVersionUID = -3242571286067822619L;

    private static Logger logger = MyLogger.getLogger(EntityCache.class.getName());

    void attach(EntityCacheKey cacheKey, String key, Object value) {

        Map<String, Object> entry = get(cacheKey);

        if (entry == null) {
            entry = new HashMap<String, Object>();
            put(cacheKey, entry);
        }

        entry.put(key, value);

        logger.trace("attached " + cacheKey + ": " + key);
    }

    boolean exists(EntityCacheKey cacheKey, String key) {

        Map<String, Object> entry = get(cacheKey);
        if (entry == null) {
            return false;
        } else {
            return entry.containsKey(key);
        }
    }

    Object find(EntityCacheKey cacheKey, String key) {

        Map<String, Object> entry = get(cacheKey);
        if (entry == null) {
            return null;
        } else {
            return entry.get(key);
        }
    }

    void detach(EntityCacheKey cacheKey) {

        remove(cacheKey);

        logger.trace("detached " + cacheKey);
    }
}
