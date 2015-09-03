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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import ch.algotrader.adapter.fix.DefaultFixApplicationFactory;
import ch.algotrader.adapter.fix.DefaultFixSessionStateHolder;
import ch.algotrader.adapter.fix.FixApplicationFactory;
import ch.algotrader.adapter.ExternalSessionStateHolder;
import ch.algotrader.adapter.ib.IBCustomMessage;
import ch.algotrader.adapter.ib.IBFixOrderMessageHandler;
import ch.algotrader.esper.Engine;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.ordermgmt.OpenOrderRegistry;

/**
 * IB Fix configuration.
 */
@Configuration
@Profile("iBFix")
public class IBFixWiring {

    @Bean(name = "iBOrderSessionStateHolder")
    public ExternalSessionStateHolder createIBOrderSessionStateHolder(final EventDispatcher eventDispatcher) {

        return new DefaultFixSessionStateHolder("IBFT", eventDispatcher);
    }

    @Bean(name = "iBAllocationMessageQueue")
    public BlockingQueue<IBCustomMessage> createIBAllocationMessageQueue() {

        return new LinkedBlockingDeque<>();
    }

    @Bean(name = "iBOrderApplicationFactory")
    public FixApplicationFactory createIBOrderApplicationFactory(
            final OpenOrderRegistry openOrderRegistry,
            final BlockingQueue<IBCustomMessage> iBAllocationMessageQueue,
            final Engine serverEngine,
            final ExternalSessionStateHolder iBOrderSessionStateHolder) {

        IBFixOrderMessageHandler ibFixOrderMessageHandler = new IBFixOrderMessageHandler(openOrderRegistry, iBAllocationMessageQueue, serverEngine);

        return new DefaultFixApplicationFactory(ibFixOrderMessageHandler, iBOrderSessionStateHolder);
    }

}
