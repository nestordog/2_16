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
import java.util.List;
import java.util.Map;

import ch.algotrader.entity.BaseEntityI;

/**
 * Entry point to the Level-0 Cache
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface CacheManager {

    /**
     * gets an object of the given {@code clazz} by the defined {@code key}.
     */
    public <T> T get(Class<T> clazz, Serializable key);

    /**
     * checks whether an object of the given {@code clazz} and {@code key} is in the cache
     */
    public boolean contains(Class<?> clazz, Serializable key);

    /**
     * lazy-initilazes the give {@code key} of the specified {@code entity}
     */
    public Object initialze(BaseEntityI entity, String key);

    /**
     * performs the given HQL {@code query}
     */
    public List<?> query(String queryString);

    /**
     * performs the given HQL {@code query} by passing defined {@code namedParameters}
     */
    public List<?> query(String queryString, Map<String, Object> namedParameters);

}
