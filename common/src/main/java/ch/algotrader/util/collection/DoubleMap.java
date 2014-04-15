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

/**
 * Map having an Double as value and an arbitraty type as key.
 *
 * @param <K> Type of the Key
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
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
