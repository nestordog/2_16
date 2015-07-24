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
package ch.algotrader.event.dispatch;

import org.apache.commons.lang.Validate;

import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.marketData.MarketDataEvent;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.event.EventBroadcaster;

/**
* {@link EngineManager} implementation.
*
* @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
*
* @version $Revision$ $Date$
*/
public class LocalEventDispatcherImpl implements EventDispatcher {

    private final EventBroadcaster localEventBroadcaster;
    private final EngineManager engineManager;

    public LocalEventDispatcherImpl(
            final EventBroadcaster localEventBroadcaster,
            final EngineManager engineManager) {

        Validate.notNull(localEventBroadcaster, "EventBroadcaster is null");
        Validate.notNull(engineManager, "EngineManager is null");

        this.localEventBroadcaster = localEventBroadcaster;
        this.engineManager = engineManager;
    }

    @Override
    public void sendEvent(final String engineName, final Object obj) {
        // check if it is a local engine
        final Engine engine = this.engineManager.getEngine(engineName);
        if (engine != null) {
            engine.sendEvent(obj);
        }
    }

    @Override
    public void sendMarketDataEvent(final MarketDataEvent marketDataEvent) {

        for (final Subscription subscription : marketDataEvent.getSecurity().getSubscriptions()) {
            if (!subscription.getStrategy().getName().equals(StrategyImpl.SERVER)) {
                final String strategyName = subscription.getStrategy().getName();
                final Engine engine = this.engineManager.getEngine(strategyName);
                if (engine != null) {
                    engine.sendEvent(marketDataEvent);
                }
            }
        }
        this.localEventBroadcaster.broadcast(marketDataEvent);//TODO should engines receive the event first or the local VM ?
    }

    @Override
    public void broadcastLocal(final Object event) {

        for (Engine engine : this.engineManager.getEngines()) {

            engine.sendEvent(event);
        }

        this.localEventBroadcaster.broadcast(event);
    }

    @Override
    public void broadcastLocalStrategies(final Object event) {

        for (Engine engine : this.engineManager.getStrategyEngines()) {

            engine.sendEvent(event);
        }

        this.localEventBroadcaster.broadcast(event);
    }

    @Override
    public void broadcastRemote(final Object event) {
    }

    @Override
    public void broadcastAllStrategies(final Object event) {

        broadcastLocalStrategies(event);
    }

    @Override
    public void broadcast(final Object event) {

        broadcastLocal(event);
    }

}
