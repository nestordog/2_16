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

import java.util.List;

/**
 * Abstract Cache Handler class.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
abstract class AbstractHandler {

    /**
     * return true if this Handler is responsible for the specified class
     */
    protected abstract boolean handles(Class<?> clazz);

    /**
     * Puts an object into the Cache
     */
    protected abstract CacheResponse put(Object obj, List<EntityCacheSubKey> stack);

    /**
     * Updates the specified object
     */
    protected abstract CacheResponse update(Object obj);

    /**
     * Lazy-initializes the specified Object.
     */
    protected abstract CacheResponse initialize(Object obj);
}
