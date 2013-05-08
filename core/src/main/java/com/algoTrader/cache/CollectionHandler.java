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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.hibernate.collection.AbstractPersistentCollection;
import org.hibernate.collection.PersistentCollection;

import com.algoTrader.entity.IdentifiableI;
import com.algoTrader.util.TypeUtil;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
class CollectionHandler extends AbstractHandler {

    private CacheManagerImpl cacheManager;

    public CollectionHandler(CacheManagerImpl cacheManager) {
        this.cacheManager = cacheManager;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected Object put(Object obj) {

        // do not process uninitialized PersistenCollections
        if (obj instanceof AbstractPersistentCollection && !((AbstractPersistentCollection) obj).wasInitialized()) {
            return null;
        }

        if (obj instanceof Map) {

            Map map = (Map) obj;
            for (Iterator i = map.entrySet().iterator(); i.hasNext();) {

                Map.Entry entry = (Map.Entry) i.next();
                Object existingValue = this.cacheManager.put(entry.getValue());
                if (existingValue != null && existingValue != entry.getValue()) {

                    // replace the value with the existingValue
                    entry.setValue(existingValue);
                }
            }

        } else if (obj instanceof List) {

            List list = (List) obj;
            for (ListIterator it = list.listIterator(); it.hasNext();) {

                Object value = it.next();
                Object existingValue = this.cacheManager.put(value);
                if (existingValue != null && existingValue != value) {

                    // replace the value with the existingValue
                    it.set(existingValue);
                }
            }

        } else if (obj instanceof Set) {

            Set set = (Set) obj;
            Set replacements = new HashSet();
            for (Iterator it = set.iterator(); it.hasNext();) {

                Object value = it.next();
                Object existingValue = this.cacheManager.put(value);
                if (existingValue != null && existingValue != value) {
                    replacements.add(value);
                }
            }

            // need to replace the values outside the loop to prevent java.util.ConcurrentModificationException
            set.removeAll(replacements);
            set.addAll(replacements);
        }

        return null;
    }

    @Override
    protected boolean update(Object obj) {

        if (!(obj instanceof PersistentCollection)) {
            throw new IllegalArgumentException("PersistentCollection needed");
        }

        PersistentCollection origCollection = (PersistentCollection) obj;

        Object updatedObj = this.cacheManager.getGenericDao().getInitializedCollection(origCollection.getRole(), origCollection.getKey());

        // owner does not exist anymore so remove it
        if (updatedObj == null) {

            Object owner = origCollection.getOwner();
            EntityCacheKey cacheKey = new EntityCacheKey((IdentifiableI) owner);
            this.cacheManager.getEntityCache().detach(cacheKey);

            // update was not successfull
            return false;

        } else {

            if (!(updatedObj instanceof PersistentCollection)) {

                if (updatedObj instanceof Collection) {
                    Collection<?> col = (Collection<?>) updatedObj;
                    if (col.size() != 0) {
                        System.out.println("not empty collection returned instead of PersistentCollection");
                    }
                } else if (updatedObj instanceof Map) {
                    Map<?, ?> map = (Map<?, ?>) updatedObj;
                    if (map.size() != 0) {
                        System.out.println("not empty map returned instead of PersistentCollection");
                    }
                }

                return false;
            }

            PersistentCollection updatedCol = (PersistentCollection) updatedObj;
            TypeUtil.copyAllFields(origCollection, updatedCol);

            // make sure everything is in the cache
            this.cacheManager.put(origCollection);

            return true;
        }
    }

    @Override
    protected boolean handles(Class<?> clazz) {
        return Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz);
    }
}
