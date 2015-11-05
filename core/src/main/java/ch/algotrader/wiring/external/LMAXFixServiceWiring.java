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
import ch.algotrader.adapter.fix.DropCopyAllocator;
import ch.algotrader.adapter.fix.ManagedFixAdapter;
import ch.algotrader.adapter.lmax.LMAXTickerIdGenerator;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.dao.AccountDao;
import ch.algotrader.dao.security.SecurityDao;
import ch.algotrader.dao.strategy.StrategyDao;
import ch.algotrader.dao.trade.OrderDao;
import ch.algotrader.esper.Engine;
import ch.algotrader.ordermgmt.OrderRegistry;
import ch.algotrader.service.ExternalMarketDataService;
import ch.algotrader.service.ExternalOrderService;
import ch.algotrader.service.OrderPersistenceService;
import ch.algotrader.service.lmax.LMAXDropCopyAllocationServiceImpl;
import ch.algotrader.service.lmax.LMAXFixMarketDataServiceImpl;
import ch.algotrader.service.lmax.LMAXFixOrderServiceImpl;

/**
 * LMAX Fix service configuration.
 */
@Configuration
public class LMAXFixServiceWiring {

    @Profile("lMAXFix")
    @Bean(name = "lMAXFixFixDropCopyAllocator")
    public DropCopyAllocator createLMAXDropCopyAllocator(
            final SecurityDao securityDao,
            final AccountDao accountDao,
            final StrategyDao strategyDao) {

        return new LMAXDropCopyAllocationServiceImpl(securityDao, accountDao, strategyDao);
    }

    @Profile("lMAXFix")
    @Bean(name = "lMAXFixOrderService")
    public ExternalOrderService createLMAXFixOrderService(
            final ManagedFixAdapter fixAdapter,
            final OrderRegistry orderRegistry,
            final OrderPersistenceService orderPersistenceService,
            final OrderDao orderDao,
            final AccountDao accountDao,
            final CommonConfig commonConfig) {

        return new LMAXFixOrderServiceImpl(fixAdapter, orderRegistry, orderPersistenceService, orderDao, accountDao, commonConfig);
    }

    @Profile("lMAXMarketData")
    @Bean(name = "lMAXFixMarketDataService")
    public ExternalMarketDataService createLMAXFixMarketDataService(
            final ExternalSessionStateHolder lMAXMarketDataSessionStateHolder,
            final ManagedFixAdapter fixAdapter,
            final Engine serverEngine) {

        return new LMAXFixMarketDataServiceImpl(lMAXMarketDataSessionStateHolder, fixAdapter, new LMAXTickerIdGenerator(), serverEngine);
    }

}
