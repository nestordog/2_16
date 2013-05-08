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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.algoTrader.util.MyLogger;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
class QueryCache {

    private static Logger logger = MyLogger.getLogger(EntityCache.class.getName());

    private Map<String, Set<QueryCacheKey>> spaces;
    private Map<QueryCacheKey, List<?>> results;

    QueryCache() {

        this.spaces = new HashMap<String, Set<QueryCacheKey>>();
        this.results = new HashMap<QueryCacheKey, List<?>>();
    }

    void attach(QueryCacheKey cacheKey, Set<String> spaceNames, List<?> result) {

        // add all spaces
        for (String spaceName : spaceNames) {

            Set<QueryCacheKey> space = this.spaces.get(spaceName);
            if (space == null) {

                space = new HashSet<QueryCacheKey>();
                this.spaces.put(spaceName, space);
            }
            space.add(cacheKey);
        }

        // put the query itself
        this.results.put(cacheKey, result);

        logger.trace("attached " + cacheKey);
    }

    List<?> find(QueryCacheKey cacheKey) {

        return this.results.get(cacheKey);
    }

    void detach(String spaceName) {

        Set<QueryCacheKey> space = this.spaces.get(spaceName);

        if (space != null) {

            for (QueryCacheKey cacheKey : space) {

                // remove the query
                this.results.remove(cacheKey);

                // remove the query also from all other spaces
                for (Map.Entry<String, Set<QueryCacheKey>> entry : this.spaces.entrySet()) {

                    entry.getValue().remove(cacheKey);
                }
                logger.trace("detached " + cacheKey);
            }
        }
    }
}
