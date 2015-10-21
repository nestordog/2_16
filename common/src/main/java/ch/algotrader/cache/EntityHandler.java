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

import java.lang.reflect.Field;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

import ch.algotrader.cache.CacheResponse.CacheState;
import ch.algotrader.entity.BaseEntityI;
import ch.algotrader.util.FieldUtil;

/**
 * Cache Handler for Entities.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class EntityHandler extends AbstractHandler {

    private static final Logger LOGGER = LogManager.getLogger(EntityHandler.class);

    private final CacheManagerImpl cacheManager;

    public EntityHandler(CacheManagerImpl cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    protected CacheResponse put(Object obj, List<EntityCacheSubKey> stack) {

        // do not process HibernateProxies
        if (obj instanceof HibernateProxy) {
            return CacheResponse.proxyObject();
        }

        // check stack
        BaseEntityI entity = (BaseEntityI) obj;
        EntityCacheKey rootCacheKey = new EntityCacheKey(entity);
        EntityCacheSubKey subCacheKey = new EntityCacheSubKey(entity);
        if (stack.contains(subCacheKey)) {
            return CacheResponse.processedObject();
        } else {
            stack.add(subCacheKey);
        }

        synchronized (obj) {

            // check if the object is already exists in the cache
            if (this.cacheManager.getEntityCache().exists(rootCacheKey, EntityCacheKey.ROOT)) {

                Object existingObj = this.cacheManager.getEntityCache().find(rootCacheKey, EntityCacheKey.ROOT);
                if (obj != existingObj) {
                    processFieldsWithExisting(entity.getId(), obj, existingObj, stack);
                }
                return CacheResponse.existingObject(existingObj);

            } else {

                this.cacheManager.getEntityCache().attach(rootCacheKey, EntityCacheKey.ROOT, obj);
                processFields(entity.getId(), obj, stack);
                return CacheResponse.newObject();
            }
        }
    }

    private void processFieldsWithExisting(long entityId, Object obj, Object existingObj, List<EntityCacheSubKey> stack) {

        Validate.notNull(obj, "obj is null");
        Validate.notNull(existingObj, "existingObj is null");

        for (Field field : FieldUtil.getAllFields(obj.getClass())) {

            Object value = null;
            try {
                value = field.get(obj);
            } catch (Exception e) {
                LOGGER.error("problem getting field", e);
            }

            // nothing to do on simple attributes
            if (FieldUtil.isSimpleAttribute(field) || value == null) {
                continue;
            }

            // if the object already existed but does not have the same reference replace it
            CacheResponse response = this.cacheManager.put(value, stack);

            try {
                if (response.getState() == CacheState.EXISTING) {
                    Object existingValue = field.get(existingObj);
                    if (response.getValue() != existingValue) {

                        field.set(existingObj, response.getValue());

                        EntityCacheKey cacheKey = new EntityCacheKey(field.getDeclaringClass(), entityId);
                        this.cacheManager.getEntityCache().attach(cacheKey, field.getName(), response.getValue());
                    }
                } else if (response.getState() == CacheState.NEW) {
                    Object existingValue = field.get(existingObj);
                    if (existingValue != value) {

                        field.set(existingObj, value);

                        EntityCacheKey cacheKey = new EntityCacheKey(field.getDeclaringClass(), entityId);
                        this.cacheManager.getEntityCache().attach(cacheKey, field.getName(), value);
                    }
                }
            } catch (IllegalArgumentException e) {
                LOGGER.error("problem update field value", e);
            } catch (IllegalAccessException e) {
                LOGGER.error("problem update field value", e);
            }
        }
    }

    private void processFields(long entityId, Object obj, List<EntityCacheSubKey> stack) {

        Validate.notNull(obj, "obj is null");

        for (Field field : FieldUtil.getAllFields(obj.getClass())) {

            Object value = null;
            try {
                value = field.get(obj);
            } catch (Exception e) {
                LOGGER.error("problem getting field", e);
            }

            // nothing to do on simple attributes
            if (FieldUtil.isSimpleAttribute(field) || value == null) {
                continue;
            }

            // if the object already existed but does not have the same reference replace it
            CacheResponse response = this.cacheManager.put(value, stack);

            try {
                if (response.getState() == CacheState.EXISTING && response.getValue() != value) {
                    field.set(obj, response.getValue());
                }

                EntityCacheKey cacheKey = new EntityCacheKey(field.getDeclaringClass(), entityId);
                this.cacheManager.getEntityCache().attach(cacheKey, field.getName(), value);
            } catch (IllegalArgumentException e) {
                LOGGER.error("problem update field value", e);
            } catch (IllegalAccessException e) {
                LOGGER.error("problem update field value", e);
            }
        }
    }

    @Override
    protected CacheResponse update(Object obj) {

        synchronized (obj) {

            // get the updatedObj
            BaseEntityI origEntity = (BaseEntityI) obj;
            BaseEntityI updatedEntity = this.cacheManager.getGenericDao().get(origEntity.getClass(), origEntity.getId());

            // updatedObj does not exist anymore so remove it from the cache
            if (updatedEntity == null) {

                EntityCacheKey cacheKey = new EntityCacheKey(origEntity);
                this.cacheManager.getEntityCache().detach(cacheKey);

                return CacheResponse.removedObject();

            } else {

                // replace all simple Attributes
                for (Field field : FieldUtil.getAllFields(obj.getClass())) {

                    if (FieldUtil.isSimpleAttribute(field)) {

                        try {
                            Object updatedValue = field.get(updatedEntity);
                            Object origValue = field.get(origEntity);

                            if (updatedValue == null) {
                                if (origValue != null) {
                                    field.set(origEntity, null);
                                }
                            } else {
                                if (!updatedValue.equals(origValue)) {
                                    field.set(origEntity, updatedValue);
                                }
                            }
                        } catch (Exception e) {
                            LOGGER.error("problem accessing field", e);
                        }
                    }
                }

                return CacheResponse.updatedObject(updatedEntity);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected CacheResponse initialize(Object obj) {

        if (!(obj instanceof HibernateProxy)) {
            throw new IllegalArgumentException("none HibernateProxy passed " + obj);
        }

        synchronized (obj) {

            HibernateProxy proxy = (HibernateProxy) obj;
            LazyInitializer initializer = proxy.getHibernateLazyInitializer();
            Object initializedObj = this.cacheManager.getGenericDao().get(initializer.getPersistentClass(), (Long) initializer.getIdentifier());

            CacheResponse response = this.cacheManager.put(initializedObj);

            if (response.getState() == CacheState.EXISTING) {
                return response;
            } else {
                return CacheResponse.updatedObject(initializedObj);
            }
        }
    }

    @Override
    protected boolean handles(Class<?> clazz) {
        return BaseEntityI.class.isAssignableFrom(clazz);
    }
}
