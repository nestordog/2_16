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
package ch.algotrader.entity.security;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.SubscriptionImpl;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.enumeration.CombinationType;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.hibernate.InMemoryDBTest;
import ch.algotrader.util.HibernateUtil;

/**
* Unit tests for {@link ch.algotrader.entity.security.CombinationDaoImpl}.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*
* @version $Revision$ $Date$
*/
public class CombinationDaoTest extends InMemoryDBTest {

    private CombinationDao dao;

    private SecurityFamily family1;

    private Forex forex1;

    private Strategy strategy1;

    public CombinationDaoTest() throws IOException {

        super();
    }

    @Override
    @Before
    public void setup() throws Exception {

        super.setup();

        this.dao = new CombinationDaoImpl(this.sessionFactory);

        this.family1 = new SecurityFamilyImpl();
        this.family1.setName("Forex1");
        this.family1.setTickSizePattern("0<0.1");
        this.family1.setCurrency(Currency.USD);

        this.forex1 = new ForexImpl();
        this.forex1.setSymbol("EUR.USD");
        this.forex1.setBaseCurrency(Currency.EUR);
        this.forex1.setSecurityFamily(this.family1);

        this.strategy1 = new StrategyImpl();
        this.strategy1.setName("Strategy1");
    }

    @Test
    public void testFindSubscribedByStrategy() {

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM);
        subscription1.setSecurity(this.forex1);
        subscription1.setStrategy(this.strategy1);

        Subscription subscription2 = new SubscriptionImpl();
        subscription2.setFeedType(FeedType.BB);
        subscription2.setSecurity(this.forex1);
        subscription2.setStrategy(this.strategy1);

        this.session.save(this.strategy1);
        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(subscription1);
        this.session.save(subscription2);
        this.session.flush();

        Combination combination = new CombinationImpl();
        combination.setSecurityFamily(this.family1);
        combination.setUuid(UUID.randomUUID().toString());
        combination.setType(CombinationType.STRADDLE);
        ComponentImpl component1 = new ComponentImpl();
        component1.setSecurity(this.forex1);
        component1.setQuantity(12L);
        combination.addComponents(component1);
        combination.addSubscriptions(subscription1);
        combination.addSubscriptions(subscription2);

        this.session.save(combination);
        this.session.flush();

        List<Combination> combinations = this.dao.findSubscribedByStrategy("Strategy1");

        Assert.assertEquals(1, combinations.size());
        Assert.assertEquals(2, combinations.get(0).getSubscriptions().size());

