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
import ch.algotrader.adapter.fix.DefaultFixSessionStateHolder;
import ch.algotrader.adapter.fix.FixApplicationFactory;
import ch.algotrader.adapter.fxcm.FXCMFixApplicationFactory;
import ch.algotrader.adapter.fxcm.FXCMFixMarketDataMessageHandler;
import ch.algotrader.adapter.fxcm.FXCMFixMessageHandler;
import ch.algotrader.adapter.fxcm.FXCMFixOrderMessageHandler;
import ch.algotrader.esper.Engine;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.service.OrderExecutionService;
import ch.algotrader.service.TransactionService;

/**
 * FXCM Fix configuration.
 */
@Configuration
@Profile({"fXCMMarketData", "fXCMFix"})
public class FXCMFixWiring {

    @Bean(name = "fXCMSessionLifeCycle")
    public ExternalSessionStateHolder createFXCMSessionLifeCycle(
            @Value("${fix.fxcm.sessionQualifier}")
            final String sessionQualifier,
            final EventDispatcher eventDispatcher) {

        return new DefaultFixSessionStateHolder(sessionQualifier, eventDispatcher);
    }

    @Bean(name = "fXCMApplicationFactory")
    public FixApplicationFactory createFXCMApplicationFactory(
            final Engine serverEngine,
            final OrderExecutionService orderExecutionService,
            final TransactionService transactionService,
            final ExternalSessionStateHolder fXCMSessionLifeCycle) {

        FXCMFixMarketDataMessageHandler fxcmFixMarketDataMessageHandler = new FXCMFixMarketDataMessageHandler(serverEngine);
        FXCMFixOrderMessageHandler fxcmFixOrderMessageHandler = new FXCMFixOrderMessageHandler(orderExecutionService, transactionService, serverEngine);
        FXCMFixMessageHandler fxcmFixMessageHandler = new FXCMFixMessageHandler(fxcmFixMarketDataMessageHandler, fxcmFixOrderMessageHandler);
        return new FXCMFixApplicationFactory(fxcmFixMessageHandler, fXCMSessionLifeCycle);
    }

}
