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

import ch.algotrader.adapter.ExternalSessionStateHolder;
import ch.algotrader.adapter.cnx.CNXFixApplicationFactory;
import ch.algotrader.adapter.cnx.CNXFixMarketDataMessageHandler;
import ch.algotrader.adapter.cnx.CNXFixOrderMessageHandler;
import ch.algotrader.adapter.fix.DefaultFixSessionStateHolder;
import ch.algotrader.adapter.fix.DefaultLogonMessageHandler;
import ch.algotrader.adapter.fix.MarketDataFixSessionStateHolder;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.esper.Engine;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.ordermgmt.OrderRegistry;
import quickfix.SessionSettings;

/**
 * Currenex Fix configuration.
 */
@Configuration
public class CNXFixWiring {

    @Profile({"cNXMarketData", "cNXFix"})
    @Bean(name = "cNXLogonMessageHandler")
    public DefaultLogonMessageHandler createCNXLogonMessageHandler(final SessionSettings fixSessionSettings) {

        return new DefaultLogonMessageHandler(fixSessionSettings);
    }

    @Profile("cNXFix")
    @Bean(name = "cNXOrderSessionStateHolder")
    public ExternalSessionStateHolder createCNXOrderSessionStateHolder(final EventDispatcher eventDispatcher) throws Exception {

        return new DefaultFixSessionStateHolder("CNXT", eventDispatcher);
    }

    @Profile("cNXFix")
    @Bean(name = "cNXOrderApplicationFactory")
    public CNXFixApplicationFactory createCNXOrderApplicationFactory(
            final OrderRegistry orderRegistry,
            final Engine serverEngine,
            final DefaultLogonMessageHandler cNXLogonMessageHandler,
            final ExternalSessionStateHolder cNXOrderSessionStateHolder) {

        CNXFixOrderMessageHandler cnxFixOrderMessageHandler = new CNXFixOrderMessageHandler(orderRegistry, serverEngine);
        return new CNXFixApplicationFactory(cnxFixOrderMessageHandler, cNXLogonMessageHandler, cNXOrderSessionStateHolder);
    }

    @Profile("cNXMarketData")
    @Bean(name = "cNXMarketDataSessionStateHolder")
    public ExternalSessionStateHolder createCNXMarketDataSessionStateHolder(final EventDispatcher eventDispatcher) {

        return new MarketDataFixSessionStateHolder("CNXMD", eventDispatcher, FeedType.CNX.name());
    }

    @Profile("cNXMarketData")
    @Bean(name = "cNXMarketDataApplicationFactory")
    public CNXFixApplicationFactory createCNXMarketDataApplicationFactory(
            final Engine serverEngine,
            final DefaultLogonMessageHandler cNXLogonMessageHandler,
            final ExternalSessionStateHolder cNXMarketDataSessionStateHolder) {

        CNXFixMarketDataMessageHandler cnxFixMarketDataMessageHandler = new CNXFixMarketDataMessageHandler(serverEngine);
        return new CNXFixApplicationFactory(cnxFixMarketDataMessageHandler, cNXLogonMessageHandler, cNXMarketDataSessionStateHolder);
    }

}
