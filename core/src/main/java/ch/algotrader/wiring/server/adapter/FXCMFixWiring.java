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
import ch.algotrader.adapter.fix.FixApplicationFactory;
import ch.algotrader.adapter.fix.MarketDataFixSessionStateHolder;
import ch.algotrader.adapter.fxcm.FXCMFixApplicationFactory;
import ch.algotrader.adapter.fxcm.FXCMFixMarketDataMessageHandler;
import ch.algotrader.adapter.fxcm.FXCMFixMessageHandler;
import ch.algotrader.adapter.fxcm.FXCMFixOrderMessageHandler;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.esper.Engine;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.ordermgmt.OrderRegistry;

/**
 * FXCM Fix configuration.
 */
@Configuration
@Profile({"fXCMMarketData", "fXCMFix"})
public class FXCMFixWiring {

    @Bean(name = "fXCMSessionLifeCycle")
    public ExternalSessionStateHolder createFXCMSessionLifeCycle(final EventDispatcher eventDispatcher) {

        return new MarketDataFixSessionStateHolder("FXCM", eventDispatcher, FeedType.FXCM.name());
    }

    @Bean(name = "fXCMApplicationFactory")
    public FixApplicationFactory createFXCMApplicationFactory(
            final Engine serverEngine,
            final OrderRegistry orderRegistry,
            final ExternalSessionStateHolder fXCMSessionLifeCycle) {

        FXCMFixMarketDataMessageHandler fxcmFixMarketDataMessageHandler = new FXCMFixMarketDataMessageHandler(serverEngine);
        FXCMFixOrderMessageHandler fxcmFixOrderMessageHandler = new FXCMFixOrderMessageHandler(orderRegistry, serverEngine);
        FXCMFixMessageHandler fxcmFixMessageHandler = new FXCMFixMessageHandler(fxcmFixMarketDataMessageHandler, fxcmFixOrderMessageHandler);
        return new FXCMFixApplicationFactory(fxcmFixMessageHandler, fXCMSessionLifeCycle);
    }

}
