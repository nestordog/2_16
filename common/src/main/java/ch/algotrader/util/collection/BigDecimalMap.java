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
package ch.algotrader.util.collection;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Map having a BigDecimal as value and an arbitraty type as key.
 *
 * @param <K> Type of the Key
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class BigDecimalMap<K> extends ConcurrentHashMap<K, BigDecimal> {

    private static final long serialVersionUID = 7996620853224763555L;

    public void increment(K key, BigDecimal value) {

        if (super.containsKey(key)) {
            super.put(key, super.get(key).add(value));
        } else {
            super.put(key, value);
        }
    }

    public BigDecimal getBigDecimal(K key) {

        if (super.containsKey(key)) {
            return super.get(key);
        } else {
            return new BigDecimal(0.0);
        }
    }
}
