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
import org.springframework.context.annotation.Profile;

import ch.algotrader.cache.CacheManager;
import ch.algotrader.cache.CacheManagerImpl;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.ConfigParams;
import ch.algotrader.dao.GenericDao;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.lifecycle.LifecycleManager;
import ch.algotrader.lifecycle.LifecycleManagerImpl;
import ch.algotrader.service.CombinationService;
import ch.algotrader.service.LocalLookupService;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.LookupServiceImpl;
import ch.algotrader.service.ManagementService;
import ch.algotrader.service.ManagementServiceImpl;
import ch.algotrader.service.MarketDataService;
import ch.algotrader.service.OrderService;
import ch.algotrader.service.PortfolioChartService;
import ch.algotrader.service.PortfolioChartServiceImpl;
import ch.algotrader.service.PortfolioService;
import ch.algotrader.service.PositionService;
import ch.algotrader.service.PropertyService;
import ch.algotrader.service.SubscriptionService;
import ch.algotrader.vo.client.ChartDefinitionVO;

/**
 * Management Service configuration.
 *
 * @version $Revision$ $Date$
 */
@Profile(value = "live")
@Configuration
public class LiveClientServiceWiring {

    @Bean(name = "managementService")
    public ManagementService createManagementService(
            final CommonConfig commonConfig,
            final EngineManager engineManager,
            final SubscriptionService subscriptionService,
            final LookupService lookupService,
            final LocalLookupService localLookupService,
            final PortfolioService portfolioService,
            final OrderService orderService,
            final PositionService positionService,
            final CombinationService combinationService,
            final PropertyService propertyService,
            final MarketDataService marketDataService,
            final ConfigParams configParams) {

        return new ManagementServiceImpl(commonConfig, engineManager, subscriptionService, lookupService, localLookupService, portfolioService, orderService, positionService,
                combinationService, propertyService, marketDataService, configParams);
    }

    @Bean(name = "portfolioChartService")
    public PortfolioChartService createPortfolioChartService(
            final ChartDefinitionVO portfolioChartDefinition,
            final EngineManager engineManager,
            final PortfolioService portfolioService) {

        return new PortfolioChartServiceImpl(portfolioChartDefinition, engineManager, portfolioService);
    }

    @Bean(name = "lifecycleManager")
    public static LifecycleManager createLifecycleManager(
            final EngineManager engineManager,
            final EventDispatcher eventDispatcher,
            final SubscriptionService subscriptionService) {

        return new LifecycleManagerImpl(engineManager, eventDispatcher, subscriptionService);
    }

    @Bean(name = "cacheManager")
    public CacheManager createCacheManager(final GenericDao genericDao) {

        return new CacheManagerImpl(genericDao);
    }

    @Bean(name = "lookupService")
    public LookupService createLookupService(final GenericDao genericDao, final CacheManager cacheManager) {

        return new LookupServiceImpl(genericDao, cacheManager);
    }
}
