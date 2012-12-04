package com.algoTrader.util.collection;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Map having a BigDecimal as value and an arbitraty type as key.
 *
 * @param <K> Type of the Key
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
