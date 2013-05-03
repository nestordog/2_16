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

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.IdentifiableI;
import com.algoTrader.util.TypeUtil;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class EntityHandler extends AbstractHandler {

    private CacheManager cacheManager;
    private HashMapCache cache;

    public EntityHandler(CacheManager cacheManager, HashMapCache cache) {
        this.cacheManager = cacheManager;
        this.cache = cache;
    }

    @Override
    protected Object put(Object obj) {

        // do not process HibernateProxies
        if (obj instanceof HibernateProxy) {
            return null;
        }

        // check if the IdentifiableI already exists in the cache
        IdentifiableI identifiable = (IdentifiableI) obj;
        CacheKey rootCacheKey = new CacheKey(identifiable);
        Object existingObj = null;
        if (this.cache.exists(rootCacheKey, CacheManager.ROOT)) {

            existingObj = this.cache.find(rootCacheKey, CacheManager.ROOT);

        } else {

            // attach the object itself
            this.cache.attach(rootCacheKey, CacheManager.ROOT, obj);

            // process all fields
            for (Field field : TypeUtil.getAllFields(obj.getClass())) {

                Object value = null;
                try {
                    value = field.get(obj);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (TypeUtil.isSimpleAttribute(field) || value == null) {
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

                CacheKey cacheKey = new CacheKey(field.getDeclaringClass(), identifiable.getId());
                this.cache.attach(cacheKey, field.getName(), value);
            }
        }

        return existingObj;
    }

    @Override
    protected boolean update(Object obj) {

        GenericDao genericDao = ServiceLocator.instance().getService("genericDao", GenericDao.class);

        // get the updatedObj
        IdentifiableI origObj = (IdentifiableI) obj;
        IdentifiableI updatedObj = (IdentifiableI) genericDao.get(obj.getClass(), origObj.getId());

        if (updatedObj == null) {

            CacheKey cacheKey = new CacheKey(origObj);
            this.cache.detach(cacheKey);

            // update was not successfull
            return false;

        } else {

            // replace all simple Attributes
            for (Field field : TypeUtil.getAllFields(obj.getClass())) {

                if (TypeUtil.isSimpleAttribute(field)) {

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
