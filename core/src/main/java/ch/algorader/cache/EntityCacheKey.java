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
package ch.algorader.cache;

import java.io.Serializable;

import ch.algorader.entity.IdentifiableI;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class EntityCacheKey {

    private final Serializable key;
    private final Class<?> clazz;
    private final Class<?> rootClass;
    private final int hashCode;

    public EntityCacheKey(IdentifiableI identifiable) {

        this(identifiable.getClass(), identifiable.getId());
    }

    public EntityCacheKey(String entityName, Serializable key) throws ClassNotFoundException {

        this(Class.forName(entityName), key);
    }

    public EntityCacheKey(Class<?> clazz, Serializable key) {

        this.clazz = clazz;
        this.key = key;

        while (!(clazz.getSuperclass()).equals(Object.class)) {
            clazz = clazz.getSuperclass();
        }

        this.rootClass = clazz;

        this.hashCode = (17 * 37 + this.rootClass.getName().hashCode()) * 37 + key.hashCode();
    }

    @Override
    public String toString() {

        return this.clazz.getName() + '#' + this.key.toString();
    }

    @Override
    public boolean equals(Object other) {

        if (!(other instanceof EntityCacheKey)) {
            return false;
        }

        EntityCacheKey that = (EntityCacheKey) other;
        return this.rootClass.equals(that.rootClass) && this.key.equals(that.key);
    }

    @Override
    public int hashCode() {

        return this.hashCode;
    }
}
