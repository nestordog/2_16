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

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.algoTrader.util.MyLogger;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CacheManager {

    private static Logger logger = MyLogger.getLogger(CacheManager.class.getName());

    public static final String ROOT = "root";

    private static CacheManager instance;

    private EntityHandler entityHandler;
    private CollectionHandler collectionHandler;
    private HashMapCache cache = new HashMapCache();

    private CacheManager() {

        this.collectionHandler = new CollectionHandler(this, this.cache);
        this.entityHandler = new EntityHandler(this, this.cache);
    }

    public static CacheManager getInstance() {

        if (instance == null) {
            instance = new CacheManager();
        }

        return instance;
    }

    /**
     * Adds an object recursively into the Cache and returns the existingObject if it was already in the Cache
     */
    public Object put(Object obj) {

        AbstractHandler handler = getHandler(obj.getClass());

        return handler.put(obj);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> clazz, Serializable key) {

        CacheKey cacheKey = new CacheKey(clazz, key);

        return (T) this.cache.find(cacheKey, ROOT);
    }

    public boolean contains(Class<?> clazz, Serializable key) {

        CacheKey cacheKey = new CacheKey(clazz, key);

        return this.cache.exists(cacheKey, ROOT);
    }

    /**
     * Invokes an update by using the Handlers
     */
    void update(CacheKey cacheKey, String key) {

        Object obj = this.cache.find(cacheKey, key);

        if (obj != null) {

            AbstractHandler handler = getHandler(obj.getClass());

            if (handler.update(obj)) {

                logger.trace("updated " + cacheKey + ": " + key);
            }
        }
    }

    private AbstractHandler getHandler(Class<?> clazz) {

        if (this.entityHandler.handles(clazz))
            return this.entityHandler;

        if (this.collectionHandler.handles(clazz))
            return this.collectionHandler;

        throw new IllegalArgumentException("Can not manage object " + clazz.getName());
    }
}
