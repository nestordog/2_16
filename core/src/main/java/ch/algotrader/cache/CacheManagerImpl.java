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

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.algotrader.hibernate.GenericDao;
import ch.algotrader.util.MyLogger;

/**
 * Main implementation class of the Level-0 Cache
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CacheManagerImpl implements CacheManager {

    private static Logger logger = MyLogger.getLogger(CacheManagerImpl.class.getName());

    public static final String ROOT = "root";

    private AbstractHandler entityHandler;
    private AbstractHandler collectionHandler;

    private EntityCache entityCache;
    private QueryCache queryCache;

    private GenericDao genericDao;

    private CacheManagerImpl() {

        this.collectionHandler = new CollectionHandler(this);
        this.entityHandler = new EntityHandler(this);
        this.entityCache = new EntityCache();
        this.queryCache = new QueryCache();
    }

    public void setGenericDao(GenericDao genericDao) {
        this.genericDao = genericDao;
    }

    EntityCache getEntityCache() {
        return this.entityCache;
    }

    QueryCache getQueryCache() {
        return this.queryCache;
    }

    GenericDao getGenericDao() {
        return this.genericDao;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> clazz, Serializable key) {

        EntityCacheKey cacheKey = new EntityCacheKey(clazz, key);

        T result = (T) this.entityCache.find(cacheKey, ROOT);

        // load the Entity if it is not available in the Cache
        if (result == null) {

            // load the object from
            result = (T) this.genericDao.get(clazz, key);

            // put into the cache
            put(result);
        }

        return result;
    }

    @Override
    public List<?> query(String queryString) {

        return query(queryString, null);
    }

    @Override
    public List<?> query(String queryString, Map<String, Object> namedParameters) {

        QueryCacheKey cacheKey = new QueryCacheKey(queryString, namedParameters);

        List<?> result = this.queryCache.find(cacheKey);

        if (result == null) {

            // do the query
            if (namedParameters != null) {
                result = this.genericDao.find(queryString, namedParameters);
            } else {
                result = this.genericDao.find(queryString);
            }

            // get the spaceNames
            Set<String> spaceNames = this.genericDao.getQuerySpaces(cacheKey.getQueryString());

            // add the query to the queryCache
            this.queryCache.attach(cacheKey, spaceNames, result);

            // put the result (potentially replacing objects)
            put(result);
        }

        return result;
    }

    @Override
    public boolean contains(Class<?> clazz, Serializable key) {

        EntityCacheKey cacheKey = new EntityCacheKey(clazz, key);

        return this.entityCache.exists(cacheKey, ROOT);
    }

    /**
     * Adds an object recursively into the Cache and returns the existingObject if it was already in the Cache
     */
    Object put(Object obj) {

        AbstractHandler handler = getHandler(obj.getClass());

        return handler.put(obj);
    }

    /**
     * Invokes an update by using the Handlers
     */
    void update(EntityCacheKey cacheKey, String key) {

        Object obj = this.entityCache.find(cacheKey, key);

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
