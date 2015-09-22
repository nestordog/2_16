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
package ch.algotrader.cache;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.io.Charsets;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.collection.internal.AbstractPersistentCollection;
import org.hibernate.proxy.HibernateProxy;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.support.TransactionTemplate;

import ch.algotrader.dao.NamedParam;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.AccountImpl;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.PositionImpl;
import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.SubscriptionImpl;
import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.exchange.ExchangeImpl;
import ch.algotrader.entity.property.Property;
import ch.algotrader.entity.property.PropertyImpl;
import ch.algotrader.entity.security.Combination;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.Index;
import ch.algotrader.entity.security.IndexImpl;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.security.SecurityImpl;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.enumeration.AssetClass;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.CombinationType;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.enumeration.QueryType;
import ch.algotrader.enumeration.TransactionType;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.service.CombinationService;
import ch.algotrader.service.ExternalMarketDataService;
import ch.algotrader.service.MarketDataService;
import ch.algotrader.service.PositionService;
import ch.algotrader.service.PropertyService;
import ch.algotrader.service.TransactionPersistenceService;
import ch.algotrader.service.TransactionService;
import ch.algotrader.wiring.DefaultConfigTestBase;
import ch.algotrader.wiring.common.CommonConfigWiring;
import ch.algotrader.wiring.common.EventDispatchPostInitWiring;
import ch.algotrader.wiring.common.EventDispatchWiring;
import ch.algotrader.wiring.server.CacheWiring;
import ch.algotrader.wiring.server.CoreConfigWiring;
import ch.algotrader.wiring.server.DaoWiring;
import ch.algotrader.wiring.server.HibernateWiring;
import ch.algotrader.wiring.server.ServiceWiring;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CacheTest extends DefaultConfigTestBase {

    private static final String ACCOUNT_NAME = "TEST_ACCOUNT";
    private static final String PROPERTY_NAME = "TEST_PROPERTY";
    private static final String STRATEGY_NAME = "TEST_STRATEGY";

    private static CacheManager cache;
    private static AnnotationConfigApplicationContext context;
    private static TransactionTemplate txTemplate;
    private static EmbeddedDatabase database;

    private static long securityFamilyId2; // NON_TRADEABLE
    private static long strategyId1;
    private static long securityId1; // EUR.USD
    private static long securityId2; // USD.CHF
    private static long securityId3; // NON_TRADEABLE

    @BeforeClass
    public static void beforeClass() {

        net.sf.ehcache.CacheManager ehCacheManager = net.sf.ehcache.CacheManager.getInstance();
        ehCacheManager.shutdown();

        context = new AnnotationConfigApplicationContext();

        // register in-memory db
        EmbeddedDatabaseFactory dbFactory = new EmbeddedDatabaseFactory();
        dbFactory.setDatabaseType(EmbeddedDatabaseType.H2);
        dbFactory.setDatabaseName("testdb;MODE=MYSQL;DATABASE_TO_UPPER=FALSE");

        database = dbFactory.getDatabase();
        context.getDefaultListableBeanFactory().registerSingleton("dataSource", database);

        // register mocked services
        TransactionPersistenceService transactionPersistenceService = Mockito.mock(TransactionPersistenceService.class);
        context.getDefaultListableBeanFactory().registerSingleton("transactionPersistenceService", transactionPersistenceService);

        EngineManager engineManager = Mockito.mock(EngineManager.class);
        context.getDefaultListableBeanFactory().registerSingleton("engineManager", engineManager);

        Engine engine = Mockito.mock(Engine.class);
        context.getDefaultListableBeanFactory().registerSingleton("serverEngine", engine);

        ExternalMarketDataService externalMarketDataService = Mockito.mock(ExternalMarketDataService.class);
        context.getDefaultListableBeanFactory().registerSingleton("externalMarketDataService", externalMarketDataService);

        Mockito.when(externalMarketDataService.getFeedType()).thenReturn(FeedType.IB.name());

        // register Wirings
        context.register(CommonConfigWiring.class, CoreConfigWiring.class, EventDispatchWiring.class, EventDispatchPostInitWiring.class,
                HibernateWiring.class, CacheWiring.class, DaoWiring.class, ServiceWiring.class);

        context.refresh();

        cache = context.getBean(CacheManager.class);
        txTemplate = context.getBean(TransactionTemplate.class);

        // create the database
        ResourceDatabasePopulator dbPopulator = new ResourceDatabasePopulator();
        dbPopulator.addScript(new ClassPathResource("/db/h2/h2.sql"));
        DatabasePopulatorUtils.execute(dbPopulator, database);

        // populate the database
        SessionFactory sessionFactory = context.getBean(SessionFactory.class);
        Session session = sessionFactory.openSession();

        Exchange exchange1 = new ExchangeImpl();
        exchange1.setName("IDEALPRO");
        exchange1.setCode("IDEALPRO");
        exchange1.setTimeZone("US/Eastern");
        session.save(exchange1);

        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("FX");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
        family1.setExchange(exchange1);
        session.save(family1);

        SecurityFamily family2 = new SecurityFamilyImpl();
        family2.setName("NON_TRADEABLE");
        family2.setTickSizePattern("0<0.1");
        family2.setCurrency(Currency.USD);
        family2.setTradeable(false);
        securityFamilyId2 = (Long) session.save(family2);

        Forex security1 = new ForexImpl();
        security1.setSymbol("EUR.USD");
        security1.setBaseCurrency(Currency.EUR);
        security1.setSecurityFamily(family1);
        securityId1 = (Long) session.save(security1);

        Forex security2 = new ForexImpl();
        security2.setSymbol("GBP.USD");
        security2.setBaseCurrency(Currency.GBP);
        security2.setSecurityFamily(family1);
        security2.setUnderlying(security1);
        securityId2 = (Long) session.save(security2);

        Index security3 = new IndexImpl();
        security3.setSymbol("NON-TRADEABLE");
        security3.setSecurityFamily(family2);
        security3.setUnderlying(security1);
        security3.setAssetClass(AssetClass.EQUITY);
        securityId3 = (Long) session.save(security3);

        Strategy strategy1 = new StrategyImpl();
        strategy1.setName(STRATEGY_NAME);
        strategyId1 = (Long) session.save(strategy1);

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setStrategy(strategy1);
        subscription1.setFeedType(FeedType.IB.name());
        subscription1.setSecurity(security2);
        session.save(subscription1);
        security2.addSubscriptions(subscription1);

        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setStrategy(strategy1);
        position1.setSecurity(security2);
        session.save(position1);
        security2.addPositions(position1);

        Account account1 = new AccountImpl();
        account1.setName(ACCOUNT_NAME);
        account1.setBroker(Broker.IB.name());
        account1.setOrderServiceType(OrderServiceType.IB_NATIVE.name());
        session.save(account1);

        Property property1 = new PropertyImpl();
        property1.setName(PROPERTY_NAME);
        property1.setDoubleValue(10.0);
        property1.setPropertyHolder(strategy1);
        session.save(property1);
        strategy1.getProps().put(PROPERTY_NAME, property1);

        session.flush();
        session.close();
    }

    @AfterClass
    public static void afterClass() {

        // cleanup the database
        ResourceDatabasePopulator dbPopulator = new ResourceDatabasePopulator();
        dbPopulator.addScript(new ByteArrayResource("DROP ALL OBJECTS".getBytes(Charsets.US_ASCII)));

        DatabasePopulatorUtils.execute(dbPopulator, database);

        context.close();

        net.sf.ehcache.CacheManager ehCacheManager = net.sf.ehcache.CacheManager.getInstance();
        ehCacheManager.shutdown();
    }

    @Test
    public void testSecurity() {

        Security security = cache.get(SecurityImpl.class, securityId2);
        Assert.assertNotNull(security);

        Assert.assertNotNull(security.getSecurityFamily());
        Assert.assertFalse(security.getSecurityFamily() instanceof HibernateProxy);

        Assert.assertNotNull(security.getUnderlying());
        Assert.assertFalse(security.getUnderlying() instanceof HibernateProxy);

        AbstractPersistentCollection subscriptions = (AbstractPersistentCollection) security.getSubscriptions();
        Assert.assertTrue(subscriptions.wasInitialized());

        AbstractPersistentCollection positions = (AbstractPersistentCollection) security.getPositions();
        Assert.assertTrue(positions.wasInitialized());
    }

    @Test
    public void testPosition() {

        TransactionService transactionService = context.getBean(TransactionService.class);

        txTemplate.execute(txStatus -> {

            transactionService.createTransaction(securityId2, STRATEGY_NAME, null, new Date(), 10000, new BigDecimal(1.0), null, null, null, Currency.USD, TransactionType.BUY, ACCOUNT_NAME, null);
            return null;
        });
        
        String queryString = "select p from PositionImpl as p join p.strategy as s where p.security.id = :securityId and s.name = :strategyName";

        Position position1 = cache.findUnique(Position.class, queryString, QueryType.HQL, new NamedParam("strategyName", STRATEGY_NAME), new NamedParam("securityId", securityId2));
            
        Position position2 = cache.get(PositionImpl.class, position1.getId());
        
        Assert.assertNotNull(position2);
        Assert.assertEquals(position1, position2);
        Assert.assertSame(position1, position2);
    }

    @Test
    public void testSubscription() {

        MarketDataService marketDataService = context.getBean(MarketDataService.class);

        Security security = cache.get(SecurityImpl.class, securityId1);
        Assert.assertEquals(0, security.getSubscriptions().size());

        txTemplate.execute(txStatus -> {
            marketDataService.subscribe(STRATEGY_NAME, securityId1);
            return null;
        });

        Assert.assertEquals(1, security.getSubscriptions().size());

        txTemplate.execute(txStatus -> {
            marketDataService.unsubscribe(STRATEGY_NAME, securityId1);
            return null;
        });

        Assert.assertEquals(0, security.getSubscriptions().size());
    }

    @Test
    public void testHQL() {

        String queryString = "from PositionImpl as p join fetch p.security as s where s.id = :id";
        NamedParam namedParam = new NamedParam("id", securityId2);

        Position position2 = cache.find(Position.class, queryString, QueryType.HQL, namedParam).iterator().next();
        Assert.assertNotNull(position2);

        Position position3 = cache.find(Position.class, queryString, QueryType.HQL, namedParam).iterator().next();
        Assert.assertNotNull(position2);
        Assert.assertEquals(position2, position3);
        Assert.assertSame(position2, position3);

    }

    @Test
    public void testNonTradeablePosition() {

        PositionService positionService = context.getBean(PositionService.class);

        Security security = cache.get(SecurityImpl.class, securityId3);

        Assert.assertEquals(0, security.getPositions().size());

        long positionId = txTemplate.execute(txStatus -> {
            return positionService.createNonTradeablePosition(STRATEGY_NAME, securityId3, 1000000).getId();
        });

        Assert.assertEquals(1, security.getPositions().size());

        txTemplate.execute(txStatus -> {
            return positionService.modifyNonTradeablePosition(positionId, 2000000);
        });

        Assert.assertEquals(2000000, security.getPositions().iterator().next().getQuantity());

        txTemplate.execute(txStatus -> {
            positionService.deleteNonTradeablePosition(positionId, false);
            return null;
        });

        Assert.assertEquals(0, security.getPositions().size());
    }

    @Test
    public void testCombination() {

        CombinationService combinationService = context.getBean(CombinationService.class);

        // combination / component modification
        long combinationId = txTemplate.execute(txStatus -> {
            return combinationService.createCombination(CombinationType.BUTTERFLY, securityFamilyId2).getId();
        });

        Combination combination1 = (Combination) cache.get(SecurityImpl.class, combinationId);
        Assert.assertNotNull(combination1);

        Combination combination2 = txTemplate.execute(txStatus -> {
            return combinationService.addComponentQuantity(combinationId, securityId1, 1000);
        });

        Assert.assertEquals(1, combination1.getComponentCount());
        Assert.assertEquals(1000, combination1.getComponents().iterator().next().getQuantity());
        Assert.assertEquals(1, combination2.getComponentCount());
        Assert.assertEquals(1000, combination2.getComponents().iterator().next().getQuantity());

        Combination combination3 = txTemplate.execute(txStatus -> {
            return combinationService.addComponentQuantity(combinationId, securityId2, 5000);
        });

        Assert.assertEquals(2, combination1.getComponentCount());
        Assert.assertEquals(6000, combination1.getComponentTotalQuantity());
        Assert.assertEquals(2, combination3.getComponentCount());
        Assert.assertEquals(6000, combination3.getComponentTotalQuantity());

        Combination combination4 = txTemplate.execute(txStatus -> {
            return combinationService.removeComponent(combinationId, securityId1);
        });

        Assert.assertEquals(1, combination1.getComponentCount());
        Assert.assertEquals(1, combination4.getComponentCount());

        txTemplate.execute(txStatus -> {
            combinationService.deleteCombination(combinationId);
            return null;
        });

        Combination combination5 = (Combination) cache.get(SecurityImpl.class, combinationId);
        Assert.assertNull(combination5);
    }

    @Test
    public void testProperty() {

        PropertyService propertyService = context.getBean(PropertyService.class);

        // property
        Strategy strategy = cache.get(StrategyImpl.class, strategyId1);
        Assert.assertEquals(1, strategy.getProps().size());

        txTemplate.execute(txStatus -> {
            return propertyService.addProperty(strategyId1, "test", 12, false);
        });

        //        Assert.assertEquals(2, strategy.getProps().size());
        //        Assert.assertEquals(12, strategy.getIntProperty("test"));

        txTemplate.execute(txStatus -> {
            return propertyService.removeProperty(strategyId1, "test");
        });

        //        Assert.assertEquals(1, strategy.getProps().size());
    }
}
