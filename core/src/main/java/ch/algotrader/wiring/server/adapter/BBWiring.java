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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import ch.algotrader.adapter.bb.BBAdapter;
import ch.algotrader.adapter.bb.BBSessionStateHolder;
import ch.algotrader.config.BBConfig;
import ch.algotrader.config.ConfigLocator;
import ch.algotrader.event.dispatch.EventDispatcher;

/**
 * Bloomberg adaptor configuration.
 */
@Configuration
public class BBWiring {

    @Profile({"bBMarketData", "bBHistoricalData", "bBReferenceData"})
    @Bean(name = "bBConfig")
    public BBConfig createBBConfig() throws Exception {

        return ConfigLocator.instance().getConfig(BBConfig.class);
    }

    @Profile({"bBMarketData", "bBHistoricalData", "bBReferenceData"})
    @Bean(name = "bBAdapter")
    public BBAdapter createBBAdapter(final BBConfig bBConfig) {
        return new BBAdapter(bBConfig);
    }

    @Profile("bBMarketData")
    @Bean(name = "bBMarketDataSessionStateHolder")
    public BBSessionStateHolder createBBSessionStateHolder(
            @Value("${bb.marketdata.sessionQualifier}")
            final String sessionQualifier,
            final EventDispatcher eventDispatcher) {
        return new BBSessionStateHolder(sessionQualifier, eventDispatcher);
    }

}
