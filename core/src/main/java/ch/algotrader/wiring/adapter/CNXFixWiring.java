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
import ch.algotrader.adapter.cnx.CNXFixApplicationFactory;
import ch.algotrader.adapter.cnx.CNXFixMarketDataMessageHandler;
import ch.algotrader.adapter.cnx.CNXFixOrderMessageHandler;
import ch.algotrader.adapter.fix.DefaultFixSessionStateHolder;
import ch.algotrader.adapter.fix.DefaultLogonMessageHandler;
import ch.algotrader.esper.Engine;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.service.OrderExecutionService;
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
    public ExternalSessionStateHolder createCNXOrderSessionStateHolder(
            @Value("${fix.cnx.trading.sessionQualifier}")
            final String sessionQualifier,
            final EventDispatcher eventDispatcher) throws Exception {

        return new DefaultFixSessionStateHolder(sessionQualifier, eventDispatcher);
    }

    @Profile("cNXFix")
    @Bean(name = "cNXOrderApplicationFactory")
    public CNXFixApplicationFactory createCNXOrderApplicationFactory(
            final OrderExecutionService orderExecutionService,
            final DefaultLogonMessageHandler cNXLogonMessageHandler,
            final ExternalSessionStateHolder cNXOrderSessionStateHolder) {

        CNXFixOrderMessageHandler cnxFixOrderMessageHandler = new CNXFixOrderMessageHandler(orderExecutionService);
        return new CNXFixApplicationFactory(cnxFixOrderMessageHandler, cNXLogonMessageHandler, cNXOrderSessionStateHolder);
    }

    @Profile("cNXMarketData")
    @Bean(name = "cNXMarketDataSessionStateHolder")
    public ExternalSessionStateHolder createCNXMarketDataSessionStateHolder(
            @Value("${fix.cnx.marketdata.sessionQualifier}")
            final String sessionQualifier,
            final EventDispatcher eventDispatcher) {

        return new DefaultFixSessionStateHolder(sessionQualifier, eventDispatcher);
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
