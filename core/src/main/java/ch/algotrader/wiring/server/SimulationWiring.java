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
import ch.algotrader.dao.PositionDao;
import ch.algotrader.dao.SubscriptionDao;
import ch.algotrader.dao.TransactionDao;
import ch.algotrader.dao.marketData.BarDao;
import ch.algotrader.dao.marketData.TickDao;
import ch.algotrader.dao.property.PropertyDao;
import ch.algotrader.dao.security.CombinationDao;
import ch.algotrader.dao.security.ComponentDao;
import ch.algotrader.dao.security.FutureDao;
import ch.algotrader.dao.security.OptionDao;
import ch.algotrader.dao.strategy.CashBalanceDao;
import ch.algotrader.dao.strategy.MeasurementDao;
import ch.algotrader.dao.strategy.PortfolioValueDao;
import ch.algotrader.dao.strategy.StrategyDao;
import ch.algotrader.dao.trade.OrderDao;
import ch.algotrader.dao.trade.OrderPropertyDao;
import ch.algotrader.dao.trade.OrderStatusDao;
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
            final ServerLookupService serverLookupService,
            final EventListenerRegistry eventListenerRegistry,
            final EventDispatcher eventDispatcher,
            final EngineManager engineManager,
            final Engine serverEngine,
            final CacheManager cacheManager) {

        return new SimulationExecutorImpl(commonConfig, positionService, resetService, transactionService, portfolioService, strategyPersistenceService,
                lookupService, serverLookupService, eventListenerRegistry, eventDispatcher, engineManager, serverEngine, cacheManager);
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
    public ResetService createResetService(final CoreConfig coreConfig,
            final OrderDao orderDao,
            final OrderStatusDao orderStatusDao,
            final OrderPropertyDao orderPropertyDao,
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
            final MeasurementDao measurementDao,
            final PortfolioValueDao portfolioValueDao,
            final BarDao barDao,
            final TickDao tickDao) {

        return new ResetServiceImpl(coreConfig, orderDao, orderStatusDao, orderPropertyDao, futureDao, transactionDao, positionDao, subscriptionDao, optionDao, strategyDao, cashBalanceDao,
                combinationDao, componentDao, propertyDao, measurementDao, portfolioValueDao, barDao, tickDao);
    }

}
