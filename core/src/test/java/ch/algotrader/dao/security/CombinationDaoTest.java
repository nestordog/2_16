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
package ch.algotrader.dao.security;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.SubscriptionImpl;
import ch.algotrader.entity.security.Combination;
import ch.algotrader.entity.security.CombinationImpl;
import ch.algotrader.entity.security.Component;
import ch.algotrader.entity.security.ComponentImpl;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.enumeration.CombinationType;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.hibernate.InMemoryDBTest;

/**
* Unit tests for {@link CombinationDaoImpl}.
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

        Component component1 = new ComponentImpl();
        component1.setSecurity(this.forex1);
        component1.setQuantity(12L);

        Combination combination1 = new CombinationImpl();
        combination1.setSecurityFamily(this.family1);
        combination1.setUuid(UUID.randomUUID().toString());
        combination1.setType(CombinationType.STRADDLE);
        combination1.addComponents(component1);

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM.name());
        subscription1.setSecurity(combination1);
        subscription1.setStrategy(this.strategy1);

        this.session.save(this.strategy1);
        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(combination1);
        this.session.save(subscription1);

        this.session.flush();

        List<Combination> combinations1 = this.dao.findSubscribedByStrategy("Strategy1");

        Assert.assertEquals(1, combinations1.size());

        Assert.assertSame(combination1, combinations1.get(0));
    }

    @Test
    public void testFindSubscribedByStrategyAndUnderlying() {

        Component component1 = new ComponentImpl();
        component1.setSecurity(this.forex1);
        component1.setQuantity(12L);

        Combination combination1 = new CombinationImpl();
        combination1.setSecurityFamily(this.family1);
        combination1.setUuid(UUID.randomUUID().toString());
        combination1.setType(CombinationType.STRADDLE);
        combination1.addComponents(component1);
        combination1.setUnderlying(this.forex1);

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM.name());
        subscription1.setSecurity(combination1);
        subscription1.setStrategy(this.strategy1);

        this.session.save(this.strategy1);
        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(combination1);
        this.session.save(subscription1);

        this.session.flush();

        List<Combination> combinations1 = this.dao.findSubscribedByStrategyAndUnderlying("Strategy1", 0);

        Assert.assertEquals(0, combinations1.size());

        List<Combination> combinations2 = this.dao.findSubscribedByStrategyAndUnderlying("Strategy1", this.forex1.getId());

        Assert.assertEquals(1, combinations2.size());

        Assert.assertSame(combination1, combinations2.get(0));
    }

    @Test
    public void testFindSubscribedByStrategyAndComponent() {

        Component component1 = new ComponentImpl();
        component1.setSecurity(this.forex1);
        component1.setQuantity(12L);

        Combination combination1 = new CombinationImpl();
        combination1.setSecurityFamily(this.family1);
        combination1.setUuid(UUID.randomUUID().toString());
        combination1.setType(CombinationType.STRADDLE);

        combination1.addComponents(component1);

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM.name());
        subscription1.setSecurity(combination1);
        subscription1.setStrategy(this.strategy1);

        this.session.save(this.strategy1);
        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(combination1);
        this.session.save(subscription1);

        component1.setCombination(combination1);

        this.session.save(component1);

        this.session.flush();

        List<Combination> combinations1 = this.dao.findSubscribedByStrategyAndComponent("Strategy1", 0);

        Assert.assertEquals(0, combinations1.size());

        List<Combination> combinations2 = this.dao.findSubscribedByStrategyAndComponent("Strategy1", this.forex1.getId());

        Assert.assertEquals(1, combinations2.size());

        Assert.assertSame(combination1, combinations2.get(0));
    }

    @Test
    public void testFindSubscribedByStrategyAndComponentType() {

        Component component1 = new ComponentImpl();
        component1.setSecurity(this.forex1);
        component1.setQuantity(12L);

        Combination combination1 = new CombinationImpl();
        combination1.setSecurityFamily(this.family1);
        combination1.setUuid(UUID.randomUUID().toString());
        combination1.setType(CombinationType.STRADDLE);

        combination1.addComponents(component1);

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM.name());
        subscription1.setSecurity(combination1);
        subscription1.setStrategy(this.strategy1);

        this.session.save(this.strategy1);
        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(combination1);
        this.session.save(subscription1);

        component1.setCombination(combination1);

        this.session.save(component1);

        this.session.flush();

        List<Combination> combinations1 = this.dao.findSubscribedByStrategyAndComponentType("Strategy1", Security.class);

        Assert.assertEquals(0, combinations1.size());

        List<Combination> combinations2 = this.dao.findSubscribedByStrategyAndComponentType("Strategy1", ForexImpl.class);

        Assert.assertEquals(1, combinations2.size());

        Assert.assertSame(combination1, combinations2.get(0));
    }

    @Test
    public void tesFindSubscribedByStrategyAndComponentTypeWithZeroQty() {

        Component component1 = new ComponentImpl();
        component1.setSecurity(this.forex1);
        component1.setQuantity(12L);

        Combination combination1 = new CombinationImpl();
        combination1.setSecurityFamily(this.family1);
        combination1.setUuid(UUID.randomUUID().toString());
        combination1.setType(CombinationType.STRADDLE);

        combination1.addComponents(component1);

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM.name());
        subscription1.setSecurity(combination1);
        subscription1.setStrategy(this.strategy1);

        this.session.save(this.strategy1);
        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(combination1);
        this.session.save(subscription1);

        component1.setCombination(combination1);

        this.session.save(component1);

        this.session.flush();

        List<Combination> combinations1 = this.dao.findSubscribedByStrategyAndComponentTypeWithZeroQty("Strategy1", Security.class);

        Assert.assertEquals(0, combinations1.size());

        component1.setQuantity(0);

        this.session.flush();

        List<Combination> combinations2 = this.dao.findSubscribedByStrategyAndComponentTypeWithZeroQty("Strategy1", ForexImpl.class);

        Assert.assertEquals(1, combinations2.size());

        Assert.assertSame(combination1, combinations2.get(0));
    }

    @Test
    public void testFindNonPersistent() {

        Component component1 = new ComponentImpl();
        component1.setSecurity(this.forex1);
        component1.setQuantity(12L);

        Combination combination1 = new CombinationImpl();
        combination1.setSecurityFamily(this.family1);
        combination1.setUuid(UUID.randomUUID().toString());
        combination1.setType(CombinationType.STRADDLE);
        combination1.addComponents(component1);
        combination1.setPersistent(false);

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM.name());
        subscription1.setSecurity(combination1);
        subscription1.setStrategy(this.strategy1);

        Component component2 = new ComponentImpl();
        component2.setSecurity(this.forex1);
        component2.setQuantity(11L);

        Combination combination2 = new CombinationImpl();
        combination2.setSecurityFamily(this.family1);
        combination2.setUuid(UUID.randomUUID().toString());
        combination2.setType(CombinationType.STRANGLE);
        combination2.addComponents(component2);
        combination2.setPersistent(true);

        Subscription subscription2 = new SubscriptionImpl();
        subscription2.setFeedType(FeedType.BB.name());
        subscription2.setSecurity(combination2);
        subscription2.setStrategy(this.strategy1);

        this.session.save(this.strategy1);
        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(combination1);
        this.session.save(subscription1);
        this.session.save(combination2);
        this.session.save(subscription2);

        this.session.flush();

        List<Combination> combinations2 = this.dao.findNonPersistent();

        Assert.assertEquals(1, combinations2.size());

        Assert.assertSame(combination1, combinations2.get(0));

        combination2.setPersistent(false);

        this.session.flush();

        List<Combination> combinations3 = this.dao.findNonPersistent();

        Assert.assertEquals(2, combinations3.size());

        Assert.assertSame(combination1, combinations3.get(0));
        Assert.assertSame(combination2, combinations3.get(1));
    }

}
