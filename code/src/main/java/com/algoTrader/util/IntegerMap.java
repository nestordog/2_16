package com.algoTrader.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Map having an (Atomic)Integer as value and an arbitraty type as key.
 *
 * @param <K> Type of the Key
 */
public class IntegerMap<K> extends ConcurrentHashMap<K, AtomicInteger> {

    private static final long serialVersionUID = -847488464256946086L;

    public void increment(K key, int value) {

        super.putIfAbsent(key, new AtomicInteger(0));
        super.get(key).addAndGet(value);
    }
}
