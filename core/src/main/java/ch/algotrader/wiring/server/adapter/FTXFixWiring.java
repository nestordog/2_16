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
import ch.algotrader.adapter.ftx.FTXFixMarketDataMessageHandler;
import ch.algotrader.adapter.ftx.FTXFixOrderMessageHandler;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.esper.Engine;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.service.MarketDataService;
import ch.algotrader.service.OrderService;

/**
 * Fortex Fix configuration.
 */
@Configuration
public class FTXFixWiring {

    @Profile({"fTXMarketData", "fTXFix"})
    @Bean(name = "fTXLogonMessageHandler")
    public DefaultLogonMessageHandler createFTXLogonMessageHandler(final SessionSettings fixSessionSettings) {

        return new DefaultLogonMessageHandler(fixSessionSettings);
    }

    @Profile("fTXFix")
    @Bean(name = "fTXOrderSessionStateHolder")
    public FixSessionStateHolder createFTXOrderSessionStateHolder(final EventDispatcher eventDispatcher) throws Exception {

        return new DefaultFixSessionStateHolder("FTXT", eventDispatcher);
    }

    @Profile("fTXFix")
    @Bean(name = "fTXOrderApplicationFactory")
    public FixApplicationFactory createFTXOrderApplicationFactory(final OrderService orderService,
            final Engine serverEngine,
            final DefaultLogonMessageHandler fTXLogonMessageHandler,
            final FixSessionStateHolder fTXOrderSessionStateHolder) {

        FTXFixOrderMessageHandler cnxFixOrderMessageHandler = new FTXFixOrderMessageHandler(orderService, serverEngine);
        return new DefaultFixApplicationFactory(cnxFixOrderMessageHandler, fTXLogonMessageHandler, fTXOrderSessionStateHolder);
    }

    @Profile("fTXMarketData")
    @Bean(name = "fTXMarketDataSessionStateHolder")
    public MarketDataFixSessionStateHolder createFTXMarketDataSessionStateHolder(
            final EventDispatcher eventDispatcher,
            final MarketDataService marketDataService) {

        return new MarketDataFixSessionStateHolder("FTXMD", eventDispatcher, marketDataService, FeedType.FTX);
    }

    @Profile("fTXMarketData")
    @Bean(name = "fTXMarketDataApplicationFactory")
    public FixApplicationFactory createFTXMarketDataApplicationFactory(
            final Engine serverEngine,
            final DefaultLogonMessageHandler fTXLogonMessageHandler,
            final MarketDataFixSessionStateHolder fTXMarketDataSessionStateHolder) {

        FTXFixMarketDataMessageHandler ftxFixMarketDataMessageHandler = new FTXFixMarketDataMessageHandler(serverEngine);
        return new DefaultFixApplicationFactory(ftxFixMarketDataMessageHandler, fTXLogonMessageHandler, fTXMarketDataSessionStateHolder);
    }

}
