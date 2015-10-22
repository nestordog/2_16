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

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.esper.EngineManagerImpl;

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

}


