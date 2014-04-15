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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Map having an (Atomic)Integer as value and an arbitraty type as key.
 *
 * @param <K> Type of the Key
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IntegerMap<K> extends ConcurrentHashMap<K, AtomicInteger> {

    private static final long serialVersionUID = -847488464256946086L;

    public void put(K key, int value) {

        super.put(key, new AtomicInteger(value));
    }

    public int increment(K key, int value) {

        super.putIfAbsent(key, new AtomicInteger(0));
        return super.get(key).addAndGet(value);
    }

    public long getInt(K key) {

        if (super.containsKey(key)) {
            return super.get(key).intValue();
        } else {
            return 0;
        }
    }
}
