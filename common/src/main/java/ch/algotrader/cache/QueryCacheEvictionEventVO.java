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

/**
 * Notifies that a particular spaceName (table) has been modified.
 * All cached queries based on this spaceName should be evicted.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class QueryCacheEvictionEventVO extends CacheEvictionEventVO {

    private static final long serialVersionUID = -9201194174175757269L;

    private final String spaceName;

    public QueryCacheEvictionEventVO(String spaceName) {
        this.spaceName = spaceName;
    }

    public String getSpaceName() {
        return this.spaceName;
    }

    @Override
    public String toString() {
        return this.spaceName;
    }

}
