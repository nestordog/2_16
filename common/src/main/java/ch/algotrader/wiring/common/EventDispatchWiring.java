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
package ch.algotrader.wiring.common;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.SimpleMessageConverter;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.event.EventListenerRegistry;
import ch.algotrader.event.EventListenerRegistryImpl;
import ch.algotrader.event.dispatch.DistributedEventDispatcherImpl;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.event.dispatch.LocalEventDispatcherImpl;

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

        if (commonConfig.isSimulation() || commonConfig.isEmbedded() || !applicationContext.containsBean("jmsActiveMQFactory")) {
            return new LocalEventDispatcherImpl(eventListenerRegistry, engineManager);
        } else {
            return new DistributedEventDispatcherImpl(
                    eventListenerRegistry,
                    engineManager,
                    applicationContext.containsBean("genericTemplate") ? applicationContext.getBean("genericTemplate", JmsTemplate.class) : null,
                    applicationContext.containsBean("strategyTemplate") ? applicationContext.getBean("strategyTemplate", JmsTemplate.class) : null,
                    applicationContext.containsBean("marketDataTemplate") ? applicationContext.getBean("marketDataTemplate", JmsTemplate.class) : null,
                    new SimpleMessageConverter());
        }
    }

}


