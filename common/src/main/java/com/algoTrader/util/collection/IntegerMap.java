package com.algoTrader.util.collection;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Map having an (Atomic)Integer as value and an arbitraty type as key.
 *
 * @param <K> Type of the Key
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
