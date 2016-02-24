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
package ch.algotrader.wiring.core;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import ch.algotrader.esper.Engine;
import ch.algotrader.service.AlgoOrderService;
import ch.algotrader.service.ForexService;
import ch.algotrader.service.SimpleOrderService;
import ch.algotrader.service.TransactionService;

/**
 * Server Engine post-initialization configuration. Ideally should be eliminated in the future.
 */
@Configuration
public class ServerEnginePostInitWiring {

    @Bean(name = "serverEnginePostprocessor")
    public ApplicationListener<ContextRefreshedEvent> createEngineManagerPostprocessor() {

        return new ApplicationListener<ContextRefreshedEvent>() {

            private final AtomicBoolean postProcessed = new AtomicBoolean(false);

            @Override
            public void onApplicationEvent(final ContextRefreshedEvent contextRefreshedEvent) {
                if (this.postProcessed.compareAndSet(false, true)) {

                    ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();
                    Engine serverEngine = applicationContext.getBean("serverEngine", Engine.class);
                    TransactionService transactionService = applicationContext.getBean("transactionService", TransactionService.class);
                    ForexService forexService = applicationContext.getBean("forexService", ForexService.class);
                    SimpleOrderService simpleOrderService = applicationContext.getBean("simpleOrderService", SimpleOrderService.class);
                    AlgoOrderService algoOrderService = applicationContext.getBean("algoOrderService", AlgoOrderService.class);
                    serverEngine.setVariableValue("transactionService", transactionService);
                    serverEngine.setVariableValue("forexService", forexService);
                    serverEngine.setVariableValue("simpleOrderService", simpleOrderService);
                    serverEngine.setVariableValue("algoOrderService", algoOrderService);
                }
            }
        };
    }

}


