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
package ch.algotrader.wiring.server.external;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import ch.algotrader.adapter.fix.ManagedFixAdapter;
import ch.algotrader.adapter.fix.MarketDataFixSessionStateHolder;
import ch.algotrader.esper.Engine;
import ch.algotrader.service.OrderService;
import ch.algotrader.service.cnx.CNXFixMarketDataService;
import ch.algotrader.service.cnx.CNXFixMarketDataServiceImpl;
import ch.algotrader.service.cnx.CNXFixOrderService;
import ch.algotrader.service.cnx.CNXFixOrderServiceImpl;

/**
 * Currenex Fix service configuration.
 */
@Configuration
public class CNXFixServiceWiring {

    @Profile("cNXFix")
    @Bean(name = "cNXFixOrderService")
    public CNXFixOrderService createCNXFixOrderService(
            final ManagedFixAdapter fixAdapter,
            final OrderService orderService) {

        return new CNXFixOrderServiceImpl(fixAdapter, orderService);
    }

    @Profile("cNXMarketData")
    @Bean(name = "cNXFixMarketDataService")
    public CNXFixMarketDataService createCNXFixMarketDataService(
            final MarketDataFixSessionStateHolder cNXMarketDataSessionStateHolder,
            final ManagedFixAdapter fixAdapter,
            final Engine serverEngine) {

        return new CNXFixMarketDataServiceImpl(cNXMarketDataSessionStateHolder, fixAdapter, serverEngine);
    }

}
