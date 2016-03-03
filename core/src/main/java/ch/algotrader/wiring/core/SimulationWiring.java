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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import ch.algotrader.cache.CacheManager;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.event.EventListenerRegistry;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.ordermgmt.OrderBook;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.MarketDataCacheService;
import ch.algotrader.service.OrderExecutionService;
import ch.algotrader.service.PortfolioService;
import ch.algotrader.service.PositionService;
import ch.algotrader.service.ResetService;
import ch.algotrader.service.ServerLookupService;
import ch.algotrader.service.StrategyPersistenceService;
import ch.algotrader.service.TransactionService;
import ch.algotrader.service.sim.SimulationOrderService;
import ch.algotrader.service.sim.SimulationOrderServiceImpl;
import ch.algotrader.simulation.SimulationExecutor;
import ch.algotrader.simulation.SimulationExecutorImpl;

/**
 * Simulation profile configuration.
 */
@Profile("simulation")
@Configuration
public class SimulationWiring {

    @Bean(name = "simulationExecutor")
    public SimulationExecutor createSimulationExecutor(
            final CommonConfig commonConfig,
            final PositionService positionService,
            final ResetService resetService,
            final TransactionService transactionService,
            final PortfolioService portfolioService,
            final StrategyPersistenceService strategyPersistenceService,
            final LookupService lookupService,
            final ServerLookupService serverLookupService,
            final EventListenerRegistry eventListenerRegistry,
            final EventDispatcher eventDispatcher,
            final EngineManager engineManager,
            final Engine serverEngine,
            final CacheManager cacheManager) {

        return new SimulationExecutorImpl(commonConfig, positionService, resetService, transactionService, portfolioService, strategyPersistenceService,
                lookupService, serverLookupService, eventListenerRegistry, eventDispatcher, engineManager, serverEngine, cacheManager);
    }

    @Bean(name = "simulationOrderService")
    public SimulationOrderService createSimulationOrderService(
            final OrderBook orderBook,
            final OrderExecutionService orderExecutionService,
            final MarketDataCacheService marketDataCacheService,
            final EngineManager engineManager) {

        return new SimulationOrderServiceImpl(orderBook, orderExecutionService, marketDataCacheService, engineManager);
    }

}
