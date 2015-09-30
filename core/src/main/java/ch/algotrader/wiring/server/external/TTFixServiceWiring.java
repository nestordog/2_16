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

import ch.algotrader.adapter.ExternalSessionStateHolder;
import ch.algotrader.adapter.fix.DefaultFixTicketIdGenerator;
import ch.algotrader.adapter.fix.ManagedFixAdapter;
import ch.algotrader.adapter.tt.TTPendingRequests;
import ch.algotrader.dao.security.FutureDao;
import ch.algotrader.dao.security.OptionDao;
import ch.algotrader.dao.security.SecurityFamilyDao;
import ch.algotrader.esper.Engine;
import ch.algotrader.service.ExternalMarketDataService;
import ch.algotrader.service.ReferenceDataService;
import ch.algotrader.service.tt.TTFixMarketDataServiceImpl;
import ch.algotrader.service.tt.TTFixReferenceDataServiceImpl;

/**
 * Trading Technologies Fix service configuration.
 */
@Configuration
public class TTFixServiceWiring {

    @Profile("tTReferenceData")
    @Bean(name = { "tTReferenceDataService", "referenceDataService" })
    public ReferenceDataService createTTReferenceDataService(
            final ManagedFixAdapter fixAdapter,
            final ExternalSessionStateHolder tTSecurityDefinitionSessionStateHolder,
            final TTPendingRequests tTPendingRequests,
            final OptionDao optionDao,
            final FutureDao futureDao,
            final SecurityFamilyDao securityFamilyDao) {

        return new TTFixReferenceDataServiceImpl(fixAdapter, tTSecurityDefinitionSessionStateHolder, tTPendingRequests, optionDao, futureDao, securityFamilyDao);
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
