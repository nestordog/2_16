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
package ch.algotrader.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.io.Charsets;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.hibernate4.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import ch.algotrader.cache.CacheManager;
import ch.algotrader.dao.GenericDao;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.AccountImpl;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.PositionImpl;
import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.SubscriptionImpl;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.TransactionImpl;
import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.exchange.ExchangeImpl;
import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.entity.marketData.BarImpl;
import ch.algotrader.entity.security.Combination;
import ch.algotrader.entity.security.CombinationImpl;
import ch.algotrader.entity.security.Component;
import ch.algotrader.entity.security.ComponentImpl;
import ch.algotrader.entity.security.EasyToBorrow;
import ch.algotrader.entity.security.EasyToBorrowImpl;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.FutureFamily;
import ch.algotrader.entity.security.FutureFamilyImpl;
import ch.algotrader.entity.security.FutureImpl;
import ch.algotrader.entity.security.Index;
import ch.algotrader.entity.security.IndexImpl;
import ch.algotrader.entity.security.IntrestRate;
import ch.algotrader.entity.security.IntrestRateImpl;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.OptionFamily;
import ch.algotrader.entity.security.OptionFamilyImpl;
import ch.algotrader.entity.security.OptionImpl;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.security.SecurityReference;
import ch.algotrader.entity.security.SecurityReferenceImpl;
import ch.algotrader.entity.security.Stock;
import ch.algotrader.entity.security.StockImpl;
import ch.algotrader.entity.strategy.CashBalance;
import ch.algotrader.entity.strategy.CashBalanceImpl;
import ch.algotrader.entity.strategy.Measurement;
import ch.algotrader.entity.strategy.MeasurementImpl;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.enumeration.AssetClass;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.CombinationType;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.ExpirationType;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.enumeration.OptionType;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.enumeration.TransactionType;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.hibernate.InMemoryDBTest;
import ch.algotrader.util.collection.Pair;
import ch.algotrader.wiring.common.CommonConfigWiring;
import ch.algotrader.wiring.common.EventDispatchPostInitWiring;
import ch.algotrader.wiring.common.EventDispatchWiring;
import ch.algotrader.wiring.server.CacheWiring;
import ch.algotrader.wiring.server.HibernateWiring;

/**
* Unit tests for {@link ch.algotrader.entity.Transaction}.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*
* @version $Revision$ $Date$
*/
public class LookupServiceTest extends InMemoryDBTest {

    private static LookupService lookupService;
    private static AnnotationConfigApplicationContext context;
    private static DataSource dataSource;
    private static SessionFactory sessionFactory;
    private static CacheManager cacheManager;
    private static TransactionTemplate txTemplate;

    protected Session session;

    @Rule public ExpectedException exception = ExpectedException.none();

    public LookupServiceTest() throws IOException {

        super();
    }

    @BeforeClass
    public static void beforeClass() {

        context = new AnnotationConfigApplicationContext();
        context.getEnvironment().setActiveProfiles("embeddedDataSource", "simulation");

        EngineManager engineManager = Mockito.mock(EngineManager.class);
        context.getDefaultListableBeanFactory().registerSingleton("engineManager", engineManager);

        EmbeddedDatabaseFactory dbFactory = new EmbeddedDatabaseFactory();
        dbFactory.setDatabaseType(EmbeddedDatabaseType.H2);
        dbFactory.setDatabaseName("testdb;MODE=MYSQL;DATABASE_TO_UPPER=FALSE");

        dataSource = dbFactory.getDatabase();
        context.getDefaultListableBeanFactory().registerSingleton("dataSource", dataSource);

        context.register(CommonConfigWiring.class, EventDispatchWiring.class, EventDispatchPostInitWiring.class, HibernateWiring.class, CacheWiring.class);

        context.refresh();

        sessionFactory = context.getBean(SessionFactory.class);
        txTemplate = context.getBean(TransactionTemplate.class);

        GenericDao genericDao = context.getBean(GenericDao.class);
        cacheManager = context.getBean(CacheManager.class);
        lookupService = new LookupServiceImpl(genericDao, cacheManager);
    }

    @AfterClass
    public static void afterClass() {

        context.close();

        net.sf.ehcache.CacheManager ehCacheManager = net.sf.ehcache.CacheManager.getInstance();
        ehCacheManager.shutdown();
    }

