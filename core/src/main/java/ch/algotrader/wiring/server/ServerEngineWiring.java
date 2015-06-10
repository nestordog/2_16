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
package ch.algotrader.wiring.server;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import ch.algotrader.config.ConfigParams;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineFactory;
import ch.algotrader.esper.SpringSubscriberResolver;

/**
 * Server engine configuration.
 */
@Configuration
public class ServerEngineWiring {

    @Bean(name = "serverEngine")
    public Engine createServerEngine(final ConfigParams configParams, final ApplicationContext applicationContext) throws Exception {

        EngineFactory engineFactory = new EngineFactory(new SpringSubscriberResolver("server", configParams, applicationContext), configParams);

        Resource resource1 = applicationContext.getResource("classpath:/META-INF/esper-common.cfg.xml");
        Resource resource2 = applicationContext.getResource("classpath:/META-INF/esper-core.cfg.xml");

        List<URL> configResources = Arrays.asList(resource1.getURL(), resource2.getURL());
        List<String> initModules = Arrays.asList(
                "market-data",
                "combination",
                "current-values",
                "market-data-simulation",
                "trades",
                "portfolio",
                "performance",
                "algo-slicing",
                "ib"
        );

        return engineFactory.createServer(configResources, initModules);
    }

}
