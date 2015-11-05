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

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.lang.Validate;

import ch.algotrader.entity.marketData.MarketDataEventVO;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.event.EventBroadcaster;
import ch.algotrader.event.EventPublisher;

/**
* {@link EngineManager} implementation.
*
* @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
*/
public class LocalEventDispatcher implements EventDispatcher {

    private final EventBroadcaster localEventBroadcaster;
    private final EngineManager engineManager;
    private final EventPublisher internalEventPublisher;
    private final ConcurrentMap<Long, Set<String>> marketDataSubscriptionMap;

    public LocalEventDispatcher(
            final EventBroadcaster localEventBroadcaster,
            final EventPublisher internalEventPublisher,
            final EngineManager engineManager) {

        Validate.notNull(localEventBroadcaster, "EventBroadcaster is null");
        Validate.notNull(engineManager, "EngineManager is null");

        this.localEventBroadcaster = localEventBroadcaster;
        this.internalEventPublisher = internalEventPublisher;
        this.engineManager = engineManager;
        this.marketDataSubscriptionMap = new ConcurrentHashMap<>();
    }

    @Override
    public void broadcast(final Object event, final Set<EventRecipient> recipients) {
        if (recipients.contains(EventRecipient.LISTENERS)) {
            this.localEventBroadcaster.broadcast(event);
        }
        if (recipients.contains(EventRecipient.STRATEGY_ENGINES)) {
            Collection<Engine> engines;
            if (recipients.contains(EventRecipient.SERVER_ENGINE)) {
                engines = this.engineManager.getEngines();
            } else {
                engines = this.engineManager.getStrategyEngines();
            }
            for (Engine engine: engines) {
                engine.sendEvent(event);
            }
        }
    }

    @Override
    public void registerMarketDataSubscription(final String strategyName, final long securityId) {

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

    @Override
    public void unregisterMarketDataSubscription(final String strategyName, final long securityId) {

        Validate.notNull(strategyName, "Strategy name is null");

        Set<String> strategySet = this.marketDataSubscriptionMap.get(securityId);
        if (strategySet != null) {
            strategySet.remove(strategyName);
        }
    }

    public boolean isMarketDataSubscriptionRegistered(final long securityId, final String strategyName) {

        Validate.notNull(strategyName, "Strategy name is null");

        Set<String> strategySet = this.marketDataSubscriptionMap.get(securityId);
        if (strategySet != null) {
            return strategySet.contains(strategyName);
        } else {
            return false;
        }
    }

    @Override
    public void sendMarketDataEvent(final MarketDataEventVO event) {

        this.localEventBroadcaster.broadcast(event);
        Set<String> strategySet = this.marketDataSubscriptionMap.get(event.getSecurityId());
        if (strategySet != null && !strategySet.isEmpty()) {
            for (String strategyName: strategySet) {
                // Do not propagate market data to the SERVER
                if (!strategyName.equalsIgnoreCase(StrategyImpl.SERVER)) {
                    final Engine engine = this.engineManager.lookup(strategyName);
                    if (engine != null) {
                        engine.sendEvent(event);
                    }
                }
            }
        }
        if (this.internalEventPublisher != null) {
            this.internalEventPublisher.publishMarketDataEvent(event);
        }
    }

    @Override
    public void sendEvent(final String strategyName, final Object event) {
        // check if it is a local engine
        this.localEventBroadcaster.broadcast(event);
        final Engine engine = this.engineManager.lookup(strategyName);
        if (engine != null) {
            engine.sendEvent(event);
        }
        if (this.internalEventPublisher != null) {
            this.internalEventPublisher.publishStrategyEvent(event, strategyName);
        }
    }

}
