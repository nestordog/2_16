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

import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import ch.algotrader.cache.CacheManager;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.ConfigParams;
import ch.algotrader.config.CoreConfig;
import ch.algotrader.dao.AccountDao;
import ch.algotrader.dao.GenericDao;
import ch.algotrader.dao.PositionDao;
import ch.algotrader.dao.SubscriptionDao;
import ch.algotrader.dao.TransactionDao;
import ch.algotrader.dao.exchange.ExchangeDao;
import ch.algotrader.dao.marketData.BarDao;
import ch.algotrader.dao.marketData.TickDao;
import ch.algotrader.dao.property.PropertyDao;
import ch.algotrader.dao.property.PropertyHolderDao;
import ch.algotrader.dao.security.CombinationDao;
import ch.algotrader.dao.security.ComponentDao;
import ch.algotrader.dao.security.ForexDao;
import ch.algotrader.dao.security.FutureDao;
import ch.algotrader.dao.security.FutureFamilyDao;
import ch.algotrader.dao.security.OptionDao;
import ch.algotrader.dao.security.OptionFamilyDao;
import ch.algotrader.dao.security.SecurityDao;
import ch.algotrader.dao.security.SecurityFamilyDao;
import ch.algotrader.dao.strategy.CashBalanceDao;
import ch.algotrader.dao.strategy.MeasurementDao;
import ch.algotrader.dao.strategy.PortfolioValueDao;
import ch.algotrader.dao.strategy.StrategyDao;
import ch.algotrader.dao.trade.LimitOrderDao;
import ch.algotrader.dao.trade.MarketOrderDao;
import ch.algotrader.dao.trade.OrderDao;
import ch.algotrader.dao.trade.OrderPreferenceDao;
import ch.algotrader.dao.trade.OrderPropertyDao;
import ch.algotrader.dao.trade.OrderStatusDao;
import ch.algotrader.dao.trade.StopLimitOrderDao;
import ch.algotrader.dao.trade.StopOrderDao;
import ch.algotrader.entity.trade.algo.AlgoOrder;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.ordermgmt.DefaultOrderBook;
import ch.algotrader.ordermgmt.OrderBook;
import ch.algotrader.service.AlgoOrderService;
import ch.algotrader.service.AlgoOrderServiceImpl;
import ch.algotrader.service.CalendarService;
import ch.algotrader.service.CalendarServiceImpl;
import ch.algotrader.service.CombinationService;
import ch.algotrader.service.CombinationServiceImpl;
import ch.algotrader.service.EventPropagator;
import ch.algotrader.service.ExternalMarketDataService;
import ch.algotrader.service.ForexService;
import ch.algotrader.service.ForexServiceImpl;
import ch.algotrader.service.FutureService;
import ch.algotrader.service.FutureServiceImpl;
import ch.algotrader.service.LazyLoaderService;
import ch.algotrader.service.LazyLoaderServiceImpl;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.LookupServiceImpl;
import ch.algotrader.service.MarketDataCacheService;
import ch.algotrader.service.MarketDataCacheServiceImpl;
import ch.algotrader.service.MarketDataPersistenceService;
import ch.algotrader.service.MarketDataPersistenceServiceImpl;
import ch.algotrader.service.MarketDataService;
import ch.algotrader.service.MarketDataServiceImpl;
import ch.algotrader.service.MeasurementService;
import ch.algotrader.service.MeasurementServiceImpl;
import ch.algotrader.service.OptionService;
import ch.algotrader.service.OptionServiceImpl;
import ch.algotrader.service.OrderExecutionService;
import ch.algotrader.service.OrderExecutionServiceImpl;
import ch.algotrader.service.OrderPersistenceService;
import ch.algotrader.service.OrderPersistenceServiceImpl;
import ch.algotrader.service.OrderService;
import ch.algotrader.service.OrderServiceImpl;
import ch.algotrader.service.PortfolioService;
import ch.algotrader.service.PortfolioServiceImpl;
import ch.algotrader.service.PositionService;
import ch.algotrader.service.PositionServiceImpl;
import ch.algotrader.service.PropertyService;
import ch.algotrader.service.PropertyServiceImpl;
import ch.algotrader.service.ResetService;
import ch.algotrader.service.ResetServiceImpl;
import ch.algotrader.service.ServerLookupService;
import ch.algotrader.service.ServerLookupServiceImpl;
import ch.algotrader.service.ServerManagementService;
import ch.algotrader.service.ServerManagementServiceImpl;
import ch.algotrader.service.ServerStateLoaderService;
import ch.algotrader.service.ServerStateLoaderServiceImpl;
import ch.algotrader.service.SimpleOrderExecService;
import ch.algotrader.service.SimpleOrderService;
import ch.algotrader.service.SimpleOrderServiceImpl;
import ch.algotrader.service.StrategyPersistenceService;
import ch.algotrader.service.StrategyPersistenceServiceImpl;
import ch.algotrader.service.SubscriptionService;
import ch.algotrader.service.SubscriptionServiceImpl;
import ch.algotrader.service.TickPersister;
import ch.algotrader.service.TransactionPersistenceService;
import ch.algotrader.service.TransactionService;
import ch.algotrader.service.TransactionServiceImpl;
import ch.algotrader.service.algo.AlgoOrderExecService;
import ch.algotrader.service.algo.SlicingOrderService;
import ch.algotrader.service.algo.TickwiseIncrementalOrderService;
import ch.algotrader.service.algo.VariableIncrementalOrderService;
import ch.algotrader.service.h2.H2TransactionPersistenceServiceImpl;
import ch.algotrader.service.mysql.MySqlTransactionPersistenceServiceImpl;

