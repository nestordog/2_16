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

import java.lang.reflect.Field;

import org.hibernate.proxy.HibernateProxy;

import com.algoTrader.entity.IdentifiableI;
import com.algoTrader.util.FieldUtil;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class EntityHandler extends AbstractHandler {

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

        // check if the IdentifiableI already exists in the cache
        IdentifiableI identifiable = (IdentifiableI) obj;
        EntityCacheKey rootCacheKey = new EntityCacheKey(identifiable);
        Object existingObj = null;
        if (this.cacheManager.getEntityCache().exists(rootCacheKey, CacheManagerImpl.ROOT)) {

            existingObj = this.cacheManager.getEntityCache().find(rootCacheKey, CacheManagerImpl.ROOT);

        } else {

            // attach the object itself
            this.cacheManager.getEntityCache().attach(rootCacheKey, CacheManagerImpl.ROOT, obj);

            // process all fields
            for (Field field : FieldUtil.getAllFields(obj.getClass())) {

                Object value = null;
                try {
                    value = field.get(obj);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (FieldUtil.isSimpleAttribute(field) || value == null) {
                    continue;
                }

                // if the object already existed but does not have the same reference replace it
                Object existingValue = this.cacheManager.put(value);
                if (existingValue != null && existingValue != value) {

                    try {
                        field.set(obj, existingValue);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                EntityCacheKey cacheKey = new EntityCacheKey(field.getDeclaringClass(), identifiable.getId());
                this.cacheManager.getEntityCache().attach(cacheKey, field.getName(), value);
            }
        }

        return existingObj;
    }

    @Override
    protected boolean update(Object obj) {

        // get the updatedObj
        IdentifiableI origObj = (IdentifiableI) obj;
        IdentifiableI updatedObj = (IdentifiableI) this.cacheManager.getGenericDao().get(obj.getClass(), origObj.getId());

        if (updatedObj == null) {

            EntityCacheKey cacheKey = new EntityCacheKey(origObj);
            this.cacheManager.getEntityCache().detach(cacheKey);

            // update was not successfull
            return false;

        } else {

            // replace all simple Attributes
            for (Field field : FieldUtil.getAllFields(obj.getClass())) {

                if (FieldUtil.isSimpleAttribute(field)) {

                    try {
                        Object updatedValue = field.get(updatedObj);
                        Object origValue = field.get(origObj);

                        if (updatedValue == null) {
                            if (origValue != null) {
                                field.set(origObj, null);
                            }
                        } else {
                            if (!updatedValue.equals(origValue)) {
                                field.set(origObj, updatedValue);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            return true;
        }
    }

    @Override
    protected boolean handles(Class<?> clazz) {
        return IdentifiableI.class.isAssignableFrom(clazz);
    }
}
