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
package ch.algotrader.wiring.external;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.service.ExternalMarketDataService;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.MarketDataService;
import ch.algotrader.service.MarketDataSubscriber;

/**
 * Market data subscriber.
 */
@Configuration
@Profile({"iBMarketData", "bBMarketData", "dcMarkteData", "lMAXMarketData", "fXCMMarketData", "cNXMarketData", "fTXMarketData", "tTMarketData"})
public class MarketDataSubscriberWiring {

    @Bean(name = "marketDataSubscriber", destroyMethod = "destroy")
    public MarketDataSubscriber createFixMarketDataSubscriber(
            final EventDispatcher eventDispatcher,
            final LookupService lookupService,
            final MarketDataService marketDataService,
            final ApplicationContext applicationContext) {

        Map<String, ExternalMarketDataService> beanMap1 = applicationContext.getBeansOfType(ExternalMarketDataService.class);
        Map<String, String> beanMap2 = beanMap1.values().stream()
                .collect(Collectors.toMap(ExternalMarketDataService::getSessionQualifier, ExternalMarketDataService::getFeedType));

        return new MarketDataSubscriber(eventDispatcher, lookupService, marketDataService, beanMap2);
    }

}
