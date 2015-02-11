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

import org.apache.log4j.Logger;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

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

    private static Logger logger = Logger.getLogger(EntityHandler.class.getName());

    private CacheManagerImpl cacheManager;

    public EntityHandler(CacheManagerImpl cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    protected Object put(Object obj) {

        // do not process HibernateProxies
        if (obj instanceof HibernateProxy) {
            return null;
        }

        synchronized (obj) {

            // check if the IdentifiableI already exists in the cache
            BaseEntityI entity = (BaseEntityI) obj;
            EntityCacheKey rootCacheKey = new EntityCacheKey(entity);
            Object existingObj = null;
            if (this.cacheManager.getEntityCache().exists(rootCacheKey, CacheManagerImpl.ROOT)) {

                existingObj = this.cacheManager.getEntityCache().find(rootCacheKey, CacheManagerImpl.ROOT);

                // IndentifiableI is not in the cache already
            } else {

                // attach the object itself
                this.cacheManager.getEntityCache().attach(rootCacheKey, CacheManagerImpl.ROOT, obj);

                // process all fields
                for (Field field : FieldUtil.getAllFields(obj.getClass())) {

                    Object value = null;
                    try {
                        value = field.get(obj);
                    } catch (Exception e) {
                        logger.error("problem getting field", e);
                    }

                    // nothing to do on simplate attributes
                    if (FieldUtil.isSimpleAttribute(field) || value == null) {
                        continue;
                    }

                    // if the object already existed but does not have the same reference replace it
                    Object existingValue = this.cacheManager.put(value);
                    if (existingValue != null && existingValue != value) {

                        try {
                            field.set(obj, existingValue);
                        } catch (Exception e) {
                            logger.error("problem setting field", e);
                        }
                    }

                    EntityCacheKey cacheKey = new EntityCacheKey(field.getDeclaringClass(), entity.getId());
                    this.cacheManager.getEntityCache().attach(cacheKey, field.getName(), value);
                }
            }
            return existingObj;
        }
    }

    @Override
    protected Object update(Object obj) {

        synchronized (obj) {

            // get the updatedObj
            BaseEntityI origEntity = (BaseEntityI) obj;
            BaseEntityI updatedEntity = (BaseEntityI) this.cacheManager.getGenericDao().get(obj.getClass(), origEntity.getId());
            // updatedObj does not exist anymore so remove it from the cache
            if (updatedEntity == null) {

                EntityCacheKey cacheKey = new EntityCacheKey(origEntity);
                this.cacheManager.getEntityCache().detach(cacheKey);

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
                            logger.error("problem accessing field", e);
                        }
                    }
                }
            }
            return updatedEntity;
        }
    }

    @Override
    protected Object initialize(Object obj) {

        if (!(obj instanceof HibernateProxy)) {

            return null;
        }

        synchronized (obj) {

            HibernateProxy proxy = (HibernateProxy) obj;
            LazyInitializer initializer = proxy.getHibernateLazyInitializer();
            Object initializedObj = this.cacheManager.getGenericDao().get(initializer.getPersistentClass(), initializer.getIdentifier());
            Object existingObj = put(initializedObj);

            // return the exstingObj if it was already in the cache otherwise the newly initialized obj
            return existingObj != null ? existingObj : initializedObj;
        }
    }

    @Override
    protected boolean handles(Class<?> clazz) {
        return BaseEntityI.class.isAssignableFrom(clazz);
    }
}
