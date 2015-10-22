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

import org.apache.commons.lang.StringUtils;

import ch.algotrader.entity.BaseEntityI;

/**
 * Represents a single CacheElement during the traversal of an object graph
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class EntityCacheSubKey extends EntityCacheKey {

    private final String key;

    public EntityCacheSubKey(BaseEntityI entity, String roleName) {

        super(entity);
        this.key = StringUtils.substringAfterLast(roleName, ".");
    }

    public EntityCacheSubKey(BaseEntityI entity) {
        super(entity);
        this.key = EntityCacheKey.ROOT;
    }

    protected String getKey() {
        return this.key;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof EntityCacheSubKey)) {
            return false;
        } else {
            EntityCacheSubKey that = (EntityCacheSubKey) obj;
            return super.equals(that) && Objects.equals(this.key, that.key);
        }
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = hash * 37 + Objects.hashCode(this.key);
        return hash;
    }

    @Override
    public String toString() {
        return super.toString() + ":" + this.key;
    }

}
