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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import ch.algotrader.adapter.fix.DefaultFixEventScheduler;
import ch.algotrader.adapter.fix.FixApplicationFactory;
import ch.algotrader.adapter.fix.FixMultiApplicationSessionFactory;
import ch.algotrader.adapter.fix.ManagedFixAdapter;
import ch.algotrader.entity.trade.OrderDao;
import ch.algotrader.esper.Engine;
import ch.algotrader.ordermgmt.DefaultOrderIdGenerator;
import ch.algotrader.ordermgmt.OrderIdGenerator;
import ch.algotrader.service.LookupService;
import quickfix.CompositeLogFactory;
import quickfix.DefaultMessageFactory;
import quickfix.FileLogFactory;
import quickfix.FileStoreFactory;
import quickfix.LogFactory;
import quickfix.MessageStoreFactory;
import quickfix.SLF4JLogFactory;
import quickfix.SessionFactory;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;

/**
 * Fix core configuration.
 */
@Configuration
@Profile({"jPMFix", "iBFix", "dCFix", "rTFix", "dcMarkteData", "lMAXFix", "lMAXMarketData", "fXCMFix", "fXCMMarketData", "cNXFix", "cNXMarketData"})
public class FixCoreWiring {

    @Bean(name = "orderIdGenerator")
    public OrderIdGenerator createOrderIdGenerator(final OrderDao orderDao) {

        return new DefaultOrderIdGenerator(orderDao);
    }

    @Bean(name = "fixSessionSettings")
    public SessionSettings createFixSessionSettings() throws Exception {

        Resource resource = new ClassPathResource("/fix.cfg");
        try (InputStream inputStream = resource.getInputStream()) {
            return new SessionSettings(inputStream);
        }
    }

    @Bean(name = "fixMessageStoreFactory")
    public MessageStoreFactory createFixMessageStoreFactory(final SessionSettings fixSessionSettings) {

        return new FileStoreFactory(fixSessionSettings);
    }

    @Bean(name = "fixLogFactory")
    public LogFactory createFixLogFactory(final SessionSettings fixSessionSettings) {

        return new CompositeLogFactory(new LogFactory[] { new SLF4JLogFactory(fixSessionSettings), new FileLogFactory(fixSessionSettings) });
    }

    @Bean(name = "fixSocketInitiator", initMethod = "start", destroyMethod = "stop")
    public SocketInitiator createFixSocketInitiator(
            final SessionSettings fixSessionSettings,
            final MessageStoreFactory fixMessageStoreFactory,
            final LogFactory fixLogFactory,
            final ApplicationContext applicationContext) throws Exception {

        Map<String, FixApplicationFactory> applicationFactoryMap = applicationContext.getBeansOfType(FixApplicationFactory.class);
        Map<String, FixApplicationFactory> applicationFactoryMapByName = new HashMap<>(applicationFactoryMap.size());
        for (Map.Entry<String, FixApplicationFactory> entry: applicationFactoryMap.entrySet()) {

            FixApplicationFactory applicationFactory = entry.getValue();
            applicationFactoryMapByName.put(applicationFactory.getName(), applicationFactory);
        }
        SessionFactory sessionFactory = new FixMultiApplicationSessionFactory(
                applicationFactoryMapByName,
                fixMessageStoreFactory,
                fixLogFactory,
                new DefaultMessageFactory());
        return new SocketInitiator(sessionFactory, fixSessionSettings);
    }

    @Bean(name = "fixAdapter")
    public ManagedFixAdapter createFixAdapter(
            final Engine serverEngine,
            final SocketInitiator fixSocketInitiator,
            final LookupService lookupService,
            final OrderIdGenerator orderIdGenerator) {

        DefaultFixEventScheduler defaultFixEventScheduler = new DefaultFixEventScheduler(serverEngine);

        return new ManagedFixAdapter(fixSocketInitiator, lookupService, defaultFixEventScheduler, orderIdGenerator);
    }

}
