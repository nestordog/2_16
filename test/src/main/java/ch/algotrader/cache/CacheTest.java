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

import org.apache.commons.collections15.map.SingletonMap;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.collection.AbstractPersistentCollection;
import org.hibernate.proxy.HibernateProxy;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import ch.algotrader.ServiceLocator;
import ch.algotrader.adapter.ib.IBSession;
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
import ch.algotrader.enumeration.TransactionType;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.LookupUtil;
import ch.algotrader.service.CombinationService;
import ch.algotrader.service.MarketDataService;
import ch.algotrader.service.PositionService;
import ch.algotrader.service.PropertyService;
import ch.algotrader.service.TransactionService;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CacheTest {

    private static final String ACCOUNT_NAME = "TEST_ACCOUNT";
    private static final String PROPERTY_NAME = "TEST_PROPERTY";
    private static final String STRATEGY_NAME = "TEST_STRATEGY";

    private static PositionService positionService;
    private static MarketDataService marketDataService;
    private static CombinationService combinationService;
    private static PropertyService propertyService;
    private static TransactionService transactionService;
    private static CacheManager cache;
    private static SessionFactory sessionFactory;

    private static int securityFamilyId2; // NON_TRADEABLE
    private static int strategyId1;
    private static int securityId1; // EUR.USD
    private static int securityId2; // USD.CHF
    private static int securityId3; // NON_TRADEABLE

    @BeforeClass
    public static void setupClass() {

        System.setProperty("spring.profiles.active", "singleDataSource,iBMarketData,iBNative");
        System.setProperty("misc.embedded", "true");
        System.setProperty("logLevel", "trace");

        ServiceLocator serviceLocator = ServiceLocator.instance();
        serviceLocator.init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);

        positionService = serviceLocator.getPositionService();
        marketDataService = serviceLocator.getMarketDataService();
        combinationService = serviceLocator.getCombinationService();
        propertyService = serviceLocator.getPropertyService();
        transactionService = serviceLocator.getService("transactionService", TransactionService.class);
        cache = serviceLocator.getService("cacheManager", CacheManager.class);
        sessionFactory = serviceLocator.getService("sessionFactory", SessionFactory.class);

        Engine engine = serviceLocator.getService("serverEngine", Engine.class);
        engine.setInternalClock(true);
        engine.deployModule("combination");
        engine.deployModule("market-data");
        engine.deployModule("trades");

        serviceLocator.getService("iBSession", IBSession.class).init();

        // setup database
        Session session = SessionFactoryUtils.getNewSession(sessionFactory);

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
        securityFamilyId2 = (Integer) session.save(family2);

        Forex security1 = new ForexImpl();
        security1.setSymbol("EUR.USD");
        security1.setBaseCurrency(Currency.EUR);
        security1.setSecurityFamily(family1);
        securityId1 = (Integer) session.save(security1);

        Forex security2 = new ForexImpl();
        security2.setSymbol("GBP.USD");
        security2.setBaseCurrency(Currency.GBP);
        security2.setSecurityFamily(family1);
        security2.setUnderlying(security1);
        securityId2 = (Integer) session.save(security2);

        Index security3 = new IndexImpl();
        security3.setSymbol("NON-TRADEABLE");
        security3.setSecurityFamily(family2);
        security3.setUnderlying(security1);
        security3.setAssetClass(AssetClass.EQUITY);
        securityId3 = (Integer) session.save(security3);

        Strategy strategy1 = new StrategyImpl();
        strategy1.setName(STRATEGY_NAME);
        strategyId1 = (Integer) session.save(strategy1);

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setStrategy(strategy1);
        subscription1.setFeedType(FeedType.IB);
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
        account1.setBroker(Broker.IB);
        account1.setOrderServiceType(OrderServiceType.IB_NATIVE);
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

    @Test
    public void testSecurity() {

        Security security = LookupUtil.getSecurityInitialized(securityId2);
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

        transactionService.createTransaction(securityId2, STRATEGY_NAME, null, new Date(), 10000, new BigDecimal(1.0), null, null, null, Currency.USD, TransactionType.BUY, ACCOUNT_NAME, null);

        Position position1 = LookupUtil.getPositionBySecurityAndStrategy(securityId2, STRATEGY_NAME);

        Position position2 = cache.get(PositionImpl.class, position1.getId());
        Assert.assertNotNull(position2);
        Assert.assertEquals(position1, position2);
        Assert.assertSame(position1, position2);
    }

    @Test
    public void testSubscription() {

        Security security = cache.get(SecurityImpl.class, securityId1);
        Assert.assertEquals(0, security.getSubscriptions().size());

        marketDataService.subscribe(STRATEGY_NAME, securityId1);
        Assert.assertEquals(1, security.getSubscriptions().size());

        marketDataService.unsubscribe(STRATEGY_NAME, securityId1);
        Assert.assertEquals(0, security.getSubscriptions().size());
    }

    @Test
    public void testHQL() {

        Position position1 = LookupUtil.getPositionBySecurityAndStrategy(securityId2, STRATEGY_NAME);

        String queryString = "from PositionImpl as p join fetch p.security as s where s.id = :id";
        SingletonMap<String, Object> namedParameters = new SingletonMap<String, Object>("id", securityId2);

        Position position2 = (Position) cache.query(queryString, namedParameters).iterator().next();
        Assert.assertNotNull(position2);

        Position position3 = (Position) cache.query(queryString, namedParameters).iterator().next();
        Assert.assertNotNull(position2);
        Assert.assertEquals(position2, position3);
        Assert.assertSame(position2, position3);

        // exit Value
        Assert.assertNull(position1.getExitValue());

        BigDecimal exitValue = new BigDecimal(1);
        positionService.setExitValue(position1.getId(), exitValue, true);

        Position position5 = (Position) cache.query(queryString, namedParameters).iterator().next();
        Assert.assertTrue(position5.getExitValue().compareTo(exitValue) == 0);

    }

    @Test
    public void testNonTradeablePosition() {

        Security security = cache.get(SecurityImpl.class, securityId3);

        Assert.assertEquals(0, security.getPositions().size());

        int positionId = positionService.createNonTradeablePosition(STRATEGY_NAME, securityId3, 1000000).getId();
        Assert.assertEquals(1, security.getPositions().size());

        positionService.modifyNonTradeablePosition(positionId, 2000000);
        Assert.assertEquals(2000000, security.getPositions().iterator().next().getQuantity());

        positionService.deleteNonTradeablePosition(positionId, false);
        Assert.assertEquals(0, security.getPositions().size());
    }

    @Test
    public void testCombination() {

        // combination / component modification
        int combinationId = combinationService.createCombination(CombinationType.BUTTERFLY, securityFamilyId2).getId();
        Combination combination1 = (Combination) cache.get(SecurityImpl.class, combinationId);
        Assert.assertNotNull(combination1);

        Combination combination2 = combinationService.addComponentQuantity(combinationId, securityId1, 1000);
        Assert.assertEquals(1, combination1.getComponentCount());
        Assert.assertEquals(1000, combination1.getComponents().iterator().next().getQuantity());
        Assert.assertEquals(1, combination2.getComponentCount());
        Assert.assertEquals(1000, combination2.getComponents().iterator().next().getQuantity());

        Combination combination3 = combinationService.addComponentQuantity(combinationId, securityId2, 5000);
        Assert.assertEquals(2, combination1.getComponentCount());
        Assert.assertEquals(6000, combination1.getComponentTotalQuantity());
        Assert.assertEquals(2, combination3.getComponentCount());
        Assert.assertEquals(6000, combination3.getComponentTotalQuantity());

        Combination combination4 = combinationService.removeComponent(combinationId, securityId1);
        Assert.assertEquals(1, combination1.getComponentCount());
        Assert.assertEquals(1, combination4.getComponentCount());

        combinationService.deleteCombination(combinationId);
        Combination combination5 = (Combination) cache.get(SecurityImpl.class, combinationId);
        Assert.assertNull(combination5);
    }

    @Test
    public void testProperty() {

        // property
        Strategy strategy = cache.get(StrategyImpl.class, strategyId1);
        Assert.assertEquals(1, strategy.getProps().size());

        propertyService.addProperty(strategyId1, "test", 12, false);
        Assert.assertEquals(2, strategy.getProps().size());
        Assert.assertEquals(12, strategy.getIntProperty("test"));

        propertyService.removeProperty(strategyId1, "test");
        Assert.assertEquals(1, strategy.getProps().size());
    }
}
