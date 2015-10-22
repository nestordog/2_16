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
package ch.algotrader.service.groups;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Bean representing the group with all strategy items.
 */
public class StrategyGroup {

    public static StrategyGroup single(final String strategyName) {
        Objects.requireNonNull(strategyName, "Strategy name is null");
        final HashMap<String, Double> map = new HashMap<>();
        map.put(strategyName, 1.0d);
        return new StrategyGroup(map);
    }

    private final Map<String, Double> nameToWeight;

    public StrategyGroup(Map<String, Double> nameToWeight) {
        this.nameToWeight = Collections.unmodifiableMap(Objects.requireNonNull(nameToWeight, "nameToWeight cannot be null"));
    }

    /**
     * Returns an unmodifiable set with all strategy names.
     * @return an unmodifiable set with strategy names.
     */
    public Set<String> getStrategyNames() {
        return nameToWeight.keySet();
    }

    /**
     * Returns the weight for the given strategy, or 0 if no such strategy exists.
     * @param strategyName the name of the strategy (item)
     * @return the weight for this strategy, or 0 if not found
     */
    public double getWeight(String strategyName) {
        final Double weight = nameToWeight.get(strategyName);
        return weight == null ? 0 : weight;
    }

    @Override
    public String toString() {
        return this.nameToWeight.toString();
    }

}
