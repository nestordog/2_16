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
package ch.algotrader.util.collection;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Map having an (Atomic)Long as value and an arbitraty type as key.
 *
 * @param <K> Type of the Key
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class LongMap<K> extends ConcurrentHashMap<K, AtomicLong> {

    private static final long serialVersionUID = -847488464256946086L;

    public void put(K key, long value) {

        super.put(key, new AtomicLong(value));
    }

    public long increment(K key, long value) {

        super.putIfAbsent(key, new AtomicLong(0));
        return super.get(key).addAndGet(value);
    }

    public long getLong(K key) {

        if (super.containsKey(key)) {
            return super.get(key).longValue();
        } else {
            return 0;
        }
    }
}
