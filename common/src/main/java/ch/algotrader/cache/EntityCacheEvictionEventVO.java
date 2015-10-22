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

import java.io.Serializable;

/**
 * Notifies that a particular key (e.g. ROOT or collection) of the specified entityClass  and id has been modified.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class EntityCacheEvictionEventVO implements Serializable {

    private static final long serialVersionUID = -9201194174175757269L;

    private final Class<?> entityClass;
    private final long id;
    private final String key;

    public EntityCacheEvictionEventVO(Class<?> entityClass, long id, String key) {
        this.entityClass = entityClass;
        this.id = id;
        this.key = key;
    }

    public Class<?> getEntityClass() {
        return this.entityClass;
    }

    public long getId() {
        return this.id;
    }

    public String getKey() {
        return this.key;
    }
    @Override
    public String toString() {
        return "entityClass=" + this.entityClass + ", id=" + this.id + ", key=" + this.key;
    }
}
