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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.collection.internal.AbstractPersistentCollection;
import org.hibernate.collection.spi.PersistentCollection;

import ch.algotrader.entity.BaseEntityI;
import ch.algotrader.util.FieldUtil;

/**
 * Cache Handler for Collections.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
class CollectionHandler extends AbstractHandler {

    private static Logger logger = Logger.getLogger(CollectionHandler.class.getName());

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

        synchronized (obj) {

            // process Maps
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

                // process Lists
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

                // process Sets
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

            } else {
                throw new IllegalArgumentException("unsupported collection type " + obj.getClass());
            }
        }
        return null;
    }

    @Override
    protected Object update(Object obj) {

        if (!(obj instanceof PersistentCollection)) {
            throw new IllegalArgumentException("PersistentCollection needed");
        }

        synchronized (obj) {

            PersistentCollection origCollection = (PersistentCollection) obj;

            // sometimes there is no role so collection initialization will not work
            if (origCollection.getRole() == null) {
                return null;
            }

            Object updatedCollection = this.cacheManager.getGenericDao().getInitializedCollection(origCollection.getRole(), origCollection.getKey());

            // owner does not exist anymore so remove it
            if (updatedCollection == null) {

                Object owner = origCollection.getOwner();
                EntityCacheKey cacheKey = new EntityCacheKey((BaseEntityI) owner);
                this.cacheManager.getEntityCache().detach(cacheKey);

            } else {

                // getInitializedCollection should normally return a PersistentCollection
                if (updatedCollection instanceof PersistentCollection) {

                    PersistentCollection updatedCol = (PersistentCollection) updatedCollection;
                    FieldUtil.copyAllFields(origCollection, updatedCol);

                    // make sure everything is in the cache
                    this.cacheManager.put(origCollection);

                    // log if PersistentCollection returns a Collection or Map to furhter investigate
                } else {

                    if (updatedCollection instanceof Collection) {
                        Collection<?> col = (Collection<?>) updatedCollection;
                        if (col.size() != 0) {
                            logger.error("non empty collection returned instead of PersistentCollection");
                        }
                    } else if (updatedCollection instanceof Map) {
                        Map<?, ?> map = (Map<?, ?>) updatedCollection;
                        if (map.size() != 0) {
                            logger.error("non empty map returned instead of PersistentCollection");
                        }
                    }
                }
            }
            return updatedCollection;
        }
    }

    @Override
    protected Object initialize(Object obj) {

        if (!(obj instanceof AbstractPersistentCollection)) {
            return null;
        }

        synchronized (obj) {

            AbstractPersistentCollection col = (AbstractPersistentCollection) obj;
            if (col.wasInitialized()) {
                return null;
            }

            Object initializedObj = this.cacheManager.getGenericDao().getInitializedCollection(col.getRole(), col.getKey());

            Object existingObj = put(initializedObj);

            // return the exstingObj if it was already in the cache otherwise the newly initialized obj
            return existingObj != null ? existingObj : initializedObj;
        }
    }

    @Override
    protected boolean handles(Class<?> clazz) {
        return Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz);
    }
}
