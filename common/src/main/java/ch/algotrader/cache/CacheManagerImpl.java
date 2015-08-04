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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ClassUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.collection.internal.AbstractPersistentCollection;
import org.hibernate.proxy.HibernateProxy;

import ch.algotrader.dao.GenericDao;
import ch.algotrader.dao.NamedParam;
import ch.algotrader.entity.BaseEntityI;
import ch.algotrader.entity.Initializer;
import ch.algotrader.enumeration.QueryType;
import ch.algotrader.event.listener.EntityCacheEventListener;
import ch.algotrader.event.listener.QueryCacheEventListener;
import ch.algotrader.util.collection.CollectionUtil;
import ch.algotrader.util.metric.MetricsUtil;
import ch.algotrader.visitor.InitializationVisitor;

/**
 * Main implementation class of the Level-0 Cache
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CacheManagerImpl implements CacheManager, Initializer, EntityCacheEventListener, QueryCacheEventListener {

    private static final Logger LOGGER = LogManager.getLogger(CacheManagerImpl.class);

    public static final String ROOT = "root";

    private final AbstractHandler entityHandler;
    private final AbstractHandler collectionHandler;

    private final EntityCache entityCache;
    private final QueryCache queryCache;

    private final GenericDao genericDao;

    private final Map<String, String> queryStringMap;

    private final Map<Class<?>, Integer> discriminatorValueMap;

    public CacheManagerImpl(GenericDao genericDao) {

        this.collectionHandler = new CollectionHandler(this);
        this.entityHandler = new EntityHandler(this);
        this.entityCache = new EntityCache();
        this.queryCache = new QueryCache();
        this.genericDao = genericDao;
        this.queryStringMap = new HashMap<>();
        this.discriminatorValueMap = new HashMap<>();
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
    public <T extends BaseEntityI> T get(Class<T> clazz, long id) {

        EntityCacheKey cacheKey = new EntityCacheKey(clazz, id);

        T entity = clazz.cast(this.entityCache.find(cacheKey, ROOT));

        // load the Entity if it is not available in the Cache
        if (entity == null) {

            // load the object from the database
            entity = clazz.cast(this.genericDao.get(clazz, id));

            // put into the cache
            if (entity != null) {
                put(entity);
            }

            // make sure Securities are initialized (as they might have been put into the cache by the CollectionHandler)
        } else {

            // make sure the entity is initialized
            entity.accept(InitializationVisitor.INSTANCE, this);
        }

        return entity;
    }

    @Override
    public <T extends BaseEntityI> List<T> getAll(Class<T> clazz) {

        String className = clazz.getSimpleName();
        if (!className.endsWith("Impl")) {
            className = className + "Impl";
        }
        return find(clazz, "from " + className, QueryType.HQL);
    }

    @Override
    public Object put(Object obj) {

        AbstractHandler handler = getHandler(obj.getClass());

        return handler.put(obj);
    }

    @Override
    public <T extends BaseEntityI> boolean contains(Class<T> clazz, long id) {

        EntityCacheKey cacheKey = new EntityCacheKey(clazz, id);

        return this.entityCache.exists(cacheKey, ROOT);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BaseEntityI> T initializeProxy(BaseEntityI entity, String context, T proxy) {

        if (proxy instanceof HibernateProxy) {

            long before = System.nanoTime();
            proxy = (T) this.initialize(entity, context);
            MetricsUtil.account(ClassUtils.getShortClassName(entity.getClass()) + context, (before));
        }

        return proxy;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BaseEntityI, C extends Collection<T>> C initializeCollection(BaseEntityI entity, String context, C col) {

        if (col instanceof AbstractPersistentCollection && !((AbstractPersistentCollection) col).wasInitialized()) {

            long before = System.nanoTime();
            col = (C) this.initialize(entity, context);
            MetricsUtil.account(ClassUtils.getShortClassName(entity.getClass()) + context, (before));
        }

        return col;
    }

    private Object initialize(BaseEntityI entity, String key) {

        EntityCacheKey cacheKey = new EntityCacheKey(entity);

        Object obj = this.entityCache.find(cacheKey, key);

        AbstractHandler handler = getHandler(obj.getClass());

        Object initializedObj = handler.initialize(obj);

        // if the key was already initialized do nothing
        if (initializedObj != null) {

            this.entityCache.attach(cacheKey, key, initializedObj);

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("initialized {}: {}", cacheKey, key);
            }
        }

        return initializedObj;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> find(Class<T> clazz, String query, QueryType type, NamedParam... namedParams) {

        String queryString = getQueryString(query, type);

        QueryCacheKey cacheKey = new QueryCacheKey(queryString, namedParams);

        List<T> result = (List<T>) this.queryCache.find(cacheKey);

        if (result == null) {

            result = this.genericDao.find(clazz, queryString, QueryType.HQL, namedParams);

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
    public <T> T findUnique(Class<T> clazz, String query, QueryType type, NamedParam... namedParams) {
        return CollectionUtil.getSingleElementOrNull(find(clazz, query, type, namedParams));
    }

    /**
     * Invokes an update by using the Handlers
     */
    public void update(EntityCacheKey cacheKey, String key) {

        Object obj = this.entityCache.find(cacheKey, key);

        if (obj != null) {

            AbstractHandler handler = getHandler(obj.getClass());

            if (handler.update(obj) != null) {

                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("updated {}: {}", cacheKey, key);
                }
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

    private String getQueryString(String query, QueryType type) {

        switch (type) {
            case HQL:
                return query;
            case BY_NAME:
                String queryString = this.queryStringMap.get(query);
                if (queryString == null) {
                    queryString = this.genericDao.getNamedQuery(query);
                    this.queryStringMap.put(query, queryString);
                }
                return queryString;

            default:
                throw new IllegalStateException("Unexpected query type: " + type);
        }
    }

    @Override
    public void clear() {

        this.entityCache.clear();
        this.queryCache.clear();
    }

    @Override
    public int getDiscriminatorValue(final Class<?> type) {

        Integer discriminator = this.discriminatorValueMap.get(type);
        if (discriminator == null) {
            discriminator = this.genericDao.getDiscriminatorValue(type);
            this.discriminatorValueMap.put(type, discriminator);
        }
        return discriminator;
    }

    @Override
    public Map<String, Integer> getCacheSize() {

        Map<String, Integer> numCached = new HashMap<>();

        numCached.put("entities", this.entityCache.size());
        numCached.put("queries", this.queryCache.size());

        return numCached;
    }

    @Override
    public void onEvent(QueryCacheEvictionEvent event) {
        this.queryCache.detach(event.getSpaceName());
    }

    @Override
    public void onEvent(EntityCacheEvictionEvent event) {
        EntityCacheKey cacheKey = new EntityCacheKey(event.getEntityClass(), event.getId());
        update(cacheKey, event.getKey());
    }
}
