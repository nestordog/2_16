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
package ch.algotrader.wiring.server.external;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import ch.algotrader.adapter.ExternalSessionStateHolder;
import ch.algotrader.adapter.dc.DCTickerIdGenerator;
import ch.algotrader.adapter.fix.ManagedFixAdapter;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.dao.AccountDao;
import ch.algotrader.dao.trade.OrderDao;
import ch.algotrader.esper.Engine;
import ch.algotrader.ordermgmt.OrderRegistry;
import ch.algotrader.service.ExternalMarketDataService;
import ch.algotrader.service.ExternalOrderService;
import ch.algotrader.service.OrderPersistenceService;
import ch.algotrader.service.dc.DCFixMarketDataServiceImpl;
import ch.algotrader.service.dc.DCFixOrderServiceImpl;

/**
 * Dukas Copy FIX service configuration.
 */
@Configuration
public class DCFixServiceWiring {

    @Profile("dCFix")
    @Bean(name = "dCFixOrderService")
    public ExternalOrderService createDCFixOrderService(
            final ManagedFixAdapter fixAdapter,
            final ExternalSessionStateHolder dCOrderSessionStateHolder,
            final OrderRegistry orderRegistry,
            final OrderPersistenceService orderPersistenceService,
            final OrderDao orderDao,
            final AccountDao accountDao,
            final CommonConfig commonConfig) {

        return new DCFixOrderServiceImpl(fixAdapter, dCOrderSessionStateHolder, orderRegistry, orderPersistenceService, orderDao, accountDao, commonConfig);
    }

    @Profile("dCMarketData")
    @Bean(name = "dCFixMarketDataService")
    public ExternalMarketDataService createDCFixMarketDataService(
            final ExternalSessionStateHolder dCMarketDataSessionStateHolder,
            final ManagedFixAdapter fixAdapter,
            final Engine serverEngine) {

        return new DCFixMarketDataServiceImpl(dCMarketDataSessionStateHolder, fixAdapter, new DCTickerIdGenerator(), serverEngine);
    }

}
