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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import ch.algotrader.adapter.ExternalSessionStateHolder;
import ch.algotrader.adapter.fix.DefaultFixTicketIdGenerator;
import ch.algotrader.adapter.fix.DropCopyAllocator;
import ch.algotrader.adapter.fix.ManagedFixAdapter;
import ch.algotrader.adapter.tt.TTPendingRequests;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.dao.AccountDao;
import ch.algotrader.dao.security.FutureDao;
import ch.algotrader.dao.security.OptionDao;
import ch.algotrader.dao.security.SecurityDao;
import ch.algotrader.dao.security.SecurityFamilyDao;
import ch.algotrader.dao.strategy.StrategyDao;
import ch.algotrader.dao.trade.OrderDao;
import ch.algotrader.esper.Engine;
import ch.algotrader.ordermgmt.OrderBook;
import ch.algotrader.service.ExternalMarketDataService;
import ch.algotrader.service.SimpleOrderExecService;
import ch.algotrader.service.OrderPersistenceService;
import ch.algotrader.service.ReferenceDataService;
import ch.algotrader.service.fix.FixStatelessService;
import ch.algotrader.service.tt.TTDropCopyAllocationServiceImpl;
import ch.algotrader.service.tt.TTFixMarketDataServiceImpl;
import ch.algotrader.service.tt.TTFixOrderServiceImpl;
import ch.algotrader.service.tt.TTFixReferenceDataServiceImpl;
import ch.algotrader.wiring.server.adapter.FixStateRequester;

/**
 * Trading Technologies Fix service configuration.
 */
@Configuration
public class TTFixServiceWiring {

    @Profile("tTFix")
    @Bean(name = "tTFixDropCopyAllocator")
    public DropCopyAllocator createTTDropCopyAllocator(
            final SecurityDao securityDao,
            final AccountDao accountDao,
            final StrategyDao strategyDao) {

        return new TTDropCopyAllocationServiceImpl(securityDao, accountDao, strategyDao);
    }

    @Profile("tTFix")
    @Bean(name = "tTFixOrderService")
    public SimpleOrderExecService createTTFixOrderService(
            final ManagedFixAdapter fixAdapter,
            final OrderBook orderBook,
            final OrderPersistenceService orderPersistenceService,
            final OrderDao orderDao,
            final AccountDao accountDao,
            final CommonConfig commonConfig) {

        return new TTFixOrderServiceImpl(fixAdapter, orderBook, orderPersistenceService, orderDao, accountDao, commonConfig);
    }

    @Profile("tTFix")
    @Bean(name = "tTFixStateRequester")
    public FixStateRequester createTTFixStateRequester(
            final FixStatelessService tTFixOrderService,
            final ExternalSessionStateHolder tTOrderSessionStateHolder) {

        return new FixStateRequester(tTFixOrderService, tTOrderSessionStateHolder);
    }

    @Profile("tTMarketData")
    @Bean(name = { "tTReferenceDataService", "referenceDataService" })
    public ReferenceDataService createTTReferenceDataService(
            final ManagedFixAdapter fixAdapter,
            final ExternalSessionStateHolder tTMarketDataSessionStateHolder,
            final TTPendingRequests tTPendingRequests,
            final OptionDao optionDao,
            final FutureDao futureDao,
            final SecurityFamilyDao securityFamilyDao) {

        return new TTFixReferenceDataServiceImpl(fixAdapter, tTMarketDataSessionStateHolder, tTPendingRequests, optionDao, futureDao, securityFamilyDao);
    }

    @Profile("tTMarketData")
    @Bean(name = "tTFixMarketDataService")
    public ExternalMarketDataService createTTFixMarketDataService(
            final ExternalSessionStateHolder tTMarketDataSessionStateHolder,
            final ManagedFixAdapter fixAdapter,
            final Engine serverEngine) {

        return new TTFixMarketDataServiceImpl(tTMarketDataSessionStateHolder, fixAdapter, new DefaultFixTicketIdGenerator(), serverEngine);
    }

}
