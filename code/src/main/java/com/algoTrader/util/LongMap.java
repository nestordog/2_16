package com.algoTrader.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Map having an (Atomic)Long as value and an arbitraty type as key.
 *
 * @param <K> Type of the Key
 */
public class LongMap<K> extends ConcurrentHashMap<K, AtomicLong> {

    private static final long serialVersionUID = -847488464256946086L;

    public void increment(K key, int value) {

        super.putIfAbsent(key, new AtomicLong(0));
        super.get(key).addAndGet(value);
    }
}
