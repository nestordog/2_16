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
package ch.algotrader.cache;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Entry point to the Level-0 Cache
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
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
     * performs the given HQL {@code query}
     */
    public List<?> query(String queryString);

    /**
     * performs the given HQL {@code query} by passing defined {@code namedParameters}
     */
    public List<?> query(String queryString, Map<String, Object> namedParameters);

}
