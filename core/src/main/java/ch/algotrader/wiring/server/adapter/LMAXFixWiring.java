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
package ch.algotrader.wiring.server.adapter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import quickfix.SessionSettings;
import ch.algotrader.adapter.fix.DefaultFixApplicationFactory;
import ch.algotrader.adapter.fix.DefaultFixSessionStateHolder;
import ch.algotrader.adapter.fix.DefaultLogonMessageHandler;
import ch.algotrader.adapter.fix.FixApplicationFactory;
import ch.algotrader.adapter.fix.FixSessionStateHolder;
import ch.algotrader.adapter.fix.MarketDataFixSessionStateHolder;
import ch.algotrader.adapter.lmax.LMAXFixMarketDataMessageHandler;
import ch.algotrader.adapter.lmax.LMAXFixOrderMessageHandler;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.esper.Engine;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.service.MarketDataService;
import ch.algotrader.service.OrderService;

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
    public FixSessionStateHolder createLMAXOrderSessionStateHolder(final EventDispatcher eventDispatcher) {

        return new DefaultFixSessionStateHolder("LMAXT", eventDispatcher);
    }

    @Profile("lMAXFix")
    @Bean(name = "lMAXOrderApplicationFactory")
    public FixApplicationFactory createLMAXOrderApplicationFactory(final OrderService orderService,
            final Engine serverEngine,
            final DefaultLogonMessageHandler lMAXLogonMessageHandler,
            final FixSessionStateHolder lMAXOrderSessionStateHolder) {

        LMAXFixOrderMessageHandler lMAXFixOrderMessageHandler = new LMAXFixOrderMessageHandler(orderService, serverEngine);
        return new DefaultFixApplicationFactory(lMAXFixOrderMessageHandler, lMAXLogonMessageHandler, lMAXOrderSessionStateHolder);
    }

    @Profile("lMAXMarketData")
    @Bean(name = "lMAXMarketDataSessionStateHolder")
    public MarketDataFixSessionStateHolder createLMAXMarketDataSessionStateHolder(
            final EventDispatcher eventDispatcher,
            final MarketDataService marketDataService) {

        return new MarketDataFixSessionStateHolder("LMAXMD", eventDispatcher, marketDataService, FeedType.LMAX);
    }

    @Profile("lMAXMarketData")
    @Bean(name = "lMAXMarketDataApplicationFactory")
    public FixApplicationFactory createLMAXMarketDataApplicationFactory(
            final Engine serverEngine,
            final DefaultLogonMessageHandler lMAXLogonMessageHandler,
            final MarketDataFixSessionStateHolder lMAXMarketDataSessionStateHolder) {

        LMAXFixMarketDataMessageHandler lMAXFixMarketDataMessageHandler = new LMAXFixMarketDataMessageHandler(serverEngine);

        return new DefaultFixApplicationFactory(lMAXFixMarketDataMessageHandler, lMAXLogonMessageHandler, lMAXMarketDataSessionStateHolder);
    }

}
