/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.cache;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.collection.internal.AbstractPersistentCollection;
import org.hibernate.collection.spi.PersistentCollection;

import ch.algotrader.cache.CacheResponse.CacheState;
import ch.algotrader.entity.BaseEntityI;
import ch.algotrader.util.FieldUtil;

/**
 * Cache Handler for Collections.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
class CollectionHandler extends AbstractHandler {

    private static final Logger LOGGER = LogManager.getLogger(CollectionHandler.class);

    private final CacheManagerImpl cacheManager;

    public CollectionHandler(CacheManagerImpl cacheManager) {
        this.cacheManager = cacheManager;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected CacheResponse put(Object obj, List<EntityCacheSubKey> stack) {

        if (obj instanceof AbstractPersistentCollection) {
            AbstractPersistentCollection col = (AbstractPersistentCollection) obj;

            // do not process uninitialized PersistentCollections
            if (!col.wasInitialized()) {
                return CacheResponse.skippedObject();
            }

            // check stack on Persistent Collections with role only
            if (col.getRole() != null) {
                EntityCacheSubKey cacheKey = new EntityCacheSubKey((BaseEntityI) col.getOwner(), col.getRole());
                if (stack.contains(cacheKey)) {
                    return CacheResponse.processedObject();
                } else {
                    stack.add(cacheKey);
                }
            }
        }

        synchronized (obj) {

            // process Maps
            if (obj instanceof Map) {

                Map map = (Map) obj;
                for (Object o : map.entrySet()) {

                    Map.Entry entry = (Map.Entry) o;
                    Object value = entry.getValue();
                    CacheResponse response = this.cacheManager.put(value, stack);
                    if (response.getState() == CacheState.EXISTING && response.getValue() != value) {

                        // replace the value with the existingValue
                        entry.setValue(response.getValue());
                    }
                }

                // process Lists
            } else if (obj instanceof List) {

                List list = (List) obj;
                for (ListIterator it = list.listIterator(); it.hasNext();) {

                    Object value = it.next();
                    CacheResponse response = this.cacheManager.put(value, stack);
                    if (response.getState() == CacheState.EXISTING && response.getValue() != value) {

                        // replace the value with the existingValue
                        it.set(response.getValue());
                    }
                }

                // process Sets
            } else if (obj instanceof Set) {

                Set set = (Set) obj;
                Set replacements = new HashSet();
                for (Object value : set) {

                    CacheResponse response = this.cacheManager.put(value, stack);
                    if (response.getState() == CacheState.EXISTING && response.getValue() != value) {
                        replacements.add(value);
                    }
                }

                // need to replace the values outside the loop to prevent java.util.ConcurrentModificationException
                set.removeAll(replacements);
                set.addAll(replacements);

            } else {
                throw new IllegalArgumentException("unsupported collection type " + obj.getClass());
            }
        }
        return CacheResponse.newObject();
    }

    @Override
    protected CacheResponse update(Object obj) {

        if (!(obj instanceof PersistentCollection)) {
            throw new IllegalArgumentException("none PersistentCollection passed " + obj);
        }

        PersistentCollection origCollection = (PersistentCollection) obj;

        // sometimes there is no role so collection initialization will not work
        if (origCollection.getRole() == null) {
            return CacheResponse.skippedObject();
        }

        synchronized (obj) {

            Object updatedCollection = this.cacheManager.getGenericDao().getInitializedCollection(origCollection.getRole(), (Long) origCollection.getKey());

            // owner does not exist anymore so remove it
            if (updatedCollection == null) {

                Object owner = origCollection.getOwner();
                EntityCacheKey cacheKey = new EntityCacheKey((BaseEntityI) owner);
                this.cacheManager.getEntityCache().detach(cacheKey);

                return CacheResponse.removedObject();

            } else {

                if (updatedCollection instanceof PersistentCollection) {

                    PersistentCollection updatedCol = (PersistentCollection) updatedCollection;
                    FieldUtil.copyAllFields(origCollection, updatedCol);

                    // make sure everything is in the cache
                    this.cacheManager.put(origCollection);

                    // getInitializedCollection should normally return a PersistentCollection
                } else {

                    if (updatedCollection instanceof Collection) {
                        Collection<?> col = (Collection<?>) updatedCollection;
                        if (col.size() != 0) {
                            LOGGER.error("non empty collection returned instead of PersistentCollection");
                        }
                    } else if (updatedCollection instanceof Map) {
                        Map<?, ?> map = (Map<?, ?>) updatedCollection;
                        if (map.size() != 0) {
                            LOGGER.error("non empty map returned instead of PersistentCollection");
                        }
                    }
                }

                return CacheResponse.updatedObject(updatedCollection);
            }
        }
    }

    @Override
    protected CacheResponse initialize(Object obj) {

        if (!(obj instanceof AbstractPersistentCollection)) {
            throw new IllegalArgumentException("none PersistentCollection passed " + obj);
        }

        AbstractPersistentCollection col = (AbstractPersistentCollection) obj;
        if (col.wasInitialized()) {
            throw new IllegalArgumentException("PersistentCollection is already initialized " + obj);
        }

        if (col.getRole() == null) {
            throw new IllegalArgumentException("missing role on " + obj);
        }

        synchronized (obj) {

            Object initializedObj = this.cacheManager.getGenericDao().getInitializedCollection(col.getRole(), (Long) col.getKey());

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
        return Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz);
    }
}
