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

/**
 * Notifies that a particular spaceName (table) has been modified.
 * All cached queries based on this spaceName should be evicted.
 * 
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class QueryCacheEvictionEvent implements Serializable {

    private static final long serialVersionUID = -9201194174175757269L;

    private final String spaceName;

    public QueryCacheEvictionEvent(String spaceName) {
        this.spaceName = spaceName;
    }

    public String getSpaceName() {
        return this.spaceName;
    }

    @Override
    public String toString() {
        return this.spaceName + "]";
    }

}
