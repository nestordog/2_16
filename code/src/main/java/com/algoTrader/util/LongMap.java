package com.algoTrader.util;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Map having an (Atomic)Long as value and an arbitraty type as key.
 *
 * @param <K> Type of the Key
 */
public class LongMap<K> extends HashMap<K, AtomicLong> {

    private static final long serialVersionUID = -847488464256946086L;

    public void increment(K key, long value) {

        if (!super.containsKey(key)) {
            super.put(key, new AtomicLong(0));
        }

        super.get(key).addAndGet(value);
    }

    public long getLong(K key) {

        return super.get(key).longValue();
    }
}
