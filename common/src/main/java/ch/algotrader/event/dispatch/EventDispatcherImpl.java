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

import java.util.Objects;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.commons.lang.Validate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.jms.support.converter.MessageConverter;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.marketData.MarketDataEvent;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.event.EventBroadcaster;

/**
* {@link ch.algotrader.esper.EngineManager} implementation.
*
* @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
*
* @version $Revision$ $Date$
*/
public class EventDispatcherImpl implements EventDispatcher, MessageListener {

    private final CommonConfig commonConfig;
    private final EventBroadcaster localEventBroadcaster;
    private final EngineManager engineManager;
    private final JmsTemplate genericTemplate;
    private final JmsTemplate strategyTemplate;
    private final JmsTemplate marketDataTemplate;
    private final MessageConverter messageConverter;

    public EventDispatcherImpl(
            final CommonConfig commonConfig,
            final EventBroadcaster localEventBroadcaster,
            final EngineManager engineManager,
            final JmsTemplate genericTemplate,
            final JmsTemplate strategyTemplate,
            final JmsTemplate marketDataTemplate,
            final MessageConverter messageConverter) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(localEventBroadcaster, "EventBroadcaster is null");
        Validate.notNull(engineManager, "EngineManager is null");
        Validate.notNull(messageConverter, "MessageConverter is null");

        this.commonConfig = commonConfig;
        this.localEventBroadcaster = localEventBroadcaster;
        this.engineManager = engineManager;
        this.genericTemplate = genericTemplate;
        this.strategyTemplate = strategyTemplate;
        this.marketDataTemplate = marketDataTemplate;
        this.messageConverter = messageConverter;
    }

    @Override
    public void sendEvent(final String engineName, final Object obj) {
        // check if it is a local engine
        final Engine engine = this.engineManager.getEngine(engineName);
        if (engine != null) {
            engine.sendEvent(obj);
        } else {
            if (!this.commonConfig.isSimulation() && !this.commonConfig.isEmbedded()) {
                // sent to the strategy queue
                Objects.requireNonNull(this.strategyTemplate, "Strategy JMS template is null");
                this.strategyTemplate.convertAndSend(engineName + ".QUEUE", obj);
            }
        }
    }

    @Override
    public void sendMarketDataEvent(final MarketDataEvent marketDataEvent) {

        if (this.commonConfig.isSimulation() || this.commonConfig.isEmbedded()) {
            for (final Subscription subscription : marketDataEvent.getSecurity().getSubscriptions()) {
                if (!subscription.getStrategy().getName().equals(StrategyImpl.SERVER)) {
                    final String strategyName = subscription.getStrategy().getName();
                    final Engine engine = this.engineManager.getEngine(strategyName);
                    if (engine != null) {
                        engine.sendEvent(marketDataEvent);
                    }
                }
            }
            localEventBroadcaster.broadcast(marketDataEvent);//TODO should engines receive the event first or the local VM ?
        } else {

            // send using the jms template
            Objects.requireNonNull(this.marketDataTemplate, "Market JMS template is null");
            this.marketDataTemplate.convertAndSend(marketDataEvent, new MessagePostProcessor() {

                @Override
                public Message postProcessMessage(Message message) throws JMSException {

                    // add securityId Property
                    message.setIntProperty("securityId", marketDataEvent.getSecurity().getId());
                    return message;
                }
            });
        }
    }

    @Override
    public void broadcastLocal(final Object event) {

        for (Engine engine: this.engineManager.getEngines()) {

            engine.sendEvent(event);
        }

        this.localEventBroadcaster.broadcast(event);
    }

    @Override
    public void broadcastRemote(final Object event) {

        if (!this.commonConfig.isSimulation() && !this.commonConfig.isEmbedded()) {

            // send using the jms template
            Objects.requireNonNull(this.genericTemplate, "Generic JMS template is null");
            this.genericTemplate.convertAndSend(event, new MessagePostProcessor() {

                @Override
                public Message postProcessMessage(Message message) throws JMSException {

                    // add class Property
                    message.setStringProperty("clazz", event.getClass().getName());
                    return message;
                }
            });
        }
    }

    @Override
    public void broadcast(final Object event) {

        broadcastLocal(event);
        broadcastRemote(event);
    }

    @Override
    public void onMessage(final Message message) {

        try {
            Object event = this.messageConverter.fromMessage(message);
            broadcastLocal(event);
        } catch (JMSException ex) {
            throw new EventDispatchException("Failure de-serializing message content", ex);
        }
    }

}
