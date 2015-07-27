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
package ch.algotrader.wiring.services;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import ch.algotrader.esper.EngineManager;
import ch.algotrader.service.CalendarService;
import ch.algotrader.service.CombinationService;
import ch.algotrader.service.ForexService;
import ch.algotrader.service.FutureService;
import ch.algotrader.service.LazyLoaderService;
import ch.algotrader.service.LocalLookupService;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.MarketDataService;
import ch.algotrader.service.MeasurementService;
import ch.algotrader.service.OptionService;
import ch.algotrader.service.OrderPersistenceService;
import ch.algotrader.service.OrderService;
import ch.algotrader.service.PortfolioService;
import ch.algotrader.service.PositionService;
import ch.algotrader.service.PropertyService;
import ch.algotrader.service.ServerManagementService;
import ch.algotrader.service.SubscriptionService;
import ch.algotrader.service.TransactionPersistenceService;
import ch.algotrader.service.TransactionService;
import ch.algotrader.wiring.HibernateNoCachingWiring;
import ch.algotrader.wiring.DefaultConfigTestBase;
import ch.algotrader.wiring.common.CommonConfigWiring;
import ch.algotrader.wiring.common.EventDispatchWiring;
import ch.algotrader.wiring.server.DaoWiring;
import ch.algotrader.wiring.server.CacheWiring;
import ch.algotrader.wiring.server.CoreConfigWiring;
import ch.algotrader.wiring.server.ServerEngineWiring;
import ch.algotrader.wiring.server.ServiceWiring;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class ServiceWiringTest extends DefaultConfigTestBase {

    @Test
    public void testServicesWiring() {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        EmbeddedDatabaseFactory dbFactory = new EmbeddedDatabaseFactory();
        dbFactory.setDatabaseType(EmbeddedDatabaseType.H2);
        dbFactory.setDatabaseName("testdb;MODE=MYSQL;DATABASE_TO_UPPER=FALSE");

        EmbeddedDatabase database = dbFactory.getDatabase();
        context.getDefaultListableBeanFactory().registerSingleton("dataSource", database);

        TransactionPersistenceService transactionPersistenceService = Mockito.mock(TransactionPersistenceService.class);
        context.getDefaultListableBeanFactory().registerSingleton("transactionPersistenceService", transactionPersistenceService);

        EngineManager engineManager = Mockito.mock(EngineManager.class);
        context.getDefaultListableBeanFactory().registerSingleton("engineManager", engineManager);

        context.register(ServiceWiring.class, CommonConfigWiring.class, CoreConfigWiring.class, HibernateNoCachingWiring.class,
                CacheWiring.class, DaoWiring.class, ServerEngineWiring.class, EventDispatchWiring.class);

        context.refresh();

        Assert.assertNotNull(context.getBean(LookupService.class));
        Assert.assertNotNull(context.getBean(PortfolioService.class));
        Assert.assertNotNull(context.getBean(PositionService.class));
        Assert.assertNotNull(context.getBean(FutureService.class));
        Assert.assertNotNull(context.getBean(ServerManagementService.class));
        Assert.assertNotNull(context.getBean(OptionService.class));
        Assert.assertNotNull(context.getBean(ForexService.class));
        Assert.assertNotNull(context.getBean(TransactionService.class));
        Assert.assertNotNull(context.getBean(MarketDataService.class));
        Assert.assertNotNull(context.getBean(OrderPersistenceService.class));
        Assert.assertNotNull(context.getBean(OrderService.class));
        Assert.assertNotNull(context.getBean(CombinationService.class));
        Assert.assertNotNull(context.getBean(MeasurementService.class));
        Assert.assertNotNull(context.getBean(PropertyService.class));
        Assert.assertNotNull(context.getBean(CalendarService.class));
        Assert.assertNotNull(context.getBean(SubscriptionService.class));
        Assert.assertNotNull(context.getBean(LocalLookupService.class));
        Assert.assertNotNull(context.getBean(LazyLoaderService.class));

        context.close();
    }

}