/**
 * Core service configuration.
 */
@Configuration
public class ServiceWiring {

    @Bean(name = "lookupService")
    public LookupService createLookupService(
            final CoreConfig coreConfig,
            final GenericDao genericDao,
            final CacheManager cacheManager) {

        return new LookupServiceImpl(genericDao, cacheManager);
    }

    @Bean(name = "portfolioService")
    public PortfolioService createPortfolioService(final CommonConfig commonConfig,
            final CoreConfig coreConfig,
            final SessionFactory sessionFactory,
            final MarketDataCacheService marketDataCacheService,
            final GenericDao genericDao,
            final StrategyDao strategyDao,
            final TransactionDao transactionDao,
            final PositionDao positionDao,
            final CashBalanceDao cashBalanceDao,
            final PortfolioValueDao portfolioValueDao,
            final TickDao tickDao,
            final ForexDao forexDao,
            final EngineManager engineManager) {

        return new PortfolioServiceImpl(commonConfig, coreConfig, sessionFactory, marketDataCacheService, genericDao, strategyDao, transactionDao, positionDao, cashBalanceDao,
                portfolioValueDao, tickDao, forexDao, engineManager);
    }

    @Bean(name = "positionService")
    public PositionService createPositionService(final CommonConfig commonConfig,
            final CoreConfig coreConfig,
            final TransactionService transactionService,
            final MarketDataService marketDataService,
            final OrderService orderService,
            final MarketDataCacheService marketDataCacheService,
            final PositionDao positionDao,
            final StrategyDao strategyDao,
            final TransactionDao transactionDao,
            final EngineManager engineManager,
            final Engine serverEngine) {

        return new PositionServiceImpl(commonConfig, coreConfig, transactionService, marketDataService, orderService, marketDataCacheService, positionDao, strategyDao, transactionDao,
                engineManager, serverEngine);
    }

    @Bean(name = "futureService")
    public FutureService createFutureService(final CommonConfig commonConfig,
            final CoreConfig coreConfig,
            final FutureFamilyDao futureFamilyDao,
            final FutureDao futureDao,
            final EngineManager engineManager) {

        return new FutureServiceImpl(commonConfig, coreConfig, futureFamilyDao, futureDao, engineManager);
    }

    @Bean(name = "serverManagementService")
    public ServerManagementService createServerManagementService(final PositionService positionService,
            final ForexService forexService,
            final CombinationService combinationService,
            final TransactionService transactionService,
            final OptionService optionService,
            final OrderService orderService,
            final EngineManager engineManager) {

        return new ServerManagementServiceImpl(positionService, forexService, combinationService, transactionService, optionService, orderService, engineManager);
    }

