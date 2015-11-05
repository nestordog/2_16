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

import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import ch.algotrader.adapter.AutoIncrementIdGenerator;
import ch.algotrader.adapter.IdGenerator;
import ch.algotrader.adapter.ib.AccountUpdate;
import ch.algotrader.adapter.ib.IBExecutions;
import ch.algotrader.adapter.ib.IBOrderMessageFactory;
import ch.algotrader.adapter.ib.IBPendingRequests;
import ch.algotrader.adapter.ib.IBSession;
import ch.algotrader.adapter.ib.IBSessionStateHolder;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.IBConfig;
import ch.algotrader.dao.marketData.BarDao;
import ch.algotrader.dao.security.FutureDao;
import ch.algotrader.dao.security.OptionDao;
import ch.algotrader.dao.security.SecurityDao;
import ch.algotrader.dao.security.SecurityFamilyDao;
import ch.algotrader.dao.security.StockDao;
import ch.algotrader.dao.trade.OrderDao;
import ch.algotrader.esper.Engine;
import ch.algotrader.ordermgmt.OrderRegistry;
import ch.algotrader.service.ExternalMarketDataService;
import ch.algotrader.service.ExternalOrderService;
import ch.algotrader.service.HistoricalDataService;
import ch.algotrader.service.OrderPersistenceService;
import ch.algotrader.service.ReferenceDataService;
import ch.algotrader.service.ib.IBNativeAccountService;
import ch.algotrader.service.ib.IBNativeAccountServiceImpl;
import ch.algotrader.service.ib.IBNativeHistoricalDataServiceImpl;
import ch.algotrader.service.ib.IBNativeMarketDataServiceImpl;
import ch.algotrader.service.ib.IBNativeOrderServiceImpl;
import ch.algotrader.service.ib.IBNativeReferenceDataServiceImpl;

/**
 * IB native service configuration.
 */
@Configuration
public class IBNativeServiceWiring {

    @Profile("iBNative")
    @Bean(name = {"iBNativeAccountService", "accountService"})
    public IBNativeAccountService createIBNativeAccountService(
            final LinkedBlockingDeque<AccountUpdate> iBAccountUpdateQueue,
            final LinkedBlockingDeque<Set<String>> accountsQueue,
            final LinkedBlockingDeque<ch.algotrader.adapter.ib.Profile> profilesQueue,
            final IBSession iBSession) {

        return new IBNativeAccountServiceImpl(iBAccountUpdateQueue, accountsQueue, profilesQueue, iBSession);
    }

    @Profile("iBNative")
    @Bean(name = "iBNativeOrderService")
    public ExternalOrderService createIBNativeOrderService(
            final IBSession iBSession,
            final AutoIncrementIdGenerator iBOrderIdGenerator,
            final OrderRegistry orderRegistry,
            final IBExecutions ibExecutions,
            final IBOrderMessageFactory iBOrderMessageFactory,
            final OrderPersistenceService orderPersistenceService,
            final OrderDao orderDao,
            final CommonConfig commonConfig) {

        return new IBNativeOrderServiceImpl(iBSession, iBOrderIdGenerator, orderRegistry, ibExecutions,
                iBOrderMessageFactory, orderPersistenceService, orderDao, commonConfig);
    }

    @Profile("iBHistoricalData")
    @Bean(name = {"iBNativeHistoricalDataService", "historicalDataService"})
    public HistoricalDataService createIBNativeHistoricalDataService(
            final IBSession iBSession,
            final IBConfig iBConfig,
            final IBPendingRequests iBPendingRequests,
            final IdGenerator iBRequestIdGenerator,
            final SecurityDao securityDao,
            final BarDao barDao) {

        return new IBNativeHistoricalDataServiceImpl(iBSession, iBConfig, iBPendingRequests, iBRequestIdGenerator,
                securityDao, barDao);
    }

    @Profile("iBMarketData")
    @Bean(name = "iBNativeMarketDataService")
    public ExternalMarketDataService createIBNativeMarketDataService(
            final IBSession iBSession,
            final IBConfig iBConfig,
            final IBSessionStateHolder iBSessionStateHolder,
            final IdGenerator iBRequestIdGenerator,
            final Engine serverEngine) {

        return new IBNativeMarketDataServiceImpl(iBSession, iBConfig, iBSessionStateHolder, iBRequestIdGenerator, serverEngine);
    }

    @Profile("iBReferenceData")
    @Bean(name = { "iBNativeReferenceDataService", "referenceDataService" })
    public ReferenceDataService createIBNativeReferenceDataService(
            final IBSession iBSession,
            final IBConfig iBConfig,
            final IBPendingRequests iBPendingRequests,
            final IdGenerator iBRequestIdGenerator,
            final OptionDao optionDao,
            final FutureDao futureDao,
            final SecurityFamilyDao securityFamilyDao,
            final StockDao stockDao) {

        return new IBNativeReferenceDataServiceImpl(iBSession, iBConfig, iBPendingRequests, iBRequestIdGenerator,
                optionDao, futureDao, securityFamilyDao, stockDao);
    }

}