    @Override
    @Before
    public void setup() throws Exception {

        ResourceDatabasePopulator dbPopulator = new ResourceDatabasePopulator();
        dbPopulator.addScript(new ClassPathResource("/db/h2/h2.sql"));
        DatabasePopulatorUtils.execute(dbPopulator, dataSource);

        this.session = sessionFactory.openSession();

        TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(this.session));
    }

    @Override
    @After
    public void cleanup() throws Exception {

        ResourceDatabasePopulator dbPopulator = new ResourceDatabasePopulator();
        dbPopulator.addScript(new ByteArrayResource("DROP ALL OBJECTS".getBytes(Charsets.US_ASCII)));

        DatabasePopulatorUtils.execute(dbPopulator, dataSource);

        TransactionSynchronizationManager.unbindResource(sessionFactory);

        if (this.session != null) {

            if (this.session.isOpen()) {
                this.session.close();
            }
        }

        cacheManager.clear();
    }

    @Test
    public void testGetSecurity() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.USD);
        forex1.setSecurityFamily(family1);
    
        this.session.save(family1);
        this.session.save(forex1);
        this.session.flush();
    
        Security forex2 = lookupService.getSecurity(0);
    
        Assert.assertNull(forex2);
    
        Security forex3 = lookupService.getSecurity(forex1.getId());
    
        Assert.assertNotNull(forex3);
    
        Assert.assertSame(family1, forex3.getSecurityFamily());
        Assert.assertSame(forex1, forex3);
    }

    @Test
    public void testGetSecurityByIsin() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.EUR);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("GBP.EUR");
        forex1.setBaseCurrency(Currency.AUD);
        forex1.setSecurityFamily(family1);
        forex1.setIsin("US0378331005");
    
        this.session.save(family1);
        this.session.save(forex1);
        this.session.flush();
    
        Security forex2 = lookupService.getSecurityByIsin("NOT_FOUND");
    
        Assert.assertNull(forex2);
    
        Security forex3 = lookupService.getSecurityByIsin("US0378331005");
    
        Assert.assertNotNull(forex3);
    
        Assert.assertSame(family1, forex3.getSecurityFamily());
        Assert.assertSame(forex1, forex3);
    }

    @Test
    public void testGetSecurityBySymbol() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.EUR);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("GBP.EUR");
        forex1.setBaseCurrency(Currency.AUD);
        forex1.setSecurityFamily(family1);
    
        this.session.save(family1);
        this.session.save(forex1);
        this.session.flush();
    
        Security forex2 = lookupService.getSecurityBySymbol("USD.EUR");
    
        Assert.assertNull(forex2);
    
        Security forex3 = lookupService.getSecurityBySymbol("GBP.EUR");
    
        Assert.assertNotNull(forex3);
    
        Assert.assertSame(family1, forex3.getSecurityFamily());
        Assert.assertSame(forex1, forex3);
    }

    @Test
    public void testGetSecurityByBbgid() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.EUR);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("GBP.EUR");
        forex1.setBaseCurrency(Currency.AUD);
        forex1.setSecurityFamily(family1);
        forex1.setIsin("US0378331005");
        forex1.setBbgid("BBG005Y3Z8B6");
    
        this.session.save(family1);
        this.session.save(forex1);
        this.session.flush();
    
        Security forex2 = lookupService.getSecurityByBbgid("NOT_FOUND");
    
        Assert.assertNull(forex2);
    
        Security forex3 = lookupService.getSecurityByBbgid("BBG005Y3Z8B6");
    
        Assert.assertNotNull(forex3);
    
        Assert.assertSame(family1, forex3.getSecurityFamily());
        Assert.assertSame(forex1, forex3);
    }

    @Test
    public void tesFindSecurityByRic() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.EUR);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("GBP.EUR");
        forex1.setBaseCurrency(Currency.AUD);
        forex1.setSecurityFamily(family1);
        forex1.setIsin("US0378331005");
        forex1.setBbgid("BBG005Y3Z8B6");
        forex1.setRic("RIC");
    
        this.session.save(family1);
        this.session.save(forex1);
        this.session.flush();
    
        Security forex2 = lookupService.getSecurityByRic("NOT_FOUND");
    
        Assert.assertNull(forex2);
    
        Security forex3 = lookupService.getSecurityByRic("RIC");
    
        Assert.assertNotNull(forex3);
    
        Assert.assertSame(family1, forex3.getSecurityFamily());
        Assert.assertSame(forex1, forex3);
    }

    @Test
    public void testGetSecurityByConid() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.EUR);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("GBP.EUR");
        forex1.setBaseCurrency(Currency.AUD);
        forex1.setSecurityFamily(family1);
        forex1.setIsin("US0378331005");
        forex1.setBbgid("BBG005Y3Z8B6");
        forex1.setRic("RIC");
        forex1.setConid("CONID");
    
        this.session.save(family1);
        this.session.save(forex1);
        this.session.flush();
    
        Security forex2 = lookupService.getSecurityByConid("NOT_FOUND");
    
        Assert.assertNull(forex2);
    
        Security forex3 = lookupService.getSecurityByConid("CONID");
    
        Assert.assertNotNull(forex3);
    
        Assert.assertSame(family1, forex3.getSecurityFamily());
        Assert.assertSame(forex1, forex3);
    }

    @Test
    public void testGetSecurityByIdInclFamilyAndUnderlying() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.EUR);
    
        SecurityFamily family2 = new SecurityFamilyImpl();
        family2.setName("family2");
        family2.setTickSizePattern("0<0.1");
        family2.setCurrency(Currency.AUD);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("GBP.EUR");
        forex1.setBaseCurrency(Currency.AUD);
        forex1.setSecurityFamily(family1);
    
        Forex forex2 = new ForexImpl();
        forex2.setSymbol("GBP.EUR");
        forex2.setBaseCurrency(Currency.AUD);
        forex2.setSecurityFamily(family2);
        forex2.setIsin("US0378331005");
        forex2.setBbgid("BBG005Y3Z8B6");
        forex2.setRic("RIC");
        forex2.setConid("CONID");
        forex2.setUnderlying(forex1);
    
        this.session.save(family1);
        this.session.save(family2);
        this.session.save(forex1);
        this.session.save(forex2);
        this.session.flush();
        this.session.clear();
    
        Security forex3 = lookupService.getSecurityInclFamilyAndUnderlying(0);
    
        Assert.assertNull(forex3);
    
        Security forex4 = lookupService.getSecurityInclFamilyAndUnderlying(forex2.getId());
    
        Assert.assertNotNull(forex4);
    
        this.session.close();
    
        Assert.assertEquals(forex1, forex4.getUnderlying());
        Assert.assertEquals(family2, forex4.getSecurityFamily());
        Assert.assertEquals(forex2, forex4);
    }

    @Test
    public void testGetSecuritiesByIds() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("USD.USD");
        forex1.setBaseCurrency(Currency.USD);
        forex1.setSecurityFamily(family1);
    
        Forex forex2 = new ForexImpl();
        forex2.setSymbol("AUD.USD");
        forex2.setBaseCurrency(Currency.AUD);
        forex2.setSecurityFamily(family1);
    
        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(forex2);
        this.session.flush();
    
        List<Long> ids = new ArrayList<>();
    
        ids.add(forex1.getId());
    
        List<Security> forexes1 = lookupService.getSecuritiesByIds(ids);
    
        Assert.assertEquals(1, forexes1.size());
    
        Assert.assertSame(forex1.getSecurityFamily(), forexes1.get(0).getSecurityFamily());
        Assert.assertSame(forex1, forexes1.get(0));
    
        ids.add(forex2.getId());
    
        List<Security> forexes2 = lookupService.getSecuritiesByIds(ids);
    
        Assert.assertEquals(2, forexes2.size());
    
        Assert.assertSame(family1, forexes2.get(0).getSecurityFamily());
        Assert.assertSame(forex1, forexes2.get(0));
        Assert.assertSame(family1, forexes2.get(1).getSecurityFamily());
        Assert.assertSame(forex2, forexes2.get(1));
    }

    @Test
    public void testGetSecurityReferenceTargetByOwnerAndName() {

        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);

        Forex forex1 = new ForexImpl();
        forex1.setSymbol("USD.USD");
        forex1.setBaseCurrency(Currency.USD);
        forex1.setSecurityFamily(family1);

        Index index1 = new IndexImpl();
        index1.setSymbol("USD.USD Index");
        index1.setAssetClass(AssetClass.FX);
        index1.setSecurityFamily(family1);

        SecurityReference securityReference1 = new SecurityReferenceImpl();
        securityReference1.setName("INDEX");
        securityReference1.setTarget(index1);
        securityReference1.setOwner(forex1);

        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(index1);
        this.session.save(securityReference1);
        this.session.flush();

        Security reference1 = lookupService.getSecurityReferenceTargetByOwnerAndName(forex1.getId(), "DUMMY");

        Assert.assertNull(reference1);

        Security reference2 = lookupService.getSecurityReferenceTargetByOwnerAndName(forex1.getId(), "INDEX");

        Assert.assertNotNull(reference2);
        Assert.assertSame(index1, reference2);

        Assert.assertNotNull(reference2.getSecurityFamily());
    }

    @Test
    public void testGetSubscribedSecuritiesForAutoActivateStrategies() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.EUR);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("INR.EUR");
        forex1.setBaseCurrency(Currency.CAD);
        forex1.setSecurityFamily(family1);
    
        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
        strategy1.setAutoActivate(Boolean.FALSE);
    
        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM.name());
        subscription1.setSecurity(forex1);
        subscription1.setStrategy(strategy1);
    
        SecurityFamily family2 = new SecurityFamilyImpl();
        family2.setName("family2");
        family2.setTickSizePattern("0<0.1");
        family2.setCurrency(Currency.AUD);
    
        Forex forex2 = new ForexImpl();
        forex2.setSymbol("INR.USD");
        forex2.setBaseCurrency(Currency.INR);
        forex2.setSecurityFamily(family2);
    
        Strategy strategy2 = new StrategyImpl();
        strategy2.setName("Strategy2");
        strategy2.setAutoActivate(Boolean.FALSE);
    
        Subscription subscription2 = new SubscriptionImpl();
        subscription2.setFeedType(FeedType.SIM.name());
        subscription2.setSecurity(forex2);
        subscription2.setStrategy(strategy2);
    
        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(strategy1);
        this.session.save(subscription1);
        this.session.save(family2);
        this.session.save(forex2);
        this.session.save(strategy2);
        this.session.save(subscription2);
    
        forex1.addSubscriptions(subscription1);
        forex2.addSubscriptions(subscription2);
    
        this.session.flush();
    
        List<Security> forexes1 = new ArrayList<Security>(lookupService.getSubscribedSecuritiesForAutoActivateStrategies());
    
        Assert.assertEquals(0, forexes1.size());
    
        strategy1.setAutoActivate(Boolean.TRUE);
        this.session.flush();
    
        List<Security> forexes2 = new ArrayList<Security>(lookupService.getSubscribedSecuritiesForAutoActivateStrategies());
    
        Assert.assertEquals(1, forexes2.size());
    
        Assert.assertSame(forex1, forexes2.get(0));
    
        strategy2.setAutoActivate(Boolean.TRUE);
        this.session.flush();
    
        List<Security> forexes3 = new ArrayList<Security>(lookupService.getSubscribedSecuritiesForAutoActivateStrategies());
    
        Assert.assertEquals(2, forexes3.size());
    
        Assert.assertSame(forex1, forexes3.get(0));
        Assert.assertSame(forex2, forexes3.get(1));
    }

    @Test
    public void testGetSubscribedSecuritiesAndFeedTypeForAutoActivateStrategies() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.EUR);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("INR.EUR");
        forex1.setBaseCurrency(Currency.CAD);
        forex1.setSecurityFamily(family1);
    
        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
        strategy1.setAutoActivate(Boolean.FALSE);
    
        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM.name());
        subscription1.setSecurity(forex1);
        subscription1.setStrategy(strategy1);
    
        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(strategy1);
        this.session.save(subscription1);
    
        forex1.addSubscriptions(subscription1);
    
        this.session.flush();
    
        List<Pair<Security, String>> subscribedSecurityList1 = lookupService.getSubscribedSecuritiesAndFeedTypeForAutoActivateStrategiesInclComponents();
    
        Assert.assertEquals(0, subscribedSecurityList1.size());
    
        strategy1.setAutoActivate(Boolean.TRUE);
        this.session.flush();
    
        List<Pair<Security, String>> subscribedSecurityList2 = lookupService.getSubscribedSecuritiesAndFeedTypeForAutoActivateStrategiesInclComponents();
    
        Assert.assertEquals(1, subscribedSecurityList2.size());

        Pair<Security, String> securityStringPair1 = subscribedSecurityList2.get(0);
        Assert.assertSame(forex1, securityStringPair1.getFirst());
        Assert.assertSame(FeedType.SIM.name(), securityStringPair1.getSecond());
    }

    @Test
    public void testGetStocksByIndustry() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        SecurityFamily family2 = new SecurityFamilyImpl();
        family2.setName("Family2");
        family2.setTickSizePattern("0<0.1");
        family2.setCurrency(Currency.GBP);
    
        Stock stock1 = new StockImpl();
        stock1.setSecurityFamily(family1);
        stock1.setGics("12345678");
    
        Stock stock2 = new StockImpl();
        stock2.setSecurityFamily(family2);
        stock2.setGics("1234");
    
        this.session.save(family1);
        this.session.save(stock1);
        this.session.save(family2);
        this.session.save(stock2);
        this.session.flush();
    
        List<Stock> stocks1 = (List<Stock>) lookupService.getStocksByIndustry("11");
    
        Assert.assertEquals(0, stocks1.size());
    
        List<Stock> stocks2 = (List<Stock>) lookupService.getStocksByIndustry("123456");
    
        Assert.assertEquals(1, stocks2.size());
    
        Assert.assertSame(stock1, stocks2.get(0));
    
        stock2.setGics("12345678");
    
        this.session.flush();
    
        List<Stock> stocks3 = (List<Stock>) lookupService.getStocksByIndustry("123456");
    
        Assert.assertEquals(2, stocks3.size());
    
        Assert.assertSame(stock1, stocks3.get(0));
        Assert.assertSame(stock2, stocks3.get(1));
    }

    @Test
    public void testGetStocksByIndustryGroup() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        SecurityFamily family2 = new SecurityFamilyImpl();
        family2.setName("Family2");
        family2.setTickSizePattern("0<0.1");
        family2.setCurrency(Currency.GBP);
    
        Stock stock1 = new StockImpl();
        stock1.setSecurityFamily(family1);
        stock1.setGics("12345678");
    
        Stock stock2 = new StockImpl();
        stock2.setSecurityFamily(family2);
        stock2.setGics("1234");
    
        this.session.save(family1);
        this.session.save(stock1);
        this.session.save(family2);
        this.session.save(stock2);
        this.session.flush();
    
        List<Stock> stocks1 = (List<Stock>) lookupService.getStocksByIndustryGroup("12");
    
        Assert.assertEquals(0, stocks1.size());
    
        List<Stock> stocks2 = (List<Stock>) lookupService.getStocksByIndustryGroup("1234");
    
        Assert.assertEquals(1, stocks2.size());
    
        Assert.assertSame(stock1, stocks2.get(0));
    
        stock2.setGics("12345678");
    
        this.session.flush();
    
        List<Stock> stocks3 = (List<Stock>) lookupService.getStocksByIndustryGroup("1234");
    
        Assert.assertEquals(2, stocks3.size());
    
        Assert.assertSame(stock1, stocks3.get(0));
        Assert.assertSame(stock2, stocks3.get(1));
    }

    @Test
    public void testGetStocksBySectory() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        SecurityFamily family2 = new SecurityFamilyImpl();
        family2.setName("Family2");
        family2.setTickSizePattern("0<0.1");
        family2.setCurrency(Currency.GBP);
    
        Stock stock1 = new StockImpl();
        stock1.setSecurityFamily(family1);
        stock1.setGics("12345678");
    
        Stock stock2 = new StockImpl();
        stock2.setSecurityFamily(family2);
        stock2.setGics("1234");
    
        this.session.save(family1);
        this.session.save(stock1);
        this.session.save(family2);
        this.session.save(stock2);
        this.session.flush();
    
        List<Stock> stocks1 = (List<Stock>) lookupService.getStocksBySector("123");
    
        Assert.assertEquals(0, stocks1.size());
    
        List<Stock> stocks2 = (List<Stock>) lookupService.getStocksBySector("12");
    
        Assert.assertEquals(1, stocks2.size());
    
        Assert.assertSame(stock1, stocks2.get(0));
    
        stock2.setGics("12345678");
    
        this.session.flush();
    
        List<Stock> stocks3 = (List<Stock>) lookupService.getStocksBySector("12");
    
        Assert.assertEquals(2, stocks3.size());
    
        Assert.assertSame(stock1, stocks3.get(0));
        Assert.assertSame(stock2, stocks3.get(1));
    }

    @Test
    public void testGetStocksBySubIndustry() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        SecurityFamily family2 = new SecurityFamilyImpl();
        family2.setName("Family2");
        family2.setTickSizePattern("0<0.1");
        family2.setCurrency(Currency.GBP);
    
        Stock stock1 = new StockImpl();
        stock1.setSecurityFamily(family1);
        stock1.setGics("12345678");
    
        Stock stock2 = new StockImpl();
        stock2.setSecurityFamily(family2);
        stock2.setGics("1234");
    
        this.session.save(family1);
        this.session.save(stock1);
        this.session.save(family2);
        this.session.save(stock2);
        this.session.flush();
    
        List<Stock> stocks1 = (List<Stock>) lookupService.getStocksBySubIndustry("11");
    
        Assert.assertEquals(0, stocks1.size());
    
        List<Stock> stocks2 = (List<Stock>) lookupService.getStocksBySubIndustry("12345678");
    
        Assert.assertEquals(1, stocks2.size());
        Assert.assertSame(stock1, stocks2.get(0));
    
        stock2.setGics("12345678");
    
        this.session.flush();
    
        List<Stock> stocks3 = (List<Stock>) lookupService.getStocksBySubIndustry("12345678");
    
        Assert.assertEquals(2, stocks3.size());
    
        Assert.assertSame(stock1, stocks3.get(0));
        Assert.assertSame(stock2, stocks3.get(1));
    }

    @Test
    public void testGetSubscribedOptions() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);
    
        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
    
        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.DAY_OF_MONTH, 1);
    
        Option option1 = new OptionImpl();
        option1.setSecurityFamily(family1);
        option1.setExpiration(cal1.getTime());
        option1.setStrike(new BigDecimal(111));
        option1.setType(OptionType.CALL);
        option1.setUnderlying(forex1);
    
        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM.name());
        subscription1.setSecurity(option1);
        subscription1.setStrategy(strategy1);
    
        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(strategy1);
        this.session.save(option1);
        this.session.save(subscription1);
    
        option1.addSubscriptions(subscription1);
    
        this.session.flush();
    
        List<Option> options1 = lookupService.getSubscribedOptions();
    
        Assert.assertEquals(1, options1.size());
    
        Assert.assertEquals(1, options1.get(0).getSubscriptions().size());
        Assert.assertSame(option1, options1.get(0));
    }

    @Test
    public void testGetSubscribedFutures() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
    
        Calendar cal1 = Calendar.getInstance();
    
        Future future1 = new FutureImpl();
        future1.setSecurityFamily(family1);
        future1.setExpiration(cal1.getTime());
    
        Calendar cal2 = Calendar.getInstance();
    
        Future future2 = new FutureImpl();
        future2.setSecurityFamily(family1);
        future2.setExpiration(cal2.getTime());
    
        this.session.save(family1);
        this.session.save(future1);
        this.session.save(future2);
        this.session.flush();
    
        List<Future> futures1 = lookupService.getSubscribedFutures();
    
        Assert.assertEquals(0, futures1.size());
    
        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM.name());
        subscription1.setSecurity(future1);
        subscription1.setStrategy(strategy1);
    
        this.session.save(strategy1);
        this.session.save(subscription1);
    
        future1.addSubscriptions(subscription1);
    
        this.session.flush();
    
        List<Future> futures2 = lookupService.getSubscribedFutures();
    
        Assert.assertEquals(1, futures2.size());
    
        Assert.assertSame(future1, futures2.get(0));
        Assert.assertSame(family1, futures2.get(0).getSecurityFamily());
        Assert.assertSame(1, futures2.get(0).getSubscriptions().size());
    
        Subscription subscription2 = new SubscriptionImpl();
        subscription2.setFeedType(FeedType.BB.name());
        subscription2.setSecurity(future2);
        subscription2.setStrategy(strategy1);
    
        this.session.save(subscription2);
    
        future2.addSubscriptions(subscription2);
    
        this.session.flush();
    
        List<Future> futures3 = lookupService.getSubscribedFutures();
    
        Assert.assertEquals(2, futures3.size());
    
        Assert.assertSame(future1, futures3.get(0));
        Assert.assertSame(family1, futures3.get(0).getSecurityFamily());
        Assert.assertSame(1, futures3.get(0).getSubscriptions().size());
        Assert.assertSame(future2, futures3.get(1));
        Assert.assertSame(family1, futures3.get(1).getSecurityFamily());
        Assert.assertSame(1, futures3.get(1).getSubscriptions().size());
    }

    @Test
    public void testGetSubscribedCombinationsByStrategy() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);
    
        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
    
        Component component1 = new ComponentImpl();
        component1.setSecurity(forex1);
        component1.setQuantity(12L);
    
        Combination combination1 = new CombinationImpl();
        combination1.setSecurityFamily(family1);
        combination1.setUuid(UUID.randomUUID().toString());
        combination1.setType(CombinationType.STRADDLE);
        combination1.addComponents(component1);
    
        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM.name());
        subscription1.setSecurity(combination1);
        subscription1.setStrategy(strategy1);
    
        this.session.save(strategy1);
        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(combination1);
        this.session.save(subscription1);
    
        combination1.addSubscriptions(subscription1);
    
        this.session.flush();
    
        List<Combination> combinations1 = (List<Combination>) lookupService.getSubscribedCombinationsByStrategy("Strategy1");
    
        Assert.assertEquals(1, combinations1.size());
    
        Assert.assertEquals(1, combinations1.get(0).getSubscriptions().size());
        Assert.assertSame(combination1, combinations1.get(0));
    }

    @Test
    public void testGetSubscribedCombinationsByStrategyAndUnderlying() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);
    
        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
    
        Component component1 = new ComponentImpl();
        component1.setSecurity(forex1);
        component1.setQuantity(12L);
    
        Combination combination1 = new CombinationImpl();
        combination1.setSecurityFamily(family1);
        combination1.setUuid(UUID.randomUUID().toString());
        combination1.setType(CombinationType.STRADDLE);
        combination1.addComponents(component1);
        combination1.setUnderlying(forex1);
    
        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM.name());
        subscription1.setSecurity(combination1);
        subscription1.setStrategy(strategy1);
    
        this.session.save(strategy1);
        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(combination1);
        this.session.save(subscription1);
    
        combination1.addSubscriptions(subscription1);
    
        this.session.flush();
    
        List<Combination> combinations1 = (List<Combination>) lookupService.getSubscribedCombinationsByStrategyAndUnderlying("Strategy1", 0);
    
        Assert.assertEquals(0, combinations1.size());
    
        List<Combination> combinations2 = (List<Combination>) lookupService.getSubscribedCombinationsByStrategyAndUnderlying("Strategy1", forex1.getId());
    
        Assert.assertEquals(1, combinations2.size());
    
        Assert.assertEquals(1, combinations2.get(0).getSubscriptions().size());
        Assert.assertSame(combination1, combinations2.get(0));
    }

    @Test
    public void testGetSubscribedCombinationsByStrategyAndComponent() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);
    
        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
    
        Component component1 = new ComponentImpl();
        component1.setSecurity(forex1);
        component1.setQuantity(12L);
    
        Combination combination1 = new CombinationImpl();
        combination1.setSecurityFamily(family1);
        combination1.setUuid(UUID.randomUUID().toString());
        combination1.setType(CombinationType.STRADDLE);
    
        combination1.addComponents(component1);
    
        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM.name());
        subscription1.setSecurity(combination1);
        subscription1.setStrategy(strategy1);
    
        this.session.save(strategy1);
        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(combination1);
        this.session.save(subscription1);
    
        component1.setCombination(combination1);
    
        this.session.save(component1);
    
        combination1.addSubscriptions(subscription1);
    
        this.session.flush();
    
        List<Combination> combinations1 = (List<Combination>) lookupService.getSubscribedCombinationsByStrategyAndComponent("Strategy1", 0);
    
        Assert.assertEquals(0, combinations1.size());
    
        List<Combination> combinations2 = (List<Combination>) lookupService.getSubscribedCombinationsByStrategyAndComponent("Strategy1", forex1.getId());
    
        Assert.assertEquals(1, combinations2.size());
    
        Assert.assertEquals(1, combinations2.get(0).getSubscriptions().size());
        Assert.assertSame(combination1, combinations2.get(0));
    }

    @Test
    public void testGetSubscribedCombinationsByStrategyAndComponentType() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);
    
        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
    
        Component component1 = new ComponentImpl();
        component1.setSecurity(forex1);
        component1.setQuantity(12L);
    
        Combination combination1 = new CombinationImpl();
        combination1.setSecurityFamily(family1);
        combination1.setUuid(UUID.randomUUID().toString());
        combination1.setType(CombinationType.STRADDLE);

        combination1.addComponents(component1);
    
        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM.name());
        subscription1.setSecurity(combination1);
        subscription1.setStrategy(strategy1);
    
        this.session.save(strategy1);
        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(combination1);
        this.session.save(subscription1);
    
        component1.setCombination(combination1);

        this.session.save(component1);

        combination1.addSubscriptions(subscription1);
    
        this.session.flush();
    
        List<Combination> combinations1 = (List<Combination>) lookupService.getSubscribedCombinationsByStrategyAndComponentClass("Strategy1", Security.class);
    
        Assert.assertEquals(0, combinations1.size());
    
        List<Combination> combinations2 = (List<Combination>) lookupService.getSubscribedCombinationsByStrategyAndComponentClass("Strategy1", ForexImpl.class);
    
        Assert.assertEquals(1, combinations2.size());
    
        Assert.assertEquals(1, combinations2.get(0).getSubscriptions().size());
        Assert.assertSame(combination1, combinations2.get(0));
    }

    @Test
    public void testGetSubscribedComponentsBySecurityInclSecurity() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);
    
        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
    
        Combination combination1 = new CombinationImpl();
        combination1.setSecurityFamily(family1);
        combination1.setUuid("521ds5ds2d");
        combination1.setType(CombinationType.BUTTERFLY);
    
        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM.name());
        subscription1.setSecurity(combination1);
        subscription1.setStrategy(strategy1);
    
        this.session.save(strategy1);
        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(combination1);
        this.session.save(subscription1);
    
        combination1.addSubscriptions(subscription1);
    
        Component component1 = new ComponentImpl();
        component1.setSecurity(forex1);
        component1.setCombination(combination1);
    
        this.session.save(component1);
        this.session.flush();
    
        List<Component> components1 = (List<Component>) lookupService.getSubscribedComponentsBySecurityInclSecurity(0);
    
        Assert.assertEquals(0, components1.size());
    
        List<Component> components2 = (List<Component>) lookupService.getSubscribedComponentsBySecurityInclSecurity(forex1.getId());
    
        Assert.assertEquals(1, components2.size());
    
        Assert.assertSame(combination1, components2.get(0).getCombination());
        Assert.assertEquals(1, components2.get(0).getCombination().getSubscriptions().size());
        Assert.assertSame(forex1, components2.get(0).getSecurity());
    }

    @Test
    public void testGetSubscribedComponentsByStrategyAndSecurityInclSecurity() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);
    
        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
    
        Combination combination1 = new CombinationImpl();
        combination1.setSecurityFamily(family1);
        combination1.setUuid("521ds5ds2d");
        combination1.setType(CombinationType.BUTTERFLY);
    
        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM.name());
        subscription1.setSecurity(combination1);
        subscription1.setStrategy(strategy1);
    
        this.session.save(strategy1);
        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(combination1);
        this.session.save(subscription1);
    
        combination1.addSubscriptions(subscription1);
    
        Component component1 = new ComponentImpl();
        component1.setSecurity(forex1);
        component1.setCombination(combination1);
    
        this.session.save(component1);
        this.session.flush();
    
        List<Component> components1 = (List<Component>) lookupService.getSubscribedComponentsByStrategyAndSecurityInclSecurity("Dummy", forex1.getId());
    
        Assert.assertEquals(0, components1.size());
    
        List<Component> components2 = (List<Component>) lookupService.getSubscribedComponentsByStrategyAndSecurityInclSecurity("Strategy1", 0);
    
        Assert.assertEquals(0, components2.size());
    
        List<Component> components3 = (List<Component>) lookupService.getSubscribedComponentsByStrategyAndSecurityInclSecurity("Strategy1", forex1.getId());
    
        Assert.assertEquals(1, components3.size());
    
        Assert.assertSame(combination1, components3.get(0).getCombination());
        Assert.assertEquals(1, components3.get(0).getCombination().getSubscriptions().size());
        Assert.assertSame(forex1, components3.get(0).getSecurity());
    }

    @Test
    public void testGetSubscribedComponentsByStrategyInclSecurity() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);
    
        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
    
        Combination combination1 = new CombinationImpl();
        combination1.setSecurityFamily(family1);
        combination1.setUuid("521ds5ds2d");
        combination1.setType(CombinationType.BUTTERFLY);
    
        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM.name());
        subscription1.setSecurity(combination1);
        subscription1.setStrategy(strategy1);
    
        this.session.save(strategy1);
        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(combination1);
        this.session.save(subscription1);
    
        combination1.addSubscriptions(subscription1);
    
        Component component1 = new ComponentImpl();
        component1.setSecurity(forex1);
        component1.setCombination(combination1);
    
        this.session.save(component1);
        this.session.flush();
    
        List<Component> components1 = (List<Component>) lookupService.getSubscribedComponentsByStrategyInclSecurity("Dummy");
    
        Assert.assertEquals(0, components1.size());
    
        List<Component> components2 = (List<Component>) lookupService.getSubscribedComponentsByStrategyInclSecurity("Strategy1");
    
        Assert.assertEquals(1, components2.size());
    
        Assert.assertSame(combination1, components2.get(0).getCombination());
        Assert.assertEquals(1, components2.get(0).getCombination().getSubscriptions().size());
        Assert.assertSame(forex1, components2.get(0).getSecurity());
    }

    @Test
    public void testGetSubscriptionsByStrategyAndSecurity() {

        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);

        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);

        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");

        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(strategy1);

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM.name());
        subscription1.setSecurity(forex1);
        subscription1.setStrategy(strategy1);

        this.session.save(subscription1);
        this.session.flush();

        Subscription subscription2 = lookupService.getSubscriptionByStrategyAndSecurity("Strategy1", 0);

        Assert.assertNull(subscription2);

        Subscription subscription3 = lookupService.getSubscriptionByStrategyAndSecurity("Strategy1", forex1.getId());

        Assert.assertNotNull(subscription3);

        Assert.assertEquals(FeedType.SIM.name(), subscription3.getFeedType());
        Assert.assertSame(forex1, subscription3.getSecurity());
        Assert.assertSame(family1, subscription3.getSecurity().getSecurityFamily());
        Assert.assertSame(strategy1, subscription3.getStrategy());
    }

    @Test
    public void testGetNonPositionSubscriptions() {

        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);

        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);

        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");

        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(strategy1);

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM.name());
        subscription1.setSecurity(forex1);
        subscription1.setStrategy(strategy1);

        Subscription subscription2 = new SubscriptionImpl();
        subscription2.setFeedType(FeedType.IB.name());
        subscription2.setStrategy(strategy1);
        subscription2.setSecurity(forex1);

        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(forex1);
        position1.setStrategy(strategy1);

        this.session.save(subscription2);
        this.session.save(subscription1);
        this.session.save(position1);
        this.session.flush();

        List<Subscription> subscriptions1 = (List<Subscription>) lookupService.getNonPositionSubscriptions("Strategy1");

        Assert.assertEquals(0, subscriptions1.size());

        position1.setQuantity(0);

        this.session.flush();

        List<Subscription> subscriptions2 = (List<Subscription>) lookupService.getNonPositionSubscriptions("Strategy1");

        Assert.assertEquals(2, subscriptions2.size());
    }

    @Test
    public void testGetStrategyByName() {
    
        Strategy strategy1 = lookupService.getStrategyByName("blah");
        Assert.assertNull(strategy1);
    
        Strategy newStrategy = new StrategyImpl();
        newStrategy.setName("blah");
    
        this.session.save(newStrategy);
        this.session.flush();
    
        Strategy strategy3 = lookupService.getStrategyByName("blah");
    
        Assert.assertNotNull(strategy3);
        Assert.assertEquals("blah", strategy3.getName());
    }

    @Test
    public void testGetSecurityFamilyByName() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        this.session.save(family1);
        this.session.flush();
    
        SecurityFamily family2 = lookupService.getSecurityFamilyByName("NOT_FOUND");
    
        Assert.assertNull(family2);
    
        SecurityFamily family3 = lookupService.getSecurityFamilyByName("family1");
    
        Assert.assertNotNull(family3);
    
        Assert.assertSame(family1, family3);
    }

    @Test
    public void testGetOptionFamilyByUnderlying() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);
    
        OptionFamily optionFamily1 = new OptionFamilyImpl();
        optionFamily1.setName("OptionFamily");
        optionFamily1.setCurrency(Currency.INR);
        optionFamily1.setTickSizePattern("TickSizePattern");
        optionFamily1.setExpirationType(ExpirationType.NEXT_3_RD_FRIDAY);
        optionFamily1.setExpirationDistance(Duration.DAY_1);
        optionFamily1.setUnderlying(forex1);
    
        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(optionFamily1);
        this.session.flush();
    
        OptionFamily optionFamily2 = lookupService.getOptionFamilyByUnderlying(0);
    
        Assert.assertNull(optionFamily2);
    
        OptionFamily optionFamily3 = lookupService.getOptionFamilyByUnderlying(forex1.getId());
    
        Assert.assertNotNull(optionFamily3);
    
        Assert.assertSame(forex1, optionFamily3.getUnderlying());
        Assert.assertSame(optionFamily1, optionFamily3);
    }

    @Test
    public void testGetFutureFamilyByUnderlying() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);
    
        FutureFamily futureFamily1 = new FutureFamilyImpl();
        futureFamily1.setName("FutureFamily");
        futureFamily1.setCurrency(Currency.INR);
        futureFamily1.setTickSizePattern("TickSizePattern");
        futureFamily1.setExpirationType(ExpirationType.NEXT_3_RD_FRIDAY);
        futureFamily1.setExpirationDistance(Duration.DAY_1);
        futureFamily1.setUnderlying(forex1);
    
        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(futureFamily1);
        this.session.flush();
    
        FutureFamily futureFamily2 = lookupService.getFutureFamilyByUnderlying(0);
    
        Assert.assertNull(futureFamily2);
    
        FutureFamily futureFamily3 = lookupService.getFutureFamilyByUnderlying(forex1.getId());
    
        Assert.assertNotNull(futureFamily3);
    
        Assert.assertSame(forex1, futureFamily3.getUnderlying());
        Assert.assertSame(futureFamily1, futureFamily3);
    }

    @Test
    public void testGetExchangeByName() {

        Exchange exchange1 = lookupService.getExchangeByName("NASDAQ");

        Assert.assertNull(exchange1);

        txTemplate.execute(txStatus -> {

            Exchange exchange2 = new ExchangeImpl();
            exchange2.setName("NASDAQ");
            exchange2.setTimeZone(TimeZone.getDefault().getDisplayName());

            this.session.save(exchange2);
            this.session.flush();

            return null;
        });

        Exchange exchange3 = lookupService.getExchangeByName("NASDAQ");

        Assert.assertNotNull(exchange3);
        Assert.assertEquals("NASDAQ", exchange3.getName());
    }

    @Test
    public void testGetPositionByIdInclSecurityAndSecurityFamily() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);
    
        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
    
        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(strategy1);
    
        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(forex1);
        position1.setStrategy(strategy1);
    
        this.session.save(position1);
        this.session.flush();
    
        Position position2 = lookupService.getPositionInclSecurityAndSecurityFamily(1);
    
        Assert.assertNull(position2);
    
        Position position3 = lookupService.getPositionInclSecurityAndSecurityFamily(position1.getId());
    
        Assert.assertEquals(222, position3.getQuantity());
        Assert.assertSame(forex1, position3.getSecurity());
        Assert.assertSame(family1, position3.getSecurity().getSecurityFamily());
        Assert.assertSame(strategy1, position3.getStrategy());
    }

    @Test
    public void testGetPositionsByStrategy() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);
    
        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
    
        SecurityFamily family2 = new SecurityFamilyImpl();
        family2.setName("Forex2");
        family2.setTickSizePattern("0<0.1");
        family2.setCurrency(Currency.GBP);
    
        Forex forex2 = new ForexImpl();
        forex2.setSymbol("EUR.GBP");
        forex2.setBaseCurrency(Currency.EUR);
        forex2.setSecurityFamily(family2);
    
        Strategy strategy2 = new StrategyImpl();
        strategy2.setName("Strategy2");
    
        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(strategy1);
    
        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(forex1);
        position1.setStrategy(strategy1);
    
        this.session.save(position1);
    
        this.session.save(family2);
        this.session.save(forex2);
        this.session.save(strategy2);
    
        Position position2 = new PositionImpl();
        position2.setQuantity(222);
        position2.setSecurity(forex2);
        position2.setStrategy(strategy1);
    
        this.session.save(position2);
        this.session.flush();
    
        List<Position> positions1 = lookupService.getPositionsByStrategy("Dummy");
    
        Assert.assertEquals(0, positions1.size());
    
        List<Position> positions2 = lookupService.getPositionsByStrategy("Strategy1");
    
        Assert.assertEquals(2, positions2.size());
    
        Assert.assertEquals(222, positions2.get(0).getQuantity());
        Assert.assertSame(forex1, positions2.get(0).getSecurity());
        Assert.assertSame(family1, positions2.get(0).getSecurity().getSecurityFamily());
        Assert.assertSame(strategy1, positions2.get(0).getStrategy());
    
        Assert.assertEquals(222, positions2.get(1).getQuantity());
        Assert.assertSame(forex2, positions2.get(1).getSecurity());
        Assert.assertSame(family2, positions2.get(1).getSecurity().getSecurityFamily());
        Assert.assertSame(strategy1, positions2.get(1).getStrategy());
    }

    @Test
    public void testGetPositionBySecurityAndStrategy() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);
    
        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
    
        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(strategy1);
    
        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(forex1);
        position1.setStrategy(strategy1);
    
        this.session.save(position1);
        this.session.flush();
    
        Position position2 = lookupService.getPositionBySecurityAndStrategy(position1.getSecurity().getId(), "Dummy");

        Assert.assertNull(position2);

        Position position3 = lookupService.getPositionBySecurityAndStrategy(position1.getSecurity().getId(), "Strategy1");
    
        Assert.assertNotNull(position3);
    
        Assert.assertEquals(222, position3.getQuantity());
        Assert.assertSame(forex1, position3.getSecurity());
        Assert.assertSame(family1, position3.getSecurity().getSecurityFamily());
        Assert.assertSame(strategy1, position3.getStrategy());
    }

    @Test
    public void testGetOpenPositions() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);
    
        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
    
        SecurityFamily family2 = new SecurityFamilyImpl();
        family2.setName("Forex2");
        family2.setTickSizePattern("0<0.1");
        family2.setCurrency(Currency.GBP);

        Forex forex2 = new ForexImpl();
        forex2.setSymbol("EUR.GBP");
        forex2.setBaseCurrency(Currency.EUR);
        forex2.setSecurityFamily(family2);

        Strategy strategy2 = new StrategyImpl();
        strategy2.setName("Strategy2");

        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(strategy1);
    
        Position position1 = new PositionImpl();
        position1.setQuantity(0);
        position1.setSecurity(forex1);
        position1.setStrategy(strategy1);
    
        this.session.save(position1);
        this.session.flush();
    
        List<Position> positions1 = lookupService.getOpenPositions();
    
        Assert.assertEquals(0, positions1.size());
    
        this.session.save(family2);
        this.session.save(forex2);
        this.session.save(strategy2);

        Position position2 = new PositionImpl();
        position2.setQuantity(222);
        position2.setSecurity(forex2);
        position2.setStrategy(strategy2);

        this.session.save(position2);
        this.session.flush();

        List<Position> positions2 = lookupService.getOpenPositions();
    
        Assert.assertEquals(1, positions2.size());
    
        Assert.assertEquals(222, positions2.get(0).getQuantity());
        Assert.assertSame(forex2, positions2.get(0).getSecurity());
        Assert.assertSame(family2, positions2.get(0).getSecurity().getSecurityFamily());
        Assert.assertSame(strategy2, positions2.get(0).getStrategy());
    }

    @Test
    public void testGetOpenTradeablePositions() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);
    
        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
    
        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(strategy1);
    
        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(forex1);
        position1.setStrategy(strategy1);
    
        this.session.save(position1);
        this.session.flush();
    
        List<Position> positions1 = lookupService.getOpenTradeablePositions();
    
        Assert.assertEquals(0, positions1.size());
    
        family1.setTradeable(true);
        this.session.flush();
    
        List<Position> positions2 = lookupService.getOpenTradeablePositions();
    
        Assert.assertEquals(1, positions2.size());
    
        Assert.assertEquals(222, positions2.get(0).getQuantity());
        Assert.assertSame(forex1, positions2.get(0).getSecurity());
        Assert.assertSame(family1, positions2.get(0).getSecurity().getSecurityFamily());
        Assert.assertSame(strategy1, positions2.get(0).getStrategy());
    }

    @Test
    public void testGetOpenPositionsByStrategy() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);
    
        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
    
        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(strategy1);
    
        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(forex1);
        position1.setStrategy(strategy1);
    
        this.session.save(position1);
        this.session.flush();
    
        List<Position> positions1 = lookupService.getOpenPositionsByStrategy("Dummy");
    
        Assert.assertEquals(0, positions1.size());
    
        List<Position> positions2 = lookupService.getOpenPositionsByStrategy("Strategy1");
    
        Assert.assertEquals(1, positions2.size());
    
        Assert.assertEquals(222, positions2.get(0).getQuantity());
        Assert.assertSame(forex1, positions2.get(0).getSecurity());
        Assert.assertSame(family1, positions2.get(0).getSecurity().getSecurityFamily());
        Assert.assertSame(strategy1, positions2.get(0).getStrategy());
    }

    @Test
    public void testGetOpenTradeablePositionsByStrategy() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);
    
        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
    
        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(strategy1);
    
        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(forex1);
        position1.setStrategy(strategy1);
    
        this.session.save(position1);
        this.session.flush();
    
        List<Position> positions1 = lookupService.getOpenTradeablePositionsByStrategy("Dummy");
    
        Assert.assertEquals(0, positions1.size());
    
        family1.setTradeable(true);
        this.session.flush();

        List<Position> positions2 = lookupService.getOpenTradeablePositionsByStrategy("Strategy1");
    
        Assert.assertEquals(1, positions2.size());
    
        Assert.assertEquals(222, positions2.get(0).getQuantity());
        Assert.assertSame(forex1, positions2.get(0).getSecurity());
        Assert.assertSame(family1, positions2.get(0).getSecurity().getSecurityFamily());
        Assert.assertSame(strategy1, positions2.get(0).getStrategy());
    }

    @Test
    public void testGetOpenPositionsBySecurity() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);
    
        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
    
        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(strategy1);
    
        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(forex1);
        position1.setStrategy(strategy1);
    
        this.session.save(position1);
        this.session.flush();
    
        List<Position> positions1 = lookupService.getOpenPositionsBySecurity(0);

        Assert.assertEquals(0, positions1.size());
    
        List<Position> positions2 = lookupService.getOpenPositionsBySecurity(forex1.getId());

        Assert.assertEquals(1, positions2.size());
    
        Assert.assertEquals(222, positions2.get(0).getQuantity());
        Assert.assertSame(forex1, positions2.get(0).getSecurity());
        Assert.assertSame(family1, positions2.get(0).getSecurity().getSecurityFamily());
        Assert.assertSame(strategy1, positions2.get(0).getStrategy());
    }

    @Test
    public void testGetOpenPositionsByStrategyAndType() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);
    
        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
    
        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(strategy1);
    
        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(forex1);
        position1.setStrategy(strategy1);
    
        this.session.save(position1);
        this.session.flush();
    
        List<Position> positions1 = lookupService.getOpenPositionsByStrategyAndType("Dummy", Security.class);
    
        Assert.assertEquals(0, positions1.size());
    
        List<Position> positions2 = lookupService.getOpenPositionsByStrategyAndType("Strategy1", Forex.class);
    
        Assert.assertEquals(1, positions2.size());
    
        Assert.assertEquals(222, positions2.get(0).getQuantity());
        Assert.assertSame(forex1, positions2.get(0).getSecurity());
        Assert.assertSame(family1, positions2.get(0).getSecurity().getSecurityFamily());
        Assert.assertSame(strategy1, positions2.get(0).getStrategy());
    }

    @Test
    public void testGetOpenPositionsByStrategyTypeAndUnderlyingType() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);
    
        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
    
        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(strategy1);
    
        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(forex1);
        position1.setStrategy(strategy1);
    
        this.session.save(position1);
        this.session.flush();
    
        List<Position> positions1 = lookupService.getOpenPositionsByStrategyTypeAndUnderlyingType("Dummy", Security.class, SecurityFamily.class);
    
        Assert.assertEquals(0, positions1.size());
    
        List<Position> positions2 = lookupService.getOpenPositionsByStrategyTypeAndUnderlyingType("Strategy1", Forex.class, SecurityFamily.class);
    
        Assert.assertEquals(1, positions2.size());
    
        Assert.assertEquals(222, positions2.get(0).getQuantity());
        Assert.assertSame(forex1, positions2.get(0).getSecurity());
        Assert.assertSame(family1, positions2.get(0).getSecurity().getSecurityFamily());
        Assert.assertSame(strategy1, positions2.get(0).getStrategy());
    }

    @Test
    public void testGetOpenPositionsByStrategyAndSecurityFamily() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);
    
        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
    
        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(strategy1);
    
        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(forex1);
        position1.setStrategy(strategy1);
    
        this.session.save(position1);
        this.session.flush();
    
        List<Position> positions1 = lookupService.getOpenPositionsByStrategyAndSecurityFamily("Dummy", 1);
        Assert.assertEquals(0, positions1.size());
    
        List<Position> positions2 = lookupService.getOpenPositionsByStrategyAndSecurityFamily("Strategy1", family1.getId());
        Assert.assertEquals(1, positions2.size());
    
        Assert.assertEquals(222, positions2.get(0).getQuantity());
        Assert.assertSame(forex1, positions2.get(0).getSecurity());
        Assert.assertSame(family1, positions2.get(0).getSecurity().getSecurityFamily());
        Assert.assertSame(strategy1, positions2.get(0).getStrategy());
    }

    @Test
    public void testGetOpenFXPositions() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);
    
        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
    
        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(strategy1);
    
        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(forex1);
        position1.setStrategy(strategy1);
    
        this.session.save(position1);
        this.session.flush();
    
        List<Position> positions1 = lookupService.getOpenFXPositions();

        Assert.assertEquals(1, positions1.size());

        Assert.assertEquals(222, positions1.get(0).getQuantity());
        Assert.assertSame(forex1, positions1.get(0).getSecurity());
        Assert.assertSame(family1, positions1.get(0).getSecurity().getSecurityFamily());
        Assert.assertSame(strategy1, positions1.get(0).getStrategy());
    }

    @Test
    public void testGetOpenFXPositionsByStrategy() {

        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);

        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);

        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");

        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(strategy1);
    
        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(forex1);
        position1.setStrategy(strategy1);
    
        this.session.save(position1);
        this.session.flush();
    
        List<Position> positions1 = lookupService.getOpenFXPositionsByStrategy("Dummy");

        Assert.assertEquals(0, positions1.size());

        List<Position> positions2 = lookupService.getOpenFXPositionsByStrategy("Strategy1");
    
        Assert.assertEquals(1, positions2.size());
    
        Assert.assertEquals(222, positions2.get(0).getQuantity());
        Assert.assertSame(forex1, positions2.get(0).getSecurity());
        Assert.assertSame(family1, positions2.get(0).getSecurity().getSecurityFamily());
        Assert.assertSame(strategy1, positions2.get(0).getStrategy());
    }

    @Test
    public void testGetDailyTransactionsDesc() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);
    
        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
    
        SecurityFamily family2 = new SecurityFamilyImpl();
        family2.setName("Forex2");
        family2.setTickSizePattern("0<0.1");
        family2.setCurrency(Currency.GBP);
    
        Forex forex2 = new ForexImpl();
        forex2.setSymbol("EUR.GBP");
        forex2.setBaseCurrency(Currency.EUR);
        forex2.setSecurityFamily(family2);
    
        Strategy strategy2 = new StrategyImpl();
        strategy2.setName("Strategy2");
    
        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(strategy1);
    
        Transaction transaction1 = new TransactionImpl();
        transaction1.setUuid(UUID.randomUUID().toString());
        transaction1.setSecurity(forex1);
        transaction1.setQuantity(222);
        transaction1.setDateTime(new Date());
        transaction1.setPrice(new BigDecimal(111));
        transaction1.setCurrency(Currency.INR);
        transaction1.setType(TransactionType.CREDIT);
        transaction1.setStrategy(strategy1);
    
        this.session.save(family2);
        this.session.save(forex2);
        this.session.save(strategy2);
    
        Transaction transaction2 = new TransactionImpl();
        transaction2.setUuid(UUID.randomUUID().toString());
        transaction2.setSecurity(forex2);
        transaction2.setQuantity(222);
    
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
    
        transaction2.setDateTime(calendar.getTime());
        transaction2.setPrice(new BigDecimal(111));
        transaction2.setCurrency(Currency.NZD);
        transaction2.setType(TransactionType.BUY);
        transaction2.setStrategy(strategy2);

        List<Transaction> transactionVOs1 = lookupService.getDailyTransactionsDesc();

        Assert.assertEquals(0, transactionVOs1.size());
    
        this.session.save(transaction1);
        this.session.save(transaction2);
        this.session.flush();
    
        List<Transaction> transactionVOs2 = lookupService.getDailyTransactionsDesc();
    
        Assert.assertEquals(1, transactionVOs2.size());
    
        Transaction transactionVO1 = transactionVOs2.get(0);
        Assert.assertEquals(222, transactionVO1.getQuantity());
        Assert.assertEquals(new BigDecimal(111), transactionVO1.getPrice());
        Assert.assertEquals(Currency.INR, transactionVO1.getCurrency());
        Assert.assertEquals(TransactionType.CREDIT, transactionVO1.getType());
        Assert.assertEquals(strategy1, transactionVO1.getStrategy());
    
    }

    @Test
    public void testGetDailyTransactionsByStrategyDesc() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);
    
        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
    
        SecurityFamily family2 = new SecurityFamilyImpl();
        family2.setName("Forex2");
        family2.setTickSizePattern("0<0.1");
        family2.setCurrency(Currency.GBP);
    
        Forex forex2 = new ForexImpl();
        forex2.setSymbol("EUR.GBP");
        forex2.setBaseCurrency(Currency.EUR);
        forex2.setSecurityFamily(family2);
    
        Strategy strategy2 = new StrategyImpl();
        strategy2.setName("Strategy2");
    
        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(strategy1);
    
        Transaction transaction1 = new TransactionImpl();
        transaction1.setUuid(UUID.randomUUID().toString());
        transaction1.setSecurity(forex1);
        transaction1.setQuantity(222);
        transaction1.setDateTime(new Date());
        transaction1.setPrice(new BigDecimal(111));
        transaction1.setCurrency(Currency.INR);
        transaction1.setType(TransactionType.CREDIT);
        transaction1.setStrategy(strategy1);
    
        this.session.save(transaction1);
        this.session.save(family2);
        this.session.save(forex2);
        this.session.save(strategy2);
    
        Transaction transaction2 = new TransactionImpl();
        transaction2.setUuid(UUID.randomUUID().toString());
        transaction2.setSecurity(forex2);
        transaction2.setQuantity(222);
    
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
    
        transaction2.setDateTime(calendar.getTime());
        transaction2.setPrice(new BigDecimal(111));
        transaction2.setCurrency(Currency.NZD);
        transaction2.setType(TransactionType.BUY);
        transaction2.setStrategy(strategy1);

        this.session.save(transaction2);
        this.session.flush();
    
        List<Transaction> transactionVOs1 = lookupService.getDailyTransactionsByStrategyDesc("Dummy");
    
        Assert.assertEquals(0, transactionVOs1.size());
    
        List<Transaction> transactionVOs2 = lookupService.getDailyTransactionsByStrategyDesc("Strategy1");
    
        Assert.assertEquals(1, transactionVOs2.size());
    
        Assert.assertEquals(222, transactionVOs2.get(0).getQuantity());
        Assert.assertEquals(new BigDecimal(111), transactionVOs2.get(0).getPrice());
        Assert.assertEquals(Currency.INR, transactionVOs2.get(0).getCurrency());
        Assert.assertEquals(TransactionType.CREDIT, transactionVOs2.get(0).getType());
        Assert.assertEquals(strategy1, transactionVOs2.get(0).getStrategy());
    }

    @Test
    public void testGetAccountByName() {
    
        Account account1 = lookupService.getAccountByName("name1");
    
        Assert.assertNull(account1);
    
        Account account2 = new AccountImpl();
    
        account2.setName("name2");
        account2.setBroker(Broker.CNX.name());
        account2.setOrderServiceType(OrderServiceType.CNX_FIX.name());
    
        this.session.save(account2);
        this.session.flush();
    
        Account account3 = lookupService.getAccountByName("name2");
    
        Assert.assertNotNull(account3);
        Assert.assertEquals("name2", account3.getName());
        Assert.assertEquals(Broker.CNX.name(), account3.getBroker());
        Assert.assertEquals(OrderServiceType.CNX_FIX.name(), account3.getOrderServiceType());
    }

    @Test
    public void testgetActiveSessionsByOrderServiceType() {

        List<String> activeSessions1 = (List<String>) lookupService.getActiveSessionsByOrderServiceType(OrderServiceType.FTX_FIX.name());

        Assert.assertEquals(0, activeSessions1.size());

        Account account1 = new AccountImpl();

        account1.setName("name1");
        account1.setSessionQualifier("qualifier1");
        account1.setBroker(Broker.CNX.name());
        account1.setActive(true);
        account1.setOrderServiceType(OrderServiceType.CNX_FIX.name());

        Account account2 = new AccountImpl();

        account2.setName("name2");
        account2.setSessionQualifier("qualifier2");
        account2.setBroker(Broker.CNX.name());
        account2.setActive(false);
        account2.setOrderServiceType(OrderServiceType.CNX_FIX.name());

        Account account3 = new AccountImpl();

        account3.setName("name3");
        account3.setSessionQualifier("qualifier3");
        account3.setBroker(Broker.DC.name());
        account3.setActive(true);
        account3.setOrderServiceType(OrderServiceType.DC_FIX.name());

        this.session.save(account1);
        this.session.save(account2);
        this.session.save(account3);
        this.session.flush();

        List<String> activeSessions2 = (List<String>) lookupService.getActiveSessionsByOrderServiceType(OrderServiceType.CNX_FIX.name());

        Assert.assertEquals(1, activeSessions2.size());
        Assert.assertEquals(account1.getSessionQualifier(), activeSessions2.get(0));

        List<String> activeSessions3 = (List<String>) lookupService.getActiveSessionsByOrderServiceType(OrderServiceType.DC_FIX.name());

        Assert.assertEquals(1, activeSessions3.size());
        Assert.assertEquals(account3.getSessionQualifier(), activeSessions3.get(0));
    }

    public void testGetLastTick() {
        // cannot test due to Hibernate Dialect specific function
    }

    public void testGetTicksByMaxDate() {
        // cannot test due to Hibernate Dialect specific function
    }

    public void testGetTicksByMinDate() {
        // cannot test due to Hibernate Dialect specific function
    }

    public void testGetDailyTicksBeforeTime() {
        // cannot test due to Hibernate Dialect specific function
    }

    public void testGetDailyTicksAfterTime() {
        // cannot test due to Hibernate Dialect specific function
    }

    public void testGetHourlyTicksBeforeMinutesByMinDate() {
        // cannot test due to Hibernate Dialect specific function
    }

    public void testGetHourlyTicksAfterMinutesByMinDate() {
        // cannot test due to Hibernate Dialect specific function
    }

    public void testGetTickBySecurityAndMaxDate() {
        // cannot test due to Hibernate Dialect specific function
    }

    @Test
    public void testGetLastNBarsBySecurityAndBarSize() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);
    
        this.session.save(family1);
        this.session.save(forex1);

        Bar bar1 = new BarImpl();
        bar1.setDateTime(new Date());
        bar1.setBarSize(Duration.MIN_1);
        bar1.setOpen(new BigDecimal(222));
        bar1.setHigh(new BigDecimal(333));
        bar1.setLow(new BigDecimal(111));
        bar1.setClose(new BigDecimal(444));
        bar1.setFeedType(FeedType.CNX.name());
        bar1.setSecurity(forex1);
    
        Bar bar2 = new BarImpl();
    
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -2);
    
        bar2.setDateTime(cal.getTime());
        bar2.setBarSize(Duration.MIN_1);
        bar2.setOpen(new BigDecimal(555));
        bar2.setHigh(new BigDecimal(666));
        bar2.setLow(new BigDecimal(777));
        bar2.setClose(new BigDecimal(888));
        bar2.setFeedType(FeedType.BB.name());
        bar2.setSecurity(forex1);
    
        this.session.save(bar1);
        this.session.save(bar2);
        this.session.flush();
    
        int limit1 = 1;
        List<Bar> bars1 = lookupService.getLastNBarsBySecurityAndBarSize(limit1, forex1.getId(), Duration.MIN_1);
    
        Assert.assertEquals(1, bars1.size());
        Assert.assertSame(bar1, bars1.get(0));
    
        int limit2 = 2;
        List<Bar> bars2 = lookupService.getLastNBarsBySecurityAndBarSize(limit2, forex1.getId(), Duration.MIN_1);
    
        Assert.assertEquals(2, bars2.size());
        Assert.assertSame(bar1, bars2.get(0));
        Assert.assertSame(bar2, bars2.get(1));
    }

    @Test
    public void testGetBarsBySecurityBarSizeAndMinDate() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);
    
        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);
    
        Bar bar1 = new BarImpl();
        bar1.setDateTime(new Date());
        bar1.setBarSize(Duration.MIN_1);
        bar1.setOpen(new BigDecimal(222));
        bar1.setHigh(new BigDecimal(333));
        bar1.setLow(new BigDecimal(111));
        bar1.setClose(new BigDecimal(444));
        bar1.setFeedType(FeedType.CNX.name());
        bar1.setSecurity(forex1);
    
        Bar bar2 = new BarImpl();
    
        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.DAY_OF_MONTH, -2);
    
        bar2.setDateTime(cal1.getTime());
        bar2.setBarSize(Duration.MIN_1);
        bar2.setOpen(new BigDecimal(555));
        bar2.setHigh(new BigDecimal(666));
        bar2.setLow(new BigDecimal(777));
        bar2.setClose(new BigDecimal(888));
        bar2.setFeedType(FeedType.BB.name());
        bar2.setSecurity(forex1);
    
        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(bar1);
        this.session.save(bar2);
        this.session.flush();
    
        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_MONTH, -1);

        List<Bar> bars1 = lookupService.getBarsBySecurityBarSizeAndMinDate(forex1.getId(), Duration.MIN_1, cal2.getTime());
    
        Assert.assertEquals(1, bars1.size());
        Assert.assertSame(bar1, bars1.get(0));
    
        Calendar cal3 = Calendar.getInstance();
        cal3.add(Calendar.DAY_OF_MONTH, -3);

        List<Bar> bars2 = lookupService.getBarsBySecurityBarSizeAndMinDate(forex1.getId(), Duration.MIN_1, cal3.getTime());
    
        Assert.assertEquals(2, bars2.size());
        Assert.assertSame(bar1, bars2.get(0));
        Assert.assertSame(bar2, bars2.get(1));
    }

    @Test
    public void testGetForex() {
    
        SecurityFamily family = new SecurityFamilyImpl();
        family.setName("Forex1");
        family.setTickSizePattern("0<0.1");
        family.setCurrency(Currency.INR);
    
        Forex forex1 = new ForexImpl();
        forex1.setSecurityFamily(family);
        forex1.setBaseCurrency(Currency.USD);
    
        this.session.save(family);
        this.session.save(forex1);
        this.session.flush();
    
        Forex forex2 = lookupService.getForex(Currency.USD, Currency.INR);
    
        Assert.assertNotNull(forex2);
    
        Assert.assertSame(forex1, forex2);
        Assert.assertSame(family, forex2.getSecurityFamily());
    
        Forex forex3 = lookupService.getForex(Currency.INR, Currency.USD);
    
        Assert.assertNotNull(forex3);
    
        Assert.assertSame(forex1, forex3);
        Assert.assertSame(family, forex3.getSecurityFamily());
    }

    @Test
    public void testGetForexException1() {
    
        SecurityFamily family = new SecurityFamilyImpl();
        family.setName("Forex1");
        family.setTickSizePattern("0<0.1");
        family.setCurrency(Currency.INR);
    
        Forex forex1 = new ForexImpl();
        forex1.setSecurityFamily(family);
        forex1.setBaseCurrency(Currency.USD);
    
        this.session.save(family);
        this.session.save(forex1);
        this.session.flush();
    
        this.exception.expect(IllegalStateException.class);
    
        lookupService.getForex(Currency.AUD, Currency.INR);
    }

    @Test
    public void testGetForexException2() {
    
        SecurityFamily family = new SecurityFamilyImpl();
        family.setName("Forex1");
        family.setTickSizePattern("0<0.1");
        family.setCurrency(Currency.INR);
    
        this.session.save(family);
        this.session.flush();
    
        Forex forex1 = new ForexImpl();
        forex1.setSecurityFamily(family);
        forex1.setBaseCurrency(Currency.USD);
    
        this.session.save(forex1);
        this.session.flush();
    
        this.exception.expect(IllegalStateException.class);
    
        lookupService.getForex(Currency.USD, Currency.AUD);
    }

    @Test
    public void testGetRateDoubleByDate() {
    
        SecurityFamily family = new SecurityFamilyImpl();
        family.setName("Forex1");
        family.setTickSizePattern("0<0.1");
        family.setCurrency(Currency.INR);
    
        Forex forex1 = new ForexImpl();
        forex1.setSecurityFamily(family);
        forex1.setBaseCurrency(Currency.USD);
    
        this.session.save(family);
        this.session.save(forex1);
        this.session.flush();
    
        double rate = lookupService.getForexRateByDate(Currency.USD, Currency.USD, new Date());
    
        Assert.assertEquals(1.0, rate, 0);
    
        // Could not test further
        // Caused by: org.h2.jdbc.JdbcSQLException: Function "FROM_UNIXTIME" not found
        // Because of TickDao.findTicksBySecurityAndMaxDate call
    }

    @Test
    public void testGetRateDoubleByDateException() {
    
        // Could not test
        // Caused by: org.h2.jdbc.JdbcSQLException: Function "FROM_UNIXTIME" not found
        // Because of TickDao.findTicksBySecurityAndMaxDate call
    }

    @Test
    public void testGetRateDoubleException() {
    
        // Could not test the method for IllegalStateException due to EngineLocator dependency in forex.getCurrentMarketDataEvent() method
    }

    @Test
    public void testGetInterestRateByCurrencyAndDuration() {
    
        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.INR);
    
        IntrestRate intrestRate1 = new IntrestRateImpl();
        intrestRate1.setSecurityFamily(family1);
        intrestRate1.setDuration(Duration.DAY_2);
    
        this.session.save(family1);
        this.session.save(intrestRate1);
        this.session.flush();
    
        IntrestRate intrestRate2 = lookupService.getInterestRateByCurrencyAndDuration(Currency.USD, Duration.DAY_2);
    
        Assert.assertNull(intrestRate2);
    
        IntrestRate intrestRate3 = lookupService.getInterestRateByCurrencyAndDuration(Currency.INR, Duration.DAY_1);
    
        Assert.assertNull(intrestRate3);
    
        IntrestRate intrestRate4 = lookupService.getInterestRateByCurrencyAndDuration(Currency.INR, Duration.DAY_2);
    
        Assert.assertNotNull(intrestRate4);
    
        Assert.assertSame(family1, intrestRate4.getSecurityFamily());
        Assert.assertSame(intrestRate1, intrestRate4);
    }

    public void testGetInterestRateByCurrencyDurationAndDate() {
        // cannot test due to Hibernate Dialect specific function
    }

    @Test
    public void testGetHeldCurrencies() {
    
        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
    
        Strategy strategy2 = new StrategyImpl();
        strategy2.setName("Strategy2");
    
        CashBalance cashBalance1 = new CashBalanceImpl();
        cashBalance1.setCurrency(Currency.USD);
        cashBalance1.setAmount(new BigDecimal(111));
        cashBalance1.setStrategy(strategy1);
    
        CashBalance cashBalance2 = new CashBalanceImpl();
        cashBalance2.setCurrency(Currency.USD);
        cashBalance2.setAmount(new BigDecimal(222));
        cashBalance2.setStrategy(strategy2);
    
        this.session.save(strategy1);
        this.session.save(strategy2);
        this.session.save(cashBalance1);
        this.session.save(cashBalance2);
        this.session.flush();
    
        List<Currency> currencies1 = (List<Currency>) lookupService.getHeldCurrencies();
    
        Assert.assertEquals(1, currencies1.size());
    
        Assert.assertEquals(Currency.USD, currencies1.get(0));
    
        cashBalance2.setCurrency(Currency.AUD);
        this.session.flush();
    
        List<Currency> currencies2 = (List<Currency>) lookupService.getHeldCurrencies();
    
        Assert.assertEquals(2, currencies2.size());
    
        Assert.assertEquals(Currency.USD, currencies2.get(0));
        Assert.assertEquals(Currency.AUD, currencies2.get(1));
    }

    @Test
    public void testGetCashBalancesByStrategy() {
    
        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
    
        Strategy strategy2 = new StrategyImpl();
        strategy2.setName("Strategy2");
    
        CashBalance cashBalance1 = new CashBalanceImpl();
        cashBalance1.setCurrency(Currency.USD);
        cashBalance1.setAmount(new BigDecimal(111));
        cashBalance1.setStrategy(strategy1);
    
        CashBalance cashBalance2 = new CashBalanceImpl();
        cashBalance2.setCurrency(Currency.AUD);
        cashBalance2.setAmount(new BigDecimal(222));
        cashBalance2.setStrategy(strategy2);
    
        this.session.save(strategy1);
        this.session.save(strategy2);
        this.session.save(cashBalance1);
        this.session.save(cashBalance2);
        this.session.flush();
    
        List<CashBalance> cashBalances1 = (List<CashBalance>) lookupService.getCashBalancesByStrategy("NOT_FOUND");

        Assert.assertEquals(0, cashBalances1.size());

        List<CashBalance> cashBalances2 = (List<CashBalance>) lookupService.getCashBalancesByStrategy("Strategy1");

        Assert.assertEquals(1, cashBalances2.size());
    
        Assert.assertSame(cashBalance1.getStrategy(), cashBalances2.get(0).getStrategy());
        Assert.assertSame(cashBalance1, cashBalances2.get(0));
    
        cashBalance2.setStrategy(strategy1);
    
        this.session.flush();
    
        List<CashBalance> cashBalances3 = (List<CashBalance>) lookupService.getCashBalancesByStrategy("Strategy1");
    
        Assert.assertEquals(2, cashBalances3.size());
    
        Assert.assertSame(cashBalance1.getStrategy(), cashBalances3.get(0).getStrategy());
        Assert.assertSame(cashBalance1, cashBalances3.get(0));
        Assert.assertSame(cashBalance2.getStrategy(), cashBalances3.get(1).getStrategy());
        Assert.assertSame(cashBalance2, cashBalances3.get(1));
    }

    @Test
    public void testGetMeasurementsByMaxDate() {
    
        Strategy strategy = new StrategyImpl();
        strategy.setName("Strategy1");
    
        Calendar cal1 = Calendar.getInstance();
    
        Measurement measurement1 = new MeasurementImpl();
        measurement1.setName("Measurement");
        measurement1.setValue(12);
        measurement1.setDateTime(cal1.getTime());
        measurement1.setStrategy(strategy);
    
        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.HOUR_OF_DAY, 1);
    
        Measurement measurement2 = new MeasurementImpl();
        measurement2.setName("Measurement");
        measurement2.setValue(13);
        measurement2.setDateTime(cal2.getTime());
        measurement2.setStrategy(strategy);
    
        this.session.save(strategy);
        this.session.save(measurement1);
        this.session.save(measurement2);
        this.session.flush();
    
        Map<Date, Object> measurements1 = lookupService.getMeasurementsByMaxDate("Dummy", "Measurement", cal1.getTime());
    
        Assert.assertEquals(0, measurements1.size());
    
        Map<Date, Object> measurements2 = lookupService.getMeasurementsByMaxDate("Strategy1", "Dummy", cal1.getTime());

        Assert.assertEquals(0, measurements2.size());

        Calendar before = Calendar.getInstance();
        before.add(Calendar.HOUR_OF_DAY, -1);
    
        Map<Date, Object> measurements3 = lookupService.getMeasurementsByMaxDate("Strategy1", "Measurement", before.getTime());
    
        Assert.assertEquals(0, measurements3.size());
    
        Map<Date, Object> measurements4 = lookupService.getMeasurementsByMaxDate("Strategy1", "Measurement", cal1.getTime());
    
        Assert.assertEquals(1, measurements4.size());
    
        Assert.assertEquals(measurement1.getValue(), measurements4.get(cal1.getTime()));
    
        Map<Date, Object> measurements5 = lookupService.getMeasurementsByMaxDate("Strategy1", "Measurement", cal2.getTime());
    
        Assert.assertEquals(2, measurements5.size());
    
        Assert.assertSame(measurement1.getValue(), measurements5.get(cal1.getTime()));
        Assert.assertSame(measurement2.getValue(), measurements5.get(cal2.getTime()));
    }

    @Test
    public void testGetAllMeasurementsByMaxDate() {
    
        Strategy strategy = new StrategyImpl();
        strategy.setName("Strategy1");
    
        Calendar cal1 = Calendar.getInstance();
    
        Measurement measurement1 = new MeasurementImpl();
        measurement1.setName("Measurement1");
        measurement1.setValue(12);
        measurement1.setDateTime(cal1.getTime());
        measurement1.setStrategy(strategy);
    
        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.HOUR_OF_DAY, 1);
    
        Measurement measurement2 = new MeasurementImpl();
        measurement2.setName("Measurement2");
        measurement2.setValue(13);
        measurement2.setDateTime(cal2.getTime());
        measurement2.setStrategy(strategy);
    
        this.session.save(strategy);
        this.session.save(measurement1);
        this.session.save(measurement2);
        this.session.flush();
    
        Map<Date, Map<String, Object>> measurements1 = lookupService.getAllMeasurementsByMaxDate("Dummy", cal1.getTime());
    
        Assert.assertEquals(0, measurements1.size());
    
        Calendar before = Calendar.getInstance();
        before.add(Calendar.HOUR_OF_DAY, -1);
    
        Map<Date, Map<String, Object>> measurements2 = lookupService.getAllMeasurementsByMaxDate("Strategy1", before.getTime());
    
        Assert.assertEquals(0, measurements2.size());
    
        Map<Date, Map<String, Object>> measurements3 = lookupService.getAllMeasurementsByMaxDate("Strategy1", cal1.getTime());
    
        Assert.assertEquals(1, measurements3.size());
    
        Assert.assertSame(measurement1.getValue(), measurements3.get(cal1.getTime()).get("Measurement1"));
    
        Map<Date, Map<String, Object>> measurements4 = lookupService.getAllMeasurementsByMaxDate("Strategy1", cal2.getTime());
    
        Assert.assertEquals(2, measurements4.size());
    
        Assert.assertSame(measurement1.getValue(), measurements4.get(cal1.getTime()).get("Measurement1"));
        Assert.assertSame(measurement2.getValue(), measurements4.get(cal2.getTime()).get("Measurement2"));
    }

    @Test
    public void testGetMeasurementsByMinDate() {
    
        Strategy strategy = new StrategyImpl();
        strategy.setName("Strategy1");
    
        Calendar cal1 = Calendar.getInstance();
    
        Measurement measurement1 = new MeasurementImpl();
        measurement1.setName("Measurement");
        measurement1.setValue(12);
        measurement1.setDateTime(cal1.getTime());
        measurement1.setStrategy(strategy);
    
        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.HOUR_OF_DAY, 1);
    
        Measurement measurement2 = new MeasurementImpl();
        measurement2.setName("Measurement");
        measurement2.setValue(13);
        measurement2.setDateTime(cal2.getTime());
        measurement2.setStrategy(strategy);
    
        this.session.save(strategy);
        this.session.save(measurement1);
        this.session.save(measurement2);
        this.session.flush();
    
        Map<Date, Object> measurements1 = lookupService.getMeasurementsByMinDate("Dummy", "Measurement", cal1.getTime());

        Assert.assertEquals(0, measurements1.size());

        Map<Date, Object> measurements2 = lookupService.getMeasurementsByMinDate("Strategy1", "Dummy", cal1.getTime());
    
        Assert.assertEquals(0, measurements2.size());
    
        Calendar after = Calendar.getInstance();
        after.add(Calendar.HOUR_OF_DAY, 2);
    
        Map<Date, Object> measurements3 = lookupService.getMeasurementsByMinDate("Strategy1", "Measurement", after.getTime());
    
        Assert.assertEquals(0, measurements3.size());
    
        Map<Date, Object> measurements4 = lookupService.getMeasurementsByMinDate("Strategy1", "Measurement", cal2.getTime());
    
        Assert.assertEquals(1, measurements4.size());
    
        Assert.assertSame(measurement2.getValue(), measurements4.get(cal2.getTime()));
    
        Map<Date, Object> measurements5 = lookupService.getMeasurementsByMinDate("Strategy1", "Measurement", cal1.getTime());
    
        Assert.assertEquals(2, measurements5.size());
    
        Assert.assertSame(measurement1.getValue(), measurements5.get(cal1.getTime()));
        Assert.assertSame(measurement2.getValue(), measurements5.get(cal2.getTime()));
    }

    @Test
    public void testGetAllMeasurementsByMinDate() {
    
        Strategy strategy = new StrategyImpl();
        strategy.setName("Strategy1");
    
        Calendar cal1 = Calendar.getInstance();
    
        Measurement measurement1 = new MeasurementImpl();
        measurement1.setName("Measurement1");
        measurement1.setValue(12);
        measurement1.setDateTime(cal1.getTime());
        measurement1.setStrategy(strategy);
    
        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.HOUR_OF_DAY, 1);
    
        Measurement measurement2 = new MeasurementImpl();
        measurement2.setName("Measurement2");
        measurement2.setValue(13);
        measurement2.setDateTime(cal2.getTime());
        measurement2.setStrategy(strategy);
    
        this.session.save(strategy);
        this.session.save(measurement1);
        this.session.save(measurement2);
        this.session.flush();
    
        Map<Date, Map<String, Object>> measurements1 = lookupService.getAllMeasurementsByMinDate("Dummy", cal1.getTime());
    
        Assert.assertEquals(0, measurements1.size());
    
        Calendar after = Calendar.getInstance();
        after.add(Calendar.HOUR_OF_DAY, 2);
    
        Map<Date, Map<String, Object>> measurements2 = lookupService.getAllMeasurementsByMinDate("Strategy1", after.getTime());
    
        Assert.assertEquals(0, measurements2.size());
    
        Map<Date, Map<String, Object>> measurements3 = lookupService.getAllMeasurementsByMinDate("Strategy1", cal2.getTime());
    
        Assert.assertEquals(1, measurements3.size());
    
        Assert.assertSame(measurement2.getValue(), measurements3.get(cal2.getTime()).get("Measurement2"));
    
        Map<Date, Map<String, Object>> measurements4 = lookupService.getAllMeasurementsByMinDate("Strategy1", cal1.getTime());
    
        Assert.assertEquals(2, measurements4.size());
    
        Assert.assertSame(measurement1.getValue(), measurements4.get(cal1.getTime()).get("Measurement1"));
        Assert.assertSame(measurement2.getValue(), measurements4.get(cal2.getTime()).get("Measurement2"));
    }

    @Test
    public void testGetMeasurementByMaxDate() {
    
        Strategy strategy = new StrategyImpl();
        strategy.setName("Strategy1");
    
        Calendar cal1 = Calendar.getInstance();
    
        Measurement measurement1 = new MeasurementImpl();
        measurement1.setName("Measurement");
        measurement1.setValue(12);
        measurement1.setDateTime(cal1.getTime());
        measurement1.setStrategy(strategy);
    
        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.HOUR_OF_DAY, 1);
    
        Measurement measurement2 = new MeasurementImpl();
        measurement2.setName("Measurement");
        measurement2.setValue(13);
        measurement2.setDateTime(cal2.getTime());
        measurement2.setStrategy(strategy);
    
        this.session.save(strategy);
        this.session.save(measurement1);
        this.session.save(measurement2);
        this.session.flush();
    
        Object measurements1 = lookupService.getMeasurementByMaxDate("Dummy", "Measurement", cal1.getTime());
    
        Assert.assertNull(measurements1);
    
        Object measurements2 = lookupService.getMeasurementByMaxDate("Strategy1", "Dummy", cal1.getTime());
    
        Assert.assertNull(measurements2);
    
        Calendar before = Calendar.getInstance();
        before.add(Calendar.HOUR_OF_DAY, -12);
    
        Object measurements3 = lookupService.getMeasurementByMaxDate("Strategy1", "Measurement", before.getTime());
    
        Assert.assertNull(measurements3);
    
        Object measurements4 = lookupService.getMeasurementByMaxDate("Strategy1", "Measurement", cal1.getTime());
    
        Assert.assertNotNull(measurements4);
        Assert.assertEquals(measurement1.getValue(), measurements4);
    
        Object measurements5 = lookupService.getMeasurementByMaxDate("Strategy1", "Measurement", cal2.getTime());
    
        Assert.assertNotNull(measurements5);
        Assert.assertEquals(measurement2.getValue(), measurements5);
    }

    @Test
    public void testGetMeasurementByMinDate() {
    
        Strategy strategy = new StrategyImpl();
        strategy.setName("Strategy1");
    
        Calendar cal1 = Calendar.getInstance();
    
        Measurement measurement1 = new MeasurementImpl();
        measurement1.setName("Measurement");
        measurement1.setValue(12);
        measurement1.setDateTime(cal1.getTime());
        measurement1.setStrategy(strategy);
    
        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.HOUR_OF_DAY, 1);
    
        Measurement measurement2 = new MeasurementImpl();
        measurement2.setName("Measurement");
        measurement2.setValue(13);
        measurement2.setDateTime(cal2.getTime());
        measurement2.setStrategy(strategy);
    
        this.session.save(strategy);
        this.session.save(measurement1);
        this.session.save(measurement2);
        this.session.flush();
    
        Object measurements1 = lookupService.getMeasurementByMinDate("Dummy", "Measurement", cal1.getTime());
    
        Assert.assertNull(measurements1);
    
        Object measurements2 = lookupService.getMeasurementByMinDate("Strategy1", "Dummy", cal1.getTime());
    
        Assert.assertNull(measurements2);
    
        Calendar after = Calendar.getInstance();
        after.add(Calendar.HOUR_OF_DAY, 2);
    
        Object measurements3 = lookupService.getMeasurementByMinDate("Strategy1", "Measurement", after.getTime());
    
        Assert.assertNull(measurements3);
    
        Object measurements4 = lookupService.getMeasurementByMinDate("Strategy1", "Measurement", cal1.getTime());
    
        Assert.assertNotNull(measurements4);
    
        Assert.assertSame(measurement1.getValue(), measurements4);
    
        Object measurements5 = lookupService.getMeasurementByMinDate("Strategy1", "Measurement", cal2.getTime());
    
        Assert.assertNotNull(measurements5);
    
        Assert.assertSame(measurement2.getValue(), measurements5);
    }

    @Test
    public void testGetEasyToBorrowByDateAndBroker() {
    
        SecurityFamily family = new SecurityFamilyImpl();
        family.setName("Forex1");
        family.setTickSizePattern("0<0.1");
        family.setCurrency(Currency.USD);
    
        Stock stock1 = new StockImpl();
        stock1.setSecurityFamily(family);
    
        Stock stock2 = new StockImpl();
        stock2.setSecurityFamily(family);
    
        EasyToBorrow easyToBorrow1 = new EasyToBorrowImpl();
        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        easyToBorrow1.setDate(cal1.getTime());
        easyToBorrow1.setBroker(Broker.CNX.name());
        easyToBorrow1.setStock(stock1);
    
        EasyToBorrow easyToBorrow2 = new EasyToBorrowImpl();
        easyToBorrow2.setDate(cal1.getTime());
        easyToBorrow2.setBroker(Broker.CNX.name());
        easyToBorrow2.setStock(stock2);
    
        this.session.save(family);
        this.session.save(stock1);
        this.session.save(stock2);
        this.session.save(easyToBorrow1);
        this.session.save(easyToBorrow2);
    
        this.session.flush();
    
        List<EasyToBorrow> easyToBorrows1 = (List<EasyToBorrow>) lookupService.getEasyToBorrowByDateAndBroker(cal1.getTime(), Broker.DC.name());
    
        Assert.assertEquals(0, easyToBorrows1.size());
    
        Calendar cal2 = Calendar.getInstance();
        cal2.set(Calendar.HOUR, 2);
    
        List<EasyToBorrow> easyToBorrows2 = (List<EasyToBorrow>) lookupService.getEasyToBorrowByDateAndBroker(cal2.getTime(), Broker.CNX.name());
    
        Assert.assertEquals(2, easyToBorrows2.size());
    
        List<EasyToBorrow> easyToBorrows3 = (List<EasyToBorrow>) lookupService.getEasyToBorrowByDateAndBroker(cal1.getTime(), Broker.CNX.name());
    
        Assert.assertEquals(2, easyToBorrows3.size());
    
        Assert.assertSame(easyToBorrow1, easyToBorrows3.get(0));
        Assert.assertEquals(Broker.CNX.name(), easyToBorrows3.get(0).getBroker());
        Assert.assertSame(stock1, easyToBorrows3.get(0).getStock());
        Assert.assertSame(easyToBorrow2, easyToBorrows3.get(1));
        Assert.assertEquals(Broker.CNX.name(), easyToBorrows3.get(1).getBroker());
        Assert.assertSame(stock2, easyToBorrows3.get(1).getStock());
    }

}
