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
package ch.algotrader.wiring.client.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.service.MarketDataCache;
import ch.algotrader.service.MarketDataCacheImpl;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.MarketDataService;
import ch.algotrader.service.SubscriptionService;
import ch.algotrader.service.SubscriptionServiceImpl;

/**
 * Client service configuration.
 */
@Configuration
public class ClientServicesWiring {

    @Bean(name = "subscriptionService")
    public SubscriptionService createSubscriptionService(
            final CommonConfig commonConfig,
            final MarketDataService marketDataService,
            final LookupService lookupService,
            final EngineManager engineManager) {

        return new SubscriptionServiceImpl(commonConfig, marketDataService, lookupService, engineManager);
    }

    @Bean(name = "marketDataCache")
    public MarketDataCache createMarketDataCache(
            final CommonConfig commonConfig,
            final EngineManager engineManager,
            final LookupService lookupService) {

        return new MarketDataCacheImpl(commonConfig, engineManager, lookupService);
    }

}

