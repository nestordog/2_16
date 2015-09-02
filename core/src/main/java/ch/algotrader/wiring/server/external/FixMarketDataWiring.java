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
package ch.algotrader.wiring.server.external;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import ch.algotrader.adapter.fix.MarketDataFixSessionStateHolder;
import ch.algotrader.service.MarketDataService;

/**
 * Fix market data subscriber.
 */
@Configuration
@Profile({"dcMarkteData", "lMAXMarketData", "fXCMMarketData", "cNXMarketData", "fTXMarketData"})
public class FixMarketDataWiring {

    @Bean(name = "fixMarketDataSubscriber")
    public FixMarketDataSubscriber createFixMarketDataSubscriber(final MarketDataService marketDataService, final ApplicationContext applicationContext) {

        Map<String, MarketDataFixSessionStateHolder> beanMap1 = applicationContext.getBeansOfType(MarketDataFixSessionStateHolder.class);
        Map<String, MarketDataFixSessionStateHolder> beanMap2 = beanMap1.values().stream()
                .collect(Collectors.toMap(MarketDataFixSessionStateHolder::getName, bean -> bean));

        return new FixMarketDataSubscriber(marketDataService, beanMap2);
    }

}
