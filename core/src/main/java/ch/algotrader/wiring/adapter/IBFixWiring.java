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

import java.util.concurrent.LinkedBlockingDeque;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import ch.algotrader.adapter.ExternalSessionStateHolder;
import ch.algotrader.adapter.fix.DefaultFixApplicationFactory;
import ch.algotrader.adapter.fix.DefaultFixSessionStateHolder;
import ch.algotrader.adapter.fix.FixApplicationFactory;
import ch.algotrader.adapter.ib.IBCustomMessage;
import ch.algotrader.adapter.ib.IBFixOrderMessageHandler;
import ch.algotrader.esper.Engine;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.service.OrderExecutionService;
import ch.algotrader.service.TransactionService;

/**
 * IB Fix configuration.
 */
@Configuration
@Profile("iBFix")
public class IBFixWiring {

    @Bean(name = "iBOrderSessionStateHolder")
    public ExternalSessionStateHolder createIBOrderSessionStateHolder(
            @Value("${fix.ib.trading.sessionQualifier}")
            final String sessionQualifier,
            final EventDispatcher eventDispatcher) {

        return new DefaultFixSessionStateHolder(sessionQualifier, eventDispatcher);
    }

    @Bean(name = "iBAllocationMessageQueue")
    public LinkedBlockingDeque<IBCustomMessage> createIBAllocationMessageQueue() {

        return new LinkedBlockingDeque<>();
    }

    @Bean(name = "iBOrderApplicationFactory")
    public FixApplicationFactory createIBOrderApplicationFactory(
            final OrderExecutionService orderExecutionService,
            final TransactionService transactionService,
            final LinkedBlockingDeque<IBCustomMessage> iBAllocationMessageQueue,
            final Engine serverEngine,
            final ExternalSessionStateHolder iBOrderSessionStateHolder) {

        IBFixOrderMessageHandler ibFixOrderMessageHandler = new IBFixOrderMessageHandler(orderExecutionService, transactionService, iBAllocationMessageQueue, serverEngine);

        return new DefaultFixApplicationFactory(ibFixOrderMessageHandler, iBOrderSessionStateHolder);
    }

}
