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

import java.util.Objects;

import ch.algotrader.entity.BaseEntityI;

/**
 * A CacheKey for Entities composed of a {@code clazz} and a {@code key}.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class EntityCacheKey {

    public static final String ROOT = "root";

    private final long id;
    private final Class<?> clazz;
    private final Class<?> rootClass;

    public EntityCacheKey(BaseEntityI entity) {

        this(entity.getClass(), entity.getId());
    }

    public EntityCacheKey(Class<?> clazz, long id) {

        if (!BaseEntityI.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("must be of type BaseEntityI");
        }

        this.clazz = clazz;
        this.id = id;

        // get the top most superclass
        while (!(clazz.getSuperclass()).equals(Object.class)) {
            clazz = clazz.getSuperclass();
        }

        this.rootClass = clazz;

    }

    @Override
    public String toString() {

        return this.clazz.getName() + '#' + this.id;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof EntityCacheKey)) {
            return false;
        } else {
            EntityCacheKey that = (EntityCacheKey) obj;
            return Objects.equals(this.rootClass, that.rootClass) && this.id == that.id;
        }
    }

    @Override
    public int hashCode() {

        int hash = 17;
        hash = hash * 37 + this.rootClass.getName().hashCode();
        hash = hash * 37 + Long.hashCode(this.id);

        return hash;
    }
}
