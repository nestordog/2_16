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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.esper.EngineManagerImpl;
import ch.algotrader.service.LookupService;

/**
 * Engine manager configuration.
 */
@Configuration
public class EngineManagerWiring {

    @Bean(name = "engineManager")
    public EngineManager createEngineManager(final ApplicationContext applicationContext) throws Exception {

        final Map<String, Engine> engineBeanMap = applicationContext.getBeansOfType(Engine.class);
        final Map<String, Engine> engineMap = new HashMap<>(engineBeanMap.size());
        for (Map.Entry<String, Engine> entry: engineBeanMap.entrySet()) {
            Engine engine = entry.getValue();
            engineMap.put(engine.getStrategyName(), engine);
        }
        return new EngineManagerImpl(engineMap);
    }

    @Bean(name = "engineManagerPostprocessor")
    public ApplicationListener<ContextRefreshedEvent> createEngineManagerPostprocessor() {

        return new ApplicationListener<ContextRefreshedEvent>() {

            private final AtomicBoolean postProcessed = new AtomicBoolean(false);

            @Override
            public void onApplicationEvent(final ContextRefreshedEvent contextRefreshedEvent) {
                if (this.postProcessed.compareAndSet(false, true)) {

                    ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();
                    LookupService lookupService = applicationContext.getBean("lookupService", LookupService.class);
                    Map<String, Engine> engineBeanMap = applicationContext.getBeansOfType(Engine.class);
                    for (Map.Entry<String, Engine> entry: engineBeanMap.entrySet()) {

                        Engine engine = entry.getValue();
                        Strategy strategy = lookupService.getStrategyByName(engine.getStrategyName());
                        if (strategy != null) {

                            engine.setVariableValue("engineStrategy", strategy);
                        }
                    }
                }
            }
        };
    }

}


