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

import java.util.Objects;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;

import ch.algotrader.cache.CacheEvictionEventVO;
import ch.algotrader.entity.marketData.MarketDataEventVO;
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
public class DistributedEventDispatcherImpl implements EventDispatcher, MessageListener {

    private static final Logger EVENT_LOGGER = LogManager.getLogger("ch.algotrader.event.dispatch.mq.EVENTS");
    private static final Logger MARKET_DATA_LOGGER = LogManager.getLogger("ch.algotrader.event.dispatch.mq.MARKET_DATA");
    private static final Logger CACHE_LOGGER = LogManager.getLogger("ch.algotrader.event.dispatch.mq.CACHE");

    private final EventBroadcaster localEventBroadcaster;
    private final EngineManager engineManager;
    private final JmsTemplate genericTemplate;
    private final JmsTemplate strategyTemplate;
    private final JmsTemplate marketDataTemplate;
    private final MessageConverter messageConverter;
    private final boolean tracing;

    public DistributedEventDispatcherImpl(
            final EventBroadcaster localEventBroadcaster,
            final EngineManager engineManager,
            final JmsTemplate genericTemplate,
            final JmsTemplate strategyTemplate,
            final JmsTemplate marketDataTemplate,
            final MessageConverter messageConverter) {

        Validate.notNull(localEventBroadcaster, "EventBroadcaster is null");
        Validate.notNull(engineManager, "EngineManager is null");
        Validate.notNull(messageConverter, "MessageConverter is null");

        this.localEventBroadcaster = localEventBroadcaster;
        this.engineManager = engineManager;
        this.genericTemplate = genericTemplate;
        this.strategyTemplate = strategyTemplate;
        this.marketDataTemplate = marketDataTemplate;
        this.messageConverter = messageConverter;
        this.tracing = EVENT_LOGGER.isTraceEnabled() || MARKET_DATA_LOGGER.isTraceEnabled() || CACHE_LOGGER.isTraceEnabled();
    }

    @Override
    public void sendEvent(final String engineName, final Object obj) {
        // check if it is a local engine
        Engine engine = this.engineManager.lookup(engineName);
        if (engine != null) {
            engine.sendEvent(obj);
        } else {
            Objects.requireNonNull(this.strategyTemplate, "Strategy template is null");
            this.strategyTemplate.convertAndSend(engineName + ".QUEUE", obj);
        }
    }

    @Override
    public void broadcastLocalEventListeners(final Object event) {

        this.localEventBroadcaster.broadcast(event);
    }

    @Override
    public void broadcastLocalStrategies(final Object event) {

        broadcastLocalEventListeners(event);

        for (Engine engine : this.engineManager.getStrategyEngines()) {

            engine.sendEvent(event);
        }

    }

    @Override
    public void broadcastLocal(final Object event) {

        broadcastLocalEventListeners(event);

        for (Engine engine : this.engineManager.getEngines()) {

            engine.sendEvent(event);
        }
    }

    @Override
    public void broadcastRemote(final Object event) {

        Objects.requireNonNull(this.genericTemplate, "Generic template is null");
        this.genericTemplate.convertAndSend(event, message -> {

            // add class Property
            message.setStringProperty("clazz", event.getClass().getName());
            return message;
        });
    }

    @Override
    public void broadcastEventListeners(final Object event) {

        broadcastLocalEventListeners(event);
        broadcastRemote(event);
    }

    @Override
    public void broadcastAllStrategies(final Object event) {

        broadcastLocalStrategies(event);
        broadcastRemote(event);
    }

    @Override
    public void broadcast(final Object event) {

        broadcastLocal(event);
        broadcastRemote(event);
    }

    @Override
    public void registerMarketDataSubscription(final String strategyName, final long securityId) {
    }

    @Override
    public void unregisterMarketDataSubscription(final String strategyName, final long securityId) {
    }

    @Override
    public void sendMarketDataEvent(final MarketDataEventVO marketDataEvent) {

        Objects.requireNonNull(this.marketDataTemplate, "Market data template is null");
        this.marketDataTemplate.convertAndSend(marketDataEvent, message -> {
            // add securityId Property
            message.setLongProperty("securityId", marketDataEvent.getSecurityId());
            return message;
        });
        broadcastLocalEventListeners(marketDataEvent);
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
            broadcastLocal(event);
        } catch (JMSException ex) {
            throw new EventDispatchException("Failure de-serializing message content", ex);
        }
    }

}