    @Bean(name = "optionService")
    public OptionService createOptionService(final CommonConfig commonConfig,
            final CoreConfig coreConfig,
            final MarketDataService marketDataService,
            final FutureService futureService,
            final OrderService orderService,
            final MarketDataCacheService marketDataCacheService,
            final SecurityDao securityDao,
            final OptionFamilyDao optionFamilyDao,
            final OptionDao optionDao,
            final PositionDao positionDao,
            final SubscriptionDao subscriptionDao,
            final FutureFamilyDao futureFamilyDao,
            final StrategyDao strategyDao,
            final EngineManager engineManager,
            final Engine serverEngine) {

        return new OptionServiceImpl(commonConfig, coreConfig, marketDataService, futureService, orderService, marketDataCacheService, securityDao, optionFamilyDao, optionDao, positionDao,
                subscriptionDao, futureFamilyDao, strategyDao, engineManager, serverEngine);
    }

    @Bean(name = "forexService")
    public ForexService createForexService(final CommonConfig commonConfig,
            final CoreConfig coreConfig,
            final OrderService orderService,
            final PortfolioService portfolioService,
            final LookupService lookupService,
            final FutureService futureService,
            final MarketDataService marketDataService,
            final ForexDao forexDao,
            final StrategyDao strategyDao,
            final SubscriptionDao subscriptionDao,
            final FutureFamilyDao futureFamilyDao,
            final EngineManager engineManager) {

        return new ForexServiceImpl(commonConfig, coreConfig, orderService, portfolioService, lookupService, futureService, marketDataService, forexDao, strategyDao, subscriptionDao, futureFamilyDao,
                engineManager);
    }

    @Bean(name = "transactionService")
    public TransactionService createTransactionService(final CommonConfig commonConfig,
            final CoreConfig coreConfig,
            final TransactionPersistenceService transactionPersistenceService,
            final StrategyDao strategyDao,
            final SecurityDao securityDao,
            final AccountDao accountDao,
            final EventDispatcher eventDispatcher,
            final Engine serverEngine) {

        return new TransactionServiceImpl(commonConfig, coreConfig, transactionPersistenceService, strategyDao, securityDao, accountDao, eventDispatcher, serverEngine);
    }

    @Bean(name = "marketDataService")
    public MarketDataService createMarketDataService(final CommonConfig commonConfig,
            final CoreConfig coreConfig,
            final ConfigParams configParams,
            final SecurityDao securityDao,
            final StrategyDao strategyDao,
            final SubscriptionDao subscriptionDao,
            final ForexDao forexDao,
            final EngineManager engineManager,
            final EventDispatcher eventDispatcher,
            final MarketDataCacheService marketDataCacheService,
            final ApplicationContext applicationContext) {

        Map<String, ExternalMarketDataService> serviceMap1 = applicationContext.getBeansOfType(ExternalMarketDataService.class);
        Map<String, ExternalMarketDataService> serviceMap2 = serviceMap1.values().stream()
                .collect(Collectors.toMap(ExternalMarketDataService::getFeedType, service -> service));

        return new MarketDataServiceImpl(commonConfig, coreConfig, configParams, securityDao, strategyDao, subscriptionDao, forexDao,
                engineManager, eventDispatcher, marketDataCacheService, serviceMap2);
    }

    @Bean(name = "marketDataPersistenceService")
    public MarketDataPersistenceService createMarketDataPersistenceService(final TickDao tickDao) {

        return new MarketDataPersistenceServiceImpl(tickDao);
    }

    @Bean(name = "tickPersister")
    public TickPersister createTickPersistEventPropagator(
            final Engine serverEngine,
            final LookupService lookupService,
            final MarketDataPersistenceService marketDataPersistenceService) {

        return new TickPersister(serverEngine, lookupService, marketDataPersistenceService);
    }

    @Bean(name = "orderPersistenceService")
    public OrderPersistenceService createOrderPersistenceService(final OrderDao orderDao,
            final MarketOrderDao marketOrderDao,
            final LimitOrderDao limitOrderDao,
            final StopOrderDao stopOrderDao,
            final StopLimitOrderDao stopLimitOrderDao,
            final OrderPropertyDao orderPropertyDao,
            final OrderStatusDao orderStatusDao) {

        return new OrderPersistenceServiceImpl(orderDao, marketOrderDao, limitOrderDao, stopOrderDao, stopLimitOrderDao, orderPropertyDao, orderStatusDao);
    }

