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
package com.algoTrader.entity.security;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.StringUtils;

import com.algoTrader.util.collection.LongMap;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CombinationImpl extends Combination {

    private static final long serialVersionUID = -3967940153149799380L;

    @Override
    public LongMap<Security> getQuantityMap() {

        LongMap<Security> qtyMap = new LongMap<Security>();
        for (Component component : getComponentsInitialized()) {
            qtyMap.increment(component.getSecurityInitialized(), component.getQuantity());
        }

        return qtyMap;
    }

    @Override
    public String toString() {

        return (getSymbol() != null ? getSymbol() + " " : "") + StringUtils.join(CollectionUtils.collect(getComponentsInitialized(), new Transformer<Component, String>() {
            @Override
            public String transform(Component component) {
                return component.getQuantity() + " " + component.getSecurity();
            }
        }), " / ");
    }
}
