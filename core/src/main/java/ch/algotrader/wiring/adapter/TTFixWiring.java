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
package ch.algotrader.wiring.adapter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import ch.algotrader.adapter.ExternalSessionStateHolder;
import ch.algotrader.adapter.fix.DefaultFixApplicationFactory;
import ch.algotrader.adapter.fix.DefaultFixSessionStateHolder;
import ch.algotrader.adapter.fix.DropCopyAllocator;
import ch.algotrader.adapter.fix.FixApplicationFactory;
import ch.algotrader.adapter.tt.TTFixMarketAndReferenceDataMessageHandler;
import ch.algotrader.adapter.tt.TTFixOrderMessageHandler;
import ch.algotrader.adapter.tt.TTLogonMessageHandler;
import ch.algotrader.adapter.tt.TTPendingRequests;
import ch.algotrader.esper.Engine;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.OrderExecutionService;
import ch.algotrader.service.TransactionService;
import quickfix.SessionSettings;

/**
 * Trading Technologies FIX configuration.
 */
@Configuration
public class TTFixWiring {

    @Profile({"tTFix", "tTMarketData"})
    @Bean(name = "tTLogonMessageHandler")
    public TTLogonMessageHandler createTTLogonMessageHandler(final SessionSettings fixSessionSettings) {

        return new TTLogonMessageHandler(fixSessionSettings);
    }

    @Profile("tTFix")
    @Bean(name = "tTOrderSessionStateHolder")
    public ExternalSessionStateHolder createTTOrderSessionStateHolder(
            @Value("${fix.tt.trading.sessionQualifier}")
            final String sessionQualifier,
            final EventDispatcher eventDispatcher) {

        return new DefaultFixSessionStateHolder(sessionQualifier, eventDispatcher);
    }

    @Profile("tTFix")
    @Bean(name = "tTOrderApplicationFactory")
    public FixApplicationFactory createTTOrderApplicationFactory(
            final OrderExecutionService orderExecutionService,
            final TransactionService transactionService,
            final LookupService lookupService,
            final Engine serverEngine,
            final TTLogonMessageHandler tTLogonMessageHandler,
            final ExternalSessionStateHolder tTOrderSessionStateHolder,
            final DropCopyAllocator tTFixDropCopyAllocator) {

        TTFixOrderMessageHandler tTFixOrderMessageHandler = new TTFixOrderMessageHandler(orderExecutionService, transactionService,
                lookupService, serverEngine, tTFixDropCopyAllocator);
        return new DefaultFixApplicationFactory(tTFixOrderMessageHandler, tTLogonMessageHandler, tTOrderSessionStateHolder);
    }

    @Profile("tTMarketData")
    @Bean(name = "tTPendingRequests")
    public TTPendingRequests createTTPendingRequests() {

        return new TTPendingRequests();
    }

    @Profile("tTMarketData")
    @Bean(name = "tTMarketDataSessionStateHolder")
    public ExternalSessionStateHolder createTTMarketDataSessionStateHolder(
            @Value("${fix.tt.marketdata.sessionQualifier}")
            final String sessionQualifier,
            final EventDispatcher eventDispatcher) {

        return new DefaultFixSessionStateHolder(sessionQualifier, eventDispatcher);
    }

    @Profile("tTMarketData")
    @Bean(name = "tTMarketDataApplicationFactory")
    public FixApplicationFactory createTTMarketDataApplicationFactory(
            final Engine serverEngine,
            final TTPendingRequests tTPendingRequests,
            final TTLogonMessageHandler tTLogonMessageHandler,
            final ExternalSessionStateHolder tTMarketDataSessionStateHolder) {

        TTFixMarketAndReferenceDataMessageHandler tTFixMarketDataMessageHandler = new TTFixMarketAndReferenceDataMessageHandler(serverEngine, tTPendingRequests);

        return new DefaultFixApplicationFactory(tTFixMarketDataMessageHandler, tTLogonMessageHandler, tTMarketDataSessionStateHolder);
    }

}