        Assert.assertSame(combination, combinations.get(0));
    }

    @Test
    public void testFindSubscribedByStrategyAndUnderlying() {

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM);
        subscription1.setSecurity(this.forex1);
        subscription1.setStrategy(this.strategy1);

        this.session.save(this.strategy1);
        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(subscription1);
        this.session.flush();

        Combination combination = new CombinationImpl();
        combination.setSecurityFamily(this.family1);
        combination.setUuid(UUID.randomUUID().toString());
        combination.setType(CombinationType.STRADDLE);
        ComponentImpl component1 = new ComponentImpl();
        component1.setSecurity(this.forex1);
        component1.setQuantity(12L);
        combination.addComponents(component1);
        combination.addSubscriptions(subscription1);
        combination.setUnderlying(this.forex1);
        this.session.save(combination);
        this.session.flush();

        List<Combination> combinations1 = this.dao.findSubscribedByStrategyAndUnderlying("Strategy1", 0);

        Assert.assertEquals(0, combinations1.size());

        List<Combination> combinations2 = this.dao.findSubscribedByStrategyAndUnderlying("Strategy1", this.forex1.getId());

        Assert.assertEquals(1, combinations2.size());
        Assert.assertEquals(1, combinations2.get(0).getSubscriptions().size());

        Assert.assertSame(combination, combinations2.get(0));
    }

    @Test
    public void testFindSubscribedByStrategyAndComponent() {

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM);
        subscription1.setSecurity(this.forex1);
        subscription1.setStrategy(this.strategy1);

        this.session.save(this.strategy1);
        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(subscription1);
        this.session.flush();

        Component component1 = new ComponentImpl();
        component1.setSecurity(this.forex1);
        component1.setQuantity(12L);

        Combination combination1 = new CombinationImpl();
        combination1.setSecurityFamily(this.family1);
        combination1.setUuid(UUID.randomUUID().toString());
        combination1.setType(CombinationType.STRADDLE);
        combination1.addComponents(component1);
        combination1.addSubscriptions(subscription1);
        combination1.setUnderlying(this.forex1);

        this.session.save(combination1);
        this.session.flush();

        Combination combination2 = new CombinationImpl();
        combination2.setSecurityFamily(this.family1);
        combination2.setUuid(UUID.randomUUID().toString());
        combination2.setType(CombinationType.STRADDLE);
        combination2.addSubscriptions(subscription1);
        combination2.setUnderlying(this.forex1);
        this.session.save(combination2);
        this.session.flush();

        component1.setCombination(combination2);

        this.session.save(component1);
        this.session.flush();

        List<Combination> combinations1 = this.dao.findSubscribedByStrategyAndComponent("Strategy1", 0);

        Assert.assertEquals(0, combinations1.size());

        List<Combination> combinations2 = this.dao.findSubscribedByStrategyAndComponent("Strategy1", this.forex1.getId());

        Assert.assertEquals(1, combinations2.size());
        Assert.assertEquals(1, combinations2.get(0).getSubscriptions().size());

        Assert.assertSame(combination2, combinations2.get(0));
    }

    @Test
    public void testFindSubscribedByStrategyAndComponentType() {

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM);
        subscription1.setSecurity(this.forex1);
        subscription1.setStrategy(this.strategy1);

        this.session.save(this.strategy1);
        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(subscription1);
        this.session.flush();

        Component component1 = new ComponentImpl();
        component1.setSecurity(this.forex1);
        component1.setQuantity(12L);

        Combination combination1 = new CombinationImpl();
        combination1.setSecurityFamily(this.family1);
        combination1.setUuid(UUID.randomUUID().toString());
        combination1.setType(CombinationType.STRADDLE);
        combination1.addComponents(component1);
        combination1.addSubscriptions(subscription1);
        combination1.setUnderlying(this.forex1);

        this.session.save(combination1);
        this.session.flush();

        Combination combination2 = new CombinationImpl();
        combination2.setSecurityFamily(this.family1);
        combination2.setUuid(UUID.randomUUID().toString());
        combination2.setType(CombinationType.STRADDLE);
        combination2.addSubscriptions(subscription1);
        combination2.setUnderlying(this.forex1);
        this.session.save(combination2);
        this.session.flush();

        component1.setCombination(combination2);

        this.session.save(component1);
        this.session.flush();

        int discriminator1 = HibernateUtil.getDisriminatorValue(this.sessionFactory, Security.class);

        List<Combination> combinations1 = this.dao.findSubscribedByStrategyAndComponentType("Strategy1", discriminator1);

        Assert.assertEquals(0, combinations1.size());

        int discriminator2 = HibernateUtil.getDisriminatorValue(this.sessionFactory, ForexImpl.class);

        List<Combination> combinations2 = this.dao.findSubscribedByStrategyAndComponentType("Strategy1", discriminator2);

        Assert.assertEquals(1, combinations2.size());
        Assert.assertEquals(1, combinations2.get(0).getSubscriptions().size());

        Assert.assertSame(combination2, combinations2.get(0));
    }

    @Test
    public void tesFindSubscribedByStrategyAndComponentTypeWithZeroQty() {

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM);
        subscription1.setSecurity(this.forex1);
        subscription1.setStrategy(this.strategy1);

        this.session.save(this.strategy1);
        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(subscription1);
        this.session.flush();

        Component component1 = new ComponentImpl();
        component1.setSecurity(this.forex1);
        component1.setQuantity(12L);

        Combination combination1 = new CombinationImpl();
        combination1.setSecurityFamily(this.family1);
        combination1.setUuid(UUID.randomUUID().toString());
        combination1.setType(CombinationType.STRADDLE);
        combination1.addComponents(component1);
        combination1.addSubscriptions(subscription1);
        combination1.setUnderlying(this.forex1);

        this.session.save(combination1);
        this.session.flush();

        Combination combination2 = new CombinationImpl();
        combination2.setSecurityFamily(this.family1);
        combination2.setUuid(UUID.randomUUID().toString());
        combination2.setType(CombinationType.STRADDLE);
        combination2.addSubscriptions(subscription1);
        combination2.setUnderlying(this.forex1);
        this.session.save(combination2);
        this.session.flush();

        component1.setCombination(combination2);

        this.session.save(component1);
        this.session.flush();

        int discriminator1 = HibernateUtil.getDisriminatorValue(this.sessionFactory, Security.class);

        List<Combination> combinations1 = this.dao.findSubscribedByStrategyAndComponentTypeWithZeroQty("Strategy1", discriminator1);

        Assert.assertEquals(0, combinations1.size());

        int discriminator2 = HibernateUtil.getDisriminatorValue(this.sessionFactory, ForexImpl.class);

        component1.setQuantity(0);
        this.session.flush();

        List<Combination> combinations2 = this.dao.findSubscribedByStrategyAndComponentTypeWithZeroQty("Strategy1", discriminator2);

        Assert.assertEquals(1, combinations2.size());
        Assert.assertEquals(1, combinations2.get(0).getSubscriptions().size());

        Assert.assertSame(combination2, combinations2.get(0));
    }

    @Test
    public void testFindNonPersistent() {

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM);
        subscription1.setSecurity(this.forex1);
        subscription1.setStrategy(this.strategy1);

        this.session.save(this.strategy1);
        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(subscription1);
        this.session.flush();

        Component component1 = new ComponentImpl();
        component1.setSecurity(this.forex1);
        component1.setQuantity(12L);

        Combination combination1 = new CombinationImpl();
        combination1.setSecurityFamily(this.family1);
        combination1.setUuid(UUID.randomUUID().toString());
        combination1.setType(CombinationType.STRADDLE);
        combination1.addComponents(component1);
        combination1.addSubscriptions(subscription1);
        combination1.setUnderlying(this.forex1);
        combination1.setPersistent(true);

        Component component2 = new ComponentImpl();
        component2.setSecurity(this.forex1);
        component2.setQuantity(12L);

        Combination combination2 = new CombinationImpl();
        combination2.setSecurityFamily(this.family1);
        combination2.setUuid(UUID.randomUUID().toString());
        combination2.setType(CombinationType.STRADDLE);
        combination1.addComponents(component2);
        combination2.addSubscriptions(subscription1);
        combination2.setUnderlying(this.forex1);
        combination2.setPersistent(true);

        this.session.save(combination1);
        this.session.save(combination2);
        this.session.flush();

        List<Combination> combinations1 = this.dao.findNonPersistent();

        Assert.assertEquals(0, combinations1.size());

        combination1.setPersistent(false);
        this.session.flush();

        List<Combination> combinations2 = this.dao.findNonPersistent();

        Assert.assertEquals(1, combinations2.size());
        Assert.assertEquals(1, combinations2.get(0).getSubscriptions().size());
        Assert.assertSame(combination1, combinations2.get(0));

        combination2.setPersistent(false);
        this.session.flush();

        List<Combination> combinations3 = this.dao.findNonPersistent();

        Assert.assertEquals(2, combinations3.size());
        Assert.assertEquals(1, combinations3.get(0).getSubscriptions().size());
        Assert.assertSame(combination1, combinations3.get(0));
        Assert.assertSame(combination2, combinations3.get(1));
    }

}
