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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import ch.algotrader.cache.CacheManager;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.CoreConfig;
import ch.algotrader.entity.PositionDao;
import ch.algotrader.entity.SubscriptionDao;
import ch.algotrader.entity.TransactionDao;
import ch.algotrader.entity.property.PropertyDao;
import ch.algotrader.entity.security.CombinationDao;
import ch.algotrader.entity.security.ComponentDao;
import ch.algotrader.entity.security.FutureDao;
import ch.algotrader.entity.security.OptionDao;
import ch.algotrader.entity.security.SecurityDao;
import ch.algotrader.entity.strategy.CashBalanceDao;
import ch.algotrader.entity.strategy.MeasurementDao;
import ch.algotrader.entity.strategy.StrategyDao;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.event.EventListenerRegistry;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.service.LocalLookupService;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.OrderService;
import ch.algotrader.service.PortfolioService;
import ch.algotrader.service.PositionService;
import ch.algotrader.service.ResetService;
import ch.algotrader.service.ResetServiceImpl;
import ch.algotrader.service.StrategyPersistenceService;
import ch.algotrader.service.TransactionService;
import ch.algotrader.service.sim.SimulationOrderService;
import ch.algotrader.service.sim.SimulationOrderServiceImpl;
import ch.algotrader.simulation.SimulationExecutor;
import ch.algotrader.simulation.SimulationExecutorImpl;

/**
 * Simulation profile configuration.
 */
@Configuration
public class SimulationWiring {

    @Profile("simulation")
    @Bean(name = "simulationExecutor")
    public SimulationExecutor createSimulationExecutor(
            final CommonConfig commonConfig,
            final PositionService positionService,
            final ResetService resetService,
            final TransactionService transactionService,
            final PortfolioService portfolioService,
            final StrategyPersistenceService strategyPersistenceService,
            final LookupService lookupService,
            final EventListenerRegistry eventListenerRegistry,
            final EventDispatcher eventDispatcher,
            final EngineManager engineManager,
            final Engine serverEngine,
            final CacheManager cacheManager) {

        return new SimulationExecutorImpl(commonConfig, positionService, resetService, transactionService, portfolioService, strategyPersistenceService,
                lookupService, eventListenerRegistry, eventDispatcher, engineManager, serverEngine, cacheManager);
    }

    @Profile({"simulation", "noopSimulation"})
    @Bean(name = "simulationOrderService")
    public SimulationOrderService createSimulationOrderService(final TransactionService transactionService,
            final OrderService orderService,
            final LocalLookupService localLookupService,
            final EngineManager engineManager,
            final Engine serverEngine) {

        return new SimulationOrderServiceImpl(transactionService, orderService, localLookupService, engineManager, serverEngine);
    }

    @Profile({"simulation", "noopSimulation"})
    @Bean(name = "resetService")
    public ResetService createResetService(
            final CoreConfig coreConfig,
            final SecurityDao securityDao,
            final FutureDao futureDao,
            final TransactionDao transactionDao,
            final PositionDao positionDao,
            final SubscriptionDao subscriptionDao,
            final OptionDao optionDao,
            final StrategyDao strategyDao,
            final CashBalanceDao cashBalanceDao,
            final CombinationDao combinationDao,
            final ComponentDao componentDao,
            final PropertyDao propertyDao,
            final MeasurementDao measurementDao) {

        return new ResetServiceImpl(coreConfig, securityDao, futureDao, transactionDao, positionDao, subscriptionDao, optionDao, strategyDao, cashBalanceDao,
                combinationDao, componentDao, propertyDao, measurementDao);
    }

}
