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

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.esper.Engine;
import ch.algotrader.service.CalendarService;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.MarketDataService;
import ch.algotrader.service.OptionService;
import ch.algotrader.service.OrderService;
import ch.algotrader.service.PortfolioService;
import ch.algotrader.service.PositionService;

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
                    LookupService lookupService = applicationContext.getBean("lookupService", LookupService.class);
                    PortfolioService portfolioService = applicationContext.getBean("portfolioService", PortfolioService.class);
                    CalendarService calendarService = applicationContext.getBean("calendarService", CalendarService.class);
                    OrderService orderService = applicationContext.getBean("orderService", OrderService.class);
                    PositionService positionService = applicationContext.getBean("positionService", PositionService.class);
                    MarketDataService marketDataService = applicationContext.getBean("marketDataService", MarketDataService.class);
                    OptionService optionService = applicationContext.getBean("optionService", OptionService.class);

                    Map<String, Engine> engineBeanMap = applicationContext.getBeansOfType(Engine.class);
                    for (Map.Entry<String, Engine> entry: engineBeanMap.entrySet()) {

                        Engine engine = entry.getValue();
                        engine.setVariableValue("lookupService", lookupService);
                        engine.setVariableValue("portfolioService", portfolioService);
                        engine.setVariableValue("calendarService", calendarService);
                        engine.setVariableValue("orderService", orderService);
                        engine.setVariableValue("positionService", positionService);
                        engine.setVariableValue("marketDataService", marketDataService);
                        engine.setVariableValue("optionService", optionService);
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


