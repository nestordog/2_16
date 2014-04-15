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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import ch.algotrader.entity.BaseEntityI;
import ch.algotrader.entity.security.Security;
import ch.algotrader.hibernate.GenericDao;
import ch.algotrader.util.MyLogger;

/**
 * Main implementation class of the Level-0 Cache
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@ManagedResource(objectName = "ch.algotrader.cache:name=CacheManager")
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
            if (result != null) {
                put(result);
            }

            // make sure Securities are initialized (as they might have been put into the cache by the CollectionHandler)
        } else {

            if (result instanceof Security) {

                Security security = (Security) result;
                security.initialize();
            }
        }

        return result;
    }

    @Override
    public boolean contains(Class<?> clazz, Serializable key) {

        EntityCacheKey cacheKey = new EntityCacheKey(clazz, key);

        return this.entityCache.exists(cacheKey, ROOT);
    }

    @Override
    public Object initialze(BaseEntityI entity, String key) {

        EntityCacheKey cacheKey = new EntityCacheKey(entity);

        Object obj = this.entityCache.find(cacheKey, key);

        AbstractHandler handler = getHandler(obj.getClass());

        Object initializedObj = handler.initialize(obj);

        // if the key was already initialized do nothing
        if (initializedObj != null) {

            this.entityCache.attach(cacheKey, key, initializedObj);

            logger.trace("initialized " + cacheKey + ": " + key);
        }

        return initializedObj;
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

            if (handler.update(obj) != null) {

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

    // Management Operations

    @ManagedOperation
    @ManagedOperationParameters({})
    public void clear() {

        this.entityCache.clear();
        this.queryCache.clear();
    }

    @ManagedAttribute
    public Map<String, Integer> getCacheSize() {

        Map<String, Integer> numCached = new HashMap<String, Integer>();

        numCached.put("entities", this.entityCache.size());
        numCached.put("queries", this.queryCache.size());

        return numCached;
    }
}
