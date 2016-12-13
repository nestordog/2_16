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

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import ch.algotrader.esper.Engine;

/**
 * Engine manager post-initialization configuration. Ideally should be eliminated in the future.
 */
@Configuration
public class EnginePostInitWiring {

    @Bean(name = "engineManagerPostprocessor")
    public ApplicationListener<ContextRefreshedEvent> createEngineManagerPostprocessor() {

        return new ApplicationListener<ContextRefreshedEvent>() {

            private final AtomicBoolean postProcessed = new AtomicBoolean(false);

            @Override
            public void onApplicationEvent(final ContextRefreshedEvent contextRefreshedEvent) {
                if (this.postProcessed.compareAndSet(false, true)) {

                    ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();
                    Map<String, Engine> engineBeanMap = applicationContext.getBeansOfType(Engine.class);
                    for (Map.Entry<String, Engine> entry: engineBeanMap.entrySet()) {

                        Engine engine = entry.getValue();
                        engine.initServices();
                    }
                }
            }
        };
    }

}


