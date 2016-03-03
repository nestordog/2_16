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
import ch.algotrader.adapter.fix.DefaultLogonMessageHandler;
import ch.algotrader.adapter.fix.DropCopyAllocator;
import ch.algotrader.adapter.fix.FixApplicationFactory;
import ch.algotrader.adapter.lmax.LMAXFixMarketDataMessageHandler;
import ch.algotrader.adapter.lmax.LMAXFixOrderMessageHandler;
import ch.algotrader.esper.Engine;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.service.OrderExecutionService;
import quickfix.SessionSettings;

/**
 * LMAX FIX configuration.
 */
@Configuration
public class LMAXFixWiring {

    @Profile({"lMAXMarketData", "lMAXFix"})
    @Bean(name = "lMAXLogonMessageHandler")
    public DefaultLogonMessageHandler createLMAXLogonMessageHandler(final SessionSettings fixSessionSettings) {

        return new DefaultLogonMessageHandler(fixSessionSettings);
    }

    @Profile("lMAXFix")
    @Bean(name = "lMAXOrderSessionStateHolder")
    public ExternalSessionStateHolder createLMAXOrderSessionStateHolder(
            @Value("${fix.lmax.trading.sessionQualifier}")
            final String sessionQualifier,
            final EventDispatcher eventDispatcher) {

        return new DefaultFixSessionStateHolder(sessionQualifier, eventDispatcher);
    }

    @Profile("lMAXFix")
    @Bean(name = "lMAXOrderApplicationFactory")
    public FixApplicationFactory createLMAXOrderApplicationFactory(
            final OrderExecutionService orderExecutionService,
            final DefaultLogonMessageHandler lMAXLogonMessageHandler,
            final ExternalSessionStateHolder lMAXOrderSessionStateHolder,
            final DropCopyAllocator lMAXDropCopyAllocator) {

        LMAXFixOrderMessageHandler lMAXFixOrderMessageHandler = new LMAXFixOrderMessageHandler(orderExecutionService, lMAXDropCopyAllocator);
        return new DefaultFixApplicationFactory(lMAXFixOrderMessageHandler, lMAXLogonMessageHandler, lMAXOrderSessionStateHolder);
    }

    @Profile("lMAXMarketData")
    @Bean(name = "lMAXMarketDataSessionStateHolder")
    public ExternalSessionStateHolder createLMAXMarketDataSessionStateHolder(
            @Value("${fix.lmax.marketdata.sessionQualifier}")
            final String sessionQualifier,
            final EventDispatcher eventDispatcher) {

        return new DefaultFixSessionStateHolder(sessionQualifier, eventDispatcher);
    }

    @Profile("lMAXMarketData")
    @Bean(name = "lMAXMarketDataApplicationFactory")
    public FixApplicationFactory createLMAXMarketDataApplicationFactory(
            final Engine serverEngine,
            final DefaultLogonMessageHandler lMAXLogonMessageHandler,
            final ExternalSessionStateHolder lMAXMarketDataSessionStateHolder) {

        LMAXFixMarketDataMessageHandler lMAXFixMarketDataMessageHandler = new LMAXFixMarketDataMessageHandler(serverEngine);

        return new DefaultFixApplicationFactory(lMAXFixMarketDataMessageHandler, lMAXLogonMessageHandler, lMAXMarketDataSessionStateHolder);
    }

}
