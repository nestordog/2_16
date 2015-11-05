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

import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import ch.algotrader.adapter.AutoIncrementIdGenerator;
import ch.algotrader.adapter.ib.AbstractIBMessageHandler;
import ch.algotrader.adapter.ib.AccountUpdate;
import ch.algotrader.adapter.ib.DefaultIBAdapter;
import ch.algotrader.adapter.ib.DefaultIBMessageHandler;
import ch.algotrader.adapter.ib.DefaultIBOrderMessageFactory;
import ch.algotrader.adapter.ib.DefaultIBSessionStateHolder;
import ch.algotrader.adapter.ib.IBAdapter;
import ch.algotrader.adapter.ib.IBExecutions;
import ch.algotrader.adapter.ib.IBOrderMessageFactory;
import ch.algotrader.adapter.ib.IBPendingRequests;
import ch.algotrader.adapter.ib.IBSession;
import ch.algotrader.adapter.ib.IBSessionStateHolder;
import ch.algotrader.config.IBConfig;
import ch.algotrader.esper.Engine;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.service.OrderExecutionService;
import ch.algotrader.service.TransactionService;

/**
 * IB Native configuration.
 */
@Configuration
@Profile(value = {"iBNative", "iBMarketData", "iBReferenceData", "iBHistoricalData"})
public class IBNativeWiring {

    @Bean(name = "iBAccountUpdateQueue")
    public LinkedBlockingDeque<AccountUpdate> createAccountUpdateQueue() {
        return new LinkedBlockingDeque<>();
    }

    @Bean(name = "iBAccountsQueue")
    public LinkedBlockingDeque<Set<String>> createAccountsQueue() {
        return new LinkedBlockingDeque<>();
    }

    @Bean(name = "iBProfilesQueue")
    public LinkedBlockingDeque<ch.algotrader.adapter.ib.Profile> createProfilesQueue() {
        return new LinkedBlockingDeque<>();
    }

    @Bean(name = "iBPendingRequests")
    public IBPendingRequests createIBPendingRequests() {
        return new IBPendingRequests();
    }

    @Bean(name = "iBSessionAdapter")
    public IBAdapter createIBAdapter() {
        return new DefaultIBAdapter();
    }

    @Bean(name = "iBOrderMessageFactory")
    public IBOrderMessageFactory createIBOrderMessageFactory(final IBConfig ibConfig) {

        return new DefaultIBOrderMessageFactory(ibConfig);
    }

    @Bean(name = "iBSessionStateHolder")
    public IBSessionStateHolder createIBSessionStateHolder(
            @Value("${bb.native.sessionQualifier}")
            final String sessionQualifier,
            final EventDispatcher eventDispatcher) {
        return new DefaultIBSessionStateHolder(sessionQualifier, eventDispatcher);
    }

    @Bean(name = "iBRequestIdGenerator")
    public AutoIncrementIdGenerator createIBRequestIdGenerator() {
        return new AutoIncrementIdGenerator();
    }

    @Bean(name = "iBOrderIdGenerator")
    public AutoIncrementIdGenerator createIBOrderIdGenerator() {
        return new AutoIncrementIdGenerator();
    }

    @Bean(name = "iBExecutions")
    public IBExecutions createIBExecutions() {
        return new IBExecutions();
    }

    @Bean(name = "iBMessageHandler")
    public AbstractIBMessageHandler createIBMessageHandler(
            final IBSessionStateHolder iBSessionStateHolder,
            final AutoIncrementIdGenerator iBOrderIdGenerator,
            final IBPendingRequests iBPendingRequests,
            final OrderExecutionService orderExecutionService,
            final TransactionService transactionService,
            final IBExecutions ibExecutions,
            final LinkedBlockingDeque<AccountUpdate> iBAccountUpdateQueue,
            final LinkedBlockingDeque<Set<String>> iBAccountsQueue,
            final LinkedBlockingDeque<ch.algotrader.adapter.ib.Profile> iBProfilesQueue,
            final Engine serverEngine) {
        return new DefaultIBMessageHandler(0, iBSessionStateHolder, iBPendingRequests, iBOrderIdGenerator, orderExecutionService, transactionService,
                ibExecutions, iBAccountUpdateQueue, iBAccountsQueue, iBProfilesQueue, serverEngine);
    }

    @Bean(name = "iBSession")
    public IBSession createIBSession(
            @Value("${ib.defaultSessionId}") final int defaultSessionId,
            @Value("${ib.host}") final String host,
            @Value("${ib.port}") final int port,
            final IBSessionStateHolder iBSessionStateHolder,
            final AbstractIBMessageHandler iBMessageHandler) {
        return new IBSession(defaultSessionId, host, port, iBSessionStateHolder, iBMessageHandler);
    }

    @Bean(name = "iBSessionReconnector", destroyMethod = "destroy")
    public IBSessionReconnector createIBSessionEventListener(
            @Value("${bb.native.sessionQualifier}")
            final String sessionQualifier,
            final IBSession iBSession) {
        return new IBSessionReconnector(sessionQualifier, iBSession);
    }

}
