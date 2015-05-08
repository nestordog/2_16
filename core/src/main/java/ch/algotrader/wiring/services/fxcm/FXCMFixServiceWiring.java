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
package ch.algotrader.wiring.services.fxcm;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import ch.algotrader.adapter.fix.ManagedFixAdapter;
import ch.algotrader.adapter.fix.MarketDataFixSessionStateHolder;
import ch.algotrader.entity.security.SecurityDao;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.service.OrderService;
import ch.algotrader.service.fxcm.FXCMFixMarketDataService;
import ch.algotrader.service.fxcm.FXCMFixMarketDataServiceImpl;
import ch.algotrader.service.fxcm.FXCMFixOrderService;
import ch.algotrader.service.fxcm.FXCMFixOrderServiceImpl;

/**
 * FXCM Fix service configuration.
 */
@Configuration
public class FXCMFixServiceWiring {

    @Profile("fXCMFix")
    @Bean(name = "fXCMFixOrderService")
    public FXCMFixOrderService createFXCMFixOrderService(
            final ManagedFixAdapter fixAdapter,
            final OrderService orderService) {

        return new FXCMFixOrderServiceImpl(fixAdapter, orderService);
    }

    @Profile("fXCMMarketData")
    @Bean(name = "fXCMFixMarketDataService")
    public FXCMFixMarketDataService createFXCMFixMarketDataService(
            final MarketDataFixSessionStateHolder fXCMSessionLifeCycle,
            final ManagedFixAdapter fixAdapter,
            final EngineManager engineManager,
            final SecurityDao securityDao) {

        return new FXCMFixMarketDataServiceImpl(fXCMSessionLifeCycle, fixAdapter, engineManager, securityDao);
    }

}
