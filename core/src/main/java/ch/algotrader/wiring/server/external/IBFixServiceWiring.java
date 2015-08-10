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

import java.util.concurrent.BlockingQueue;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import ch.algotrader.adapter.fix.ManagedFixAdapter;
import ch.algotrader.adapter.ib.IBCustomMessage;
import ch.algotrader.config.IBConfig;
import ch.algotrader.ordermgmt.OpenOrderRegistry;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.ib.IBFixAllocationService;
import ch.algotrader.service.ib.IBFixAllocationServiceImpl;
import ch.algotrader.service.ib.IBFixOrderService;
import ch.algotrader.service.ib.IBFixOrderServiceImpl;

/**
 * IB FIX service configuration.
 */
@Configuration
@Profile("iBFix")
public class IBFixServiceWiring {

    @Bean(name = "iBFixOrderService")
    public IBFixOrderService createIBFixOrderService(
            final IBConfig iBConfig,
            final ManagedFixAdapter fixAdapter,
            final OpenOrderRegistry openOrderRegistry) {

        return new IBFixOrderServiceImpl(iBConfig, fixAdapter, openOrderRegistry);
    }

    @Bean(name = "iBFixAllocationService")
    public IBFixAllocationService createIBFixAllocationService(
            final ManagedFixAdapter fixAdapter,
            final BlockingQueue<IBCustomMessage> iBAllocationMessageQueue,
            final LookupService lookupService) {

        return new IBFixAllocationServiceImpl(fixAdapter, iBAllocationMessageQueue, lookupService);
    }

}
