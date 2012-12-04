package com.algoTrader.util.collection;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Map having an Double as value and an arbitraty type as key.
 *
 * @param <K> Type of the Key
 */
public class DoubleMap<K> extends ConcurrentHashMap<K, Double> {

    private static final long serialVersionUID = -6009567294465054476L;

    public void increment(K key, double value) {

        if (super.containsKey(key)) {
            super.put(key, super.get(key) + value);
        } else {
            super.put(key, value);
        }
    }

    public double getDouble(K key) {

        if (super.containsKey(key)) {
            return super.get(key).doubleValue();
        } else {
            return 0.0;
        }
    }
}