    @Bean(name = "orderPersistExecutor")
    public ThreadPoolTaskExecutor createOrderPersistExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(1);
        taskExecutor.setMaxPoolSize(1);
        return taskExecutor;
    }

    @Bean(name = "openOrderRegistry")
    public OrderBook createOpenOrderRegistry() {
        return new DefaultOrderBook();
    }

    @Bean(name = "orderExecutionService")
    public OrderExecutionService createOrderExecutionService(final CommonConfig commonConfig,
           final OrderPersistenceService orderPersistService,
           final OrderBook orderBook,
           final EventDispatcher eventDispatcher,
           final EngineManager engineManager,
           final Engine serverEngine) {

        return new OrderExecutionServiceImpl(commonConfig, orderPersistService, orderBook, eventDispatcher, engineManager, serverEngine);
    }

    @Bean(name = "simpleOrderService")
    public SimpleOrderService createServerOrderService(
            final CommonConfig commonConfig,
            final Engine serverEngine,
            final ApplicationContext applicationContext) {

        Map<String, SimpleOrderExecService> serviceMap1 = applicationContext.getBeansOfType(SimpleOrderExecService.class);
        Map<String, SimpleOrderExecService> serviceMap2 = serviceMap1.values().stream()
                .collect(Collectors.toMap(SimpleOrderExecService::getOrderServiceType, service -> service));

        return new SimpleOrderServiceImpl(commonConfig, serverEngine, serviceMap2);
    }

    @Bean(name = "algoOrderService")
    public AlgoOrderService createAlgoOrderService(
            final OrderBook orderBook,
            final Engine serverEngine,
            final ApplicationContext applicationContext) {

        Map<String, AlgoOrderExecService> serviceMap1 = applicationContext.getBeansOfType(AlgoOrderExecService.class);
        Map<Class<? extends AlgoOrder>, AlgoOrderExecService> serviceMap2 = serviceMap1.values().stream()
                .collect(Collectors.toMap(AlgoOrderExecService::getAlgoOrderType, service -> service));

        return new AlgoOrderServiceImpl(orderBook, serverEngine, serviceMap2);
    }

    @Bean(name = "slicingOrderService")
    public SlicingOrderService createSlicingOrderService(
            final OrderExecutionService orderExecutionService,
            final MarketDataCacheService marketDataCacheService,
            final SimpleOrderService simpleOrderService,
            final OrderBook orderBook) {

        return new SlicingOrderService(orderExecutionService, marketDataCacheService, simpleOrderService);
    }

    @Bean(name = "tickwiseIncrementalOrderService")
    public TickwiseIncrementalOrderService createTickwiseIncrementalOrderService(
            final OrderExecutionService orderExecutionService,
            final MarketDataCacheService marketDataCacheService,
            final SimpleOrderService simpleOrderService) {

        return new TickwiseIncrementalOrderService(orderExecutionService, marketDataCacheService, simpleOrderService);
    }

    @Bean(name = "variableIncrementalOrderService")
    public VariableIncrementalOrderService createVariableIncrementalOrderService(
            final OrderExecutionService orderExecutionService,
            final MarketDataCacheService marketDataCacheService,
            final SimpleOrderService simpleOrderService) {

        return new VariableIncrementalOrderService(orderExecutionService, marketDataCacheService, simpleOrderService);
    }

    @Bean(name = "orderService")
    public OrderService createOrderService(
            final SimpleOrderService simpleOrderService,
            final AlgoOrderService algoOrderService,
            final OrderDao orderDao,
            final StrategyDao strategyDao,
            final SecurityDao securityDao,
            final AccountDao accountDao,
            final ExchangeDao exchangeDao,
            final OrderPreferenceDao orderPreferenceDao,
            final OrderBook orderBook) {

        return new OrderServiceImpl(simpleOrderService, algoOrderService, orderDao, strategyDao, securityDao, accountDao, exchangeDao, orderPreferenceDao,
                orderBook);
    }

    @Bean(name = "combinationService")
    public CombinationService createCombinationService(final CommonConfig commonConfig,
            final MarketDataService marketDataService,
            final SubscriptionDao subscriptionDao,
            final CombinationDao combinationDao,
            final SecurityDao securityDao,
            final ComponentDao componentDao,
            final SecurityFamilyDao securityFamilyDao,
            final Engine serverEngine) {

        return new CombinationServiceImpl(commonConfig, marketDataService, subscriptionDao, combinationDao, securityDao, componentDao,
                securityFamilyDao, serverEngine);
    }

    @Bean(name = "measurementService")
    public MeasurementService createMeasurementService(final MeasurementDao measurementDao,
            final StrategyDao strategyDao,
            final EngineManager engineManager) {

        return new MeasurementServiceImpl(measurementDao, strategyDao, engineManager);
    }

    @Bean(name = "propertyService")
    public PropertyService createPropertyService(final PropertyDao propertyDao,
            final PropertyHolderDao propertyHolderDao) {

        return new PropertyServiceImpl(propertyDao, propertyHolderDao);
    }

    @Bean(name = "calendarService")
    public CalendarService createCalendarService(final ExchangeDao exchangeDao,
            final EngineManager engineManager) {

        return new CalendarServiceImpl(exchangeDao, engineManager);
    }

    @Bean(name = "subscriptionService")
    public SubscriptionService createSubscriptionService(final CommonConfig commonConfig,
            final MarketDataService marketDataService,
            final LookupService lookupService,
            final EngineManager engineManager) {

        return new SubscriptionServiceImpl(commonConfig, marketDataService, lookupService, engineManager);
    }

    @Bean(name = "marketDataCacheService")
    public MarketDataCacheService createMarketDataCacheService(
            final CommonConfig commonConfig,
            final CoreConfig coreConfig,
            final EngineManager engineManager,
            final LookupService lookupService) {

        return new MarketDataCacheServiceImpl(engineManager, lookupService, commonConfig.getPortfolioBaseCurrency(), coreConfig.getIntervalDays());
    }

    @Bean(name = "strategyPersistenceService")
    public StrategyPersistenceService createStrategyPersistenceService(final StrategyDao strategyDao) {

        return new StrategyPersistenceServiceImpl(strategyDao);
    }

    @Bean(name = "serverLookupService")
    public ServerLookupService createServerLookupService(
            final SecurityDao securityDao,
            final SubscriptionDao subscriptionDao,
            final TickDao tickDao,
            final BarDao barDao) {
        return new ServerLookupServiceImpl(tickDao, securityDao, subscriptionDao, barDao);
    }

    @Bean(name = "serverStateLoaderService")
    public ServerStateLoaderService createServerStateLoaderService(
            final SessionFactory sessionFactory,
            final OrderDao orderDao,
            final OrderStatusDao orderStatusDao,
            final PositionDao positionDao,
            final CashBalanceDao cashBalanceDao,
            final OrderBook orderBook,
            final EventDispatcher eventDispatcher) {
        return new ServerStateLoaderServiceImpl(sessionFactory, orderDao, orderStatusDao, positionDao, cashBalanceDao, orderBook, eventDispatcher);
    }

    @Bean(name = "eventPropagator")
    public EventPropagator createEventPropagator(
            final OrderExecutionService orderExecutionService,
            final EventDispatcher eventDispatcher,
            final CommonConfig commonConfig) {

        return new EventPropagator(orderExecutionService, eventDispatcher, commonConfig);
    }

    @Bean(name = "lazyLoaderService")
    public LazyLoaderService createLazyLoaderService() {

        return new LazyLoaderServiceImpl();
    }

    @Profile({"singleDataSource","pooledDataSource"})
    @Bean(name = "transactionPersistenceService")
    public TransactionPersistenceService createMySqlTransactionPersistenceService(
            final CommonConfig commonConfig,
            final PortfolioService portfolioService,
            final SessionFactory sessionFactory,
            final PositionDao positionDao,
            final TransactionDao transactionDao,
            final CashBalanceDao cashBalanceDao) {

        return new MySqlTransactionPersistenceServiceImpl(commonConfig, portfolioService, sessionFactory, positionDao, transactionDao, cashBalanceDao);
    }

    @Profile("embeddedDataSource")
    @Bean(name = "transactionPersistenceService")
    public TransactionPersistenceService createH2TransactionPersistenceService(
            final CommonConfig commonConfig,
            final PortfolioService portfolioService,
            final SessionFactory sessionFactory,
            final PositionDao positionDao,
            final TransactionDao transactionDao,
            final CashBalanceDao cashBalanceDao) {

        return new H2TransactionPersistenceServiceImpl(commonConfig, portfolioService, sessionFactory, positionDao, transactionDao, cashBalanceDao);
    }

    @Bean(name = "resetService")
    public ResetService createResetService(
            final CoreConfig coreConfig,
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
