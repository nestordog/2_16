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
package ch.algotrader.wiring.server.adapter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import ch.algotrader.adapter.ExternalSessionStateHolder;
import ch.algotrader.adapter.fix.DefaultFixApplicationFactory;
import ch.algotrader.adapter.fix.DefaultFixSessionStateHolder;
import ch.algotrader.adapter.fix.FixApplicationFactory;
import ch.algotrader.adapter.fix.fix42.GenericFix42OrderMessageHandler;
import ch.algotrader.esper.Engine;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.ordermgmt.OrderRegistry;

/**
 * JPM Fix configuration.
 */
@Configuration
@Profile("jPMFix")
public class JPMFixWiring {

    @Bean(name = "jPMOrderSessionStateHolder")
    public ExternalSessionStateHolder createJPMOrderSessionStateHolder(
            @Value("${fix.jpm.trading.sessionQualifier}")
            final String sessionQualifier,
            final EventDispatcher eventDispatcher) {

        return new DefaultFixSessionStateHolder(sessionQualifier, eventDispatcher);
    }

    @Bean(name = "jPMOrderApplicationFactory")
    public FixApplicationFactory createJPMOrderApplicationFactory(
            final OrderRegistry orderRegistry,
            final Engine serverEngine,
            final ExternalSessionStateHolder jPMOrderSessionStateHolder) {

        GenericFix42OrderMessageHandler genericFix42OrderMessageHandler = new GenericFix42OrderMessageHandler(orderRegistry, serverEngine);
        return new DefaultFixApplicationFactory(genericFix42OrderMessageHandler, jPMOrderSessionStateHolder);
    }

}
