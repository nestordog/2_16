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
package ch.algotrader.event.dispatch;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

import org.apache.commons.lang.Validate;

/**
* Internal market data subscription registry.
*
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*/
final class MarketDataSubscriptionRegistry {

    private final ConcurrentMap<Long, Set<String>> marketDataSubscriptionMap;

    public MarketDataSubscriptionRegistry() {
        this.marketDataSubscriptionMap = new ConcurrentHashMap<>();
    }

    public void register(final String strategyName, final long securityId) {

        Validate.notNull(strategyName, "Strategy name is null");

        Set<String> strategySet = this.marketDataSubscriptionMap.get(securityId);
        if (strategySet == null) {
            Set<String> newStrategySet = new CopyOnWriteArraySet<>();
            strategySet = this.marketDataSubscriptionMap.putIfAbsent(securityId, newStrategySet);
            if (strategySet == null) {
                strategySet = newStrategySet;
            }
        }
        strategySet.add(strategyName);
    }

    public void unregister(final String strategyName, final long securityId) {

        Validate.notNull(strategyName, "Strategy name is null");

        Set<String> strategySet = this.marketDataSubscriptionMap.get(securityId);
        if (strategySet != null) {
            strategySet.remove(strategyName);
        }
    }

    public boolean isRegistered(final long securityId, final String strategyName) {

        Validate.notNull(strategyName, "Strategy name is null");

        Set<String> strategySet = this.marketDataSubscriptionMap.get(securityId);
        if (strategySet != null) {
            return strategySet.contains(strategyName);
        } else {
            return false;
        }
    }

    public void invoke(final long securityId, final Consumer<String> consumer) {

        Set<String> strategySet = this.marketDataSubscriptionMap.get(securityId);
        if (strategySet != null && !strategySet.isEmpty()) {
            for (String strategyName: strategySet) {
                consumer.accept(strategyName);
            }
        }
    }

}
