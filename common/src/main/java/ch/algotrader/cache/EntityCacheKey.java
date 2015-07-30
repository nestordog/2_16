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

import ch.algotrader.entity.BaseEntityI;

/**
 * A CacheKey for Entities composed of a {@code clazz} and a {@code key}.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class EntityCacheKey {

    private final Serializable id;
    private final Class<?> clazz;
    private final Class<?> rootClass;
    private final int hashCode;

    public EntityCacheKey(BaseEntityI entity) {

        this(entity.getClass(), entity.getId());
    }

    public EntityCacheKey(String entityName, Serializable id) throws ClassNotFoundException {

        this(Class.forName(entityName), id);
    }

    public EntityCacheKey(Class<?> clazz, Serializable id) {

        this.clazz = clazz;
        this.id = id;

        // get the top most superclass
        while (!(clazz.getSuperclass()).equals(Object.class)) {
            clazz = clazz.getSuperclass();
        }

        this.rootClass = clazz;

        // create the hashCode based on the rootClass and the key
        this.hashCode = (17 * 37 + this.rootClass.getName().hashCode()) * 37 + id.hashCode();
    }

    @Override
    public String toString() {

        return this.clazz.getName() + '#' + this.id.toString();
    }

    @Override
    public boolean equals(Object other) {

        if (!(other instanceof EntityCacheKey)) {
            return false;
        }

        // compate based on the rootClass and the key
        EntityCacheKey that = (EntityCacheKey) other;
        return this.rootClass.equals(that.rootClass) && this.id.equals(that.id);
    }

    @Override
    public int hashCode() {

        return this.hashCode;
    }
}
