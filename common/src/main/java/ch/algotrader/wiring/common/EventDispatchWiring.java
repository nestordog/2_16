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
package ch.algotrader.wiring.common;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.SimpleMessageConverter;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.event.EventListenerRegistry;
import ch.algotrader.event.EventListenerRegistryImpl;
import ch.algotrader.event.EventPublisher;
import ch.algotrader.event.dispatch.DistributedEventDispatcher;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.event.dispatch.LocalEventDispatcher;

/**
 * Event dispatch configuration.
 */
@Configuration
public class EventDispatchWiring {

    @Bean(name = "eventListenerRegistry")
    public EventListenerRegistry createEventListenerRegistry() {

        return new EventListenerRegistryImpl();
    }

    @Bean(name = "eventDispatcher")
    public EventDispatcher createEventDispatcher(
            final CommonConfig commonConfig,
            final EventListenerRegistry eventListenerRegistry,
            final EngineManager engineManager,
            final ApplicationContext applicationContext) {

        EventPublisher internalEventPropagator = applicationContext.containsBean("internalEventPropagator")
                ? applicationContext.getBean("internalEventPropagator", EventPublisher.class) : null;

        if (commonConfig.isSimulation() || commonConfig.isEmbedded()) {

            return new LocalEventDispatcher(eventListenerRegistry, internalEventPropagator, engineManager);
        } else {

            EventPublisher remoteEventPropagator = applicationContext.getBean("remoteEventPropagator", EventPublisher.class);
            return new DistributedEventDispatcher(
                    eventListenerRegistry,
                    remoteEventPropagator,
                    internalEventPropagator,
                    engineManager,
                    new SimpleMessageConverter());
        }
    }

}


