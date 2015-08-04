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

import java.util.List;
import java.util.Map;

import ch.algotrader.dao.NamedParam;
import ch.algotrader.entity.BaseEntityI;
import ch.algotrader.enumeration.QueryType;

/**
 * Entry point to the Level-0 Cache
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface CacheManager {

    /**
     * gets an Entity of the given {@code clazz} by the defined {@code id}.
     */
    public <T extends BaseEntityI> T get(Class<T> clazz, long id);

    /**
     * gets all Entities of the given {@code clazz}.
     */
    public <T extends BaseEntityI> List<T> getAll(Class<T> clazz);

    /**
     * Adds an object recursively into the Cache and returns the existingObject if it was already in the Cache
     */
    public Object put(Object obj);

    /**
     * checks whether an object of the given {@code clazz} and {@code id} is in the cache
     */
    public <T extends BaseEntityI> boolean contains(Class<T> clazz, long id);

    /**
     * performs the given HQL {@code query} by passing defined {@code namedParameters}
     */
    public <T> List<T> query(Class<T> clazz, String query, QueryType type, NamedParam... namedParams);

    /**
     * performs the given HQL {@code query} by passing defined {@code namedParameters}
     */
    public <T> T queryUnique(Class<T> clazz, String query, QueryType type, NamedParam... namedParams);

    /**
     * clears the entire cache
     */
    public void clear();

    /**
     * Gets the descriminator value based on the given class.
     */
    public int getDiscriminatorValue(final Class<?> type);

    /**
     * retrieves a map of all caches including their sizes
     */
    public Map<String, Integer> getCacheSize();
}
