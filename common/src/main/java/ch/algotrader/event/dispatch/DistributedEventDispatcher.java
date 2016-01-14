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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jms.support.converter.MessageConverter;

import ch.algotrader.cache.CacheEvictionEventVO;
import ch.algotrader.entity.marketData.MarketDataEventVO;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.event.EventBroadcaster;
import ch.algotrader.event.EventPublisher;

/**
* Distributed event dispatcher implementation.
*
* @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
*/
public class DistributedEventDispatcher implements EventDispatcher, MessageListener {

    private static final Logger EVENT_LOGGER = LogManager.getLogger("ch.algotrader.event.dispatch.mq.EVENTS");
    private static final Logger MARKET_DATA_LOGGER = LogManager.getLogger("ch.algotrader.event.dispatch.mq.MARKET_DATA");
    private static final Logger CACHE_LOGGER = LogManager.getLogger("ch.algotrader.event.dispatch.mq.CACHE");

    private final EventBroadcaster localEventBroadcaster;
    private final EventPublisher remoteEventPublisher;
    private final EventPublisher internalEventPublisher;
    private final EngineManager engineManager;
    private final MessageConverter messageConverter;
    private final MarketDataSubscriptionRegistry marketDataSubscriptionRegistry;
    private final boolean tracing;

    public DistributedEventDispatcher(
            final EventBroadcaster localEventBroadcaster,
            final EventPublisher remoteEventPublisher,
            final EventPublisher internalEventPublisher,
            final EngineManager engineManager,
            final MessageConverter messageConverter) {

        Validate.notNull(localEventBroadcaster, "EventBroadcaster is null");
        Validate.notNull(engineManager, "EngineManager is null");
        Validate.notNull(messageConverter, "MessageConverter is null");

        this.localEventBroadcaster = localEventBroadcaster;
        this.remoteEventPublisher = remoteEventPublisher;
        this.internalEventPublisher = internalEventPublisher;
        this.engineManager = engineManager;
        this.messageConverter = messageConverter;
        this.marketDataSubscriptionRegistry = new MarketDataSubscriptionRegistry();
        this.tracing = EVENT_LOGGER.isTraceEnabled() || MARKET_DATA_LOGGER.isTraceEnabled() || CACHE_LOGGER.isTraceEnabled();
    }

    @Override
    public void sendEvent(final String strategyName, final Object event) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        // check if it is a local engine
        if (!StrategyImpl.SERVER.equals(strategyName)) {
            // check if it is a local engine
            Engine engine = this.engineManager.lookup(strategyName);
            if (engine != null) {
                engine.sendEvent(event);
            } else {
                if (this.remoteEventPublisher != null) {
                    this.remoteEventPublisher.publishStrategyEvent(event, strategyName);
                }
            }
        }
        if (this.internalEventPublisher != null) {
            this.internalEventPublisher.publishStrategyEvent(event, strategyName);
        }
    }

    @Override
    public void resendPastEvent(final String strategyName, final Object event) {

        if (this.internalEventPublisher != null) {
            this.internalEventPublisher.publishStrategyEvent(event, strategyName);
        }
    }

    @Override
    public void registerMarketDataSubscription(final String strategyName, final long securityId) {

        this.marketDataSubscriptionRegistry.register(strategyName, securityId);
    }

    @Override
    public void unregisterMarketDataSubscription(final String strategyName, final long securityId) {

        this.marketDataSubscriptionRegistry.unregister(strategyName, securityId);
    }

    @Override
    public boolean isMarketDataSubscriptionRegistered(final long securityId, final String strategyName) {

        return this.marketDataSubscriptionRegistry.isRegistered(securityId, strategyName);
    }

    @Override
    public void sendMarketDataEvent(final MarketDataEventVO event) {

        this.localEventBroadcaster.broadcast(event);
        if (this.remoteEventPublisher != null) {
            this.remoteEventPublisher.publishMarketDataEvent(event);
        }
        if (this.internalEventPublisher != null) {
            this.internalEventPublisher.publishMarketDataEvent(event);
        }
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
        if (recipients.contains(EventRecipient.REMOTE)) {
            if (this.remoteEventPublisher != null) {
                this.remoteEventPublisher.publishGenericEvent(event);
            }
            if (this.internalEventPublisher != null) {
                this.internalEventPublisher.publishGenericEvent(event);
            }
        }
    }

    @Override
    public void onMessage(final Message message) {

        try {
            Object event = this.messageConverter.fromMessage(message);

            if (this.tracing) {
                if (event instanceof MarketDataEventVO) {
                    if (MARKET_DATA_LOGGER.isInfoEnabled()) {
                        MARKET_DATA_LOGGER.trace(event.getClass().getName() + ": " + event);
                    }
                } else if (event instanceof CacheEvictionEventVO) {
                    if (CACHE_LOGGER.isTraceEnabled()) {
                        CACHE_LOGGER.trace(event.getClass().getName() + ": " + event);
                    }
                } else {
                    if (EVENT_LOGGER.isTraceEnabled()) {
                        EVENT_LOGGER.trace(event.getClass().getName() + ": " + event);
                    }
                }
            }
            broadcast(event, EventRecipient.ALL_LOCAL);
        } catch (JMSException ex) {
            throw new EventDispatchException("Failure de-serializing message content", ex);
        }
    }

}
