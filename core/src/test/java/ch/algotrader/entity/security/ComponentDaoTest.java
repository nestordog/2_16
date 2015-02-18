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

/**
 * Unit tests for {@link ch.algotrader.entity.security.ComponentDaoImpl}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class ComponentDaoTest extends InMemoryDBTest {

    private ComponentDao dao;

    private SecurityFamily family1;

    private Forex forex1;

    private Strategy strategy1;

    public ComponentDaoTest() throws IOException {

        super();
    }

    @Override
    @Before
    public void setup() throws Exception {

        super.setup();

        this.dao = new ComponentDaoImpl(this.sessionFactory);

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
    public void testFindSubscribedByStrategyInclSecurity() {

        Combination combination1 = new CombinationImpl();
        combination1.setSecurityFamily(this.family1);
        combination1.setUuid("521ds5ds2d");
        combination1.setType(CombinationType.BUTTERFLY);

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM);
        subscription1.setSecurity(combination1);
        subscription1.setStrategy(this.strategy1);

        this.session.save(this.strategy1);
        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(combination1);
        this.session.save(subscription1);

        combination1.addSubscriptions(subscription1);

        Component component1 = new ComponentImpl();
        component1.setSecurity(this.forex1);
        component1.setCombination(combination1);

        this.session.save(component1);
        this.session.flush();

        List<Component> components1 = this.dao.findSubscribedByStrategyInclSecurity("Dummy");

        Assert.assertEquals(0, components1.size());

        List<Component> components2 = this.dao.findSubscribedByStrategyInclSecurity("Strategy1");

        Assert.assertEquals(1, components2.size());

        Assert.assertSame(combination1, components2.get(0).getCombination());
        Assert.assertEquals(1, components2.get(0).getCombination().getSubscriptions().size());
        Assert.assertSame(this.forex1, components2.get(0).getSecurity());
    }

    @Test
    public void testFindSubscribedBySecurityInclSecurity() {

        Combination combination1 = new CombinationImpl();
        combination1.setSecurityFamily(this.family1);
        combination1.setUuid("521ds5ds2d");
        combination1.setType(CombinationType.BUTTERFLY);

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM);
        subscription1.setSecurity(combination1);
        subscription1.setStrategy(this.strategy1);

        this.session.save(this.strategy1);
        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(combination1);
        this.session.save(subscription1);

        combination1.addSubscriptions(subscription1);

        Component component1 = new ComponentImpl();
        component1.setSecurity(this.forex1);
        component1.setCombination(combination1);

        this.session.save(component1);
        this.session.flush();

        List<Component> components1 = this.dao.findSubscribedBySecurityInclSecurity(0);

        Assert.assertEquals(0, components1.size());

        List<Component> components2 = this.dao.findSubscribedBySecurityInclSecurity(this.forex1.getId());

        Assert.assertEquals(1, components2.size());

        Assert.assertSame(combination1, components2.get(0).getCombination());
        Assert.assertEquals(1, components2.get(0).getCombination().getSubscriptions().size());
        Assert.assertSame(this.forex1, components2.get(0).getSecurity());
    }

    @Test
    public void testFindSubscribedByStrategyAndSecurityInclSecurity() {

        Combination combination1 = new CombinationImpl();
        combination1.setSecurityFamily(this.family1);
        combination1.setUuid("521ds5ds2d");
        combination1.setType(CombinationType.BUTTERFLY);

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM);
        subscription1.setSecurity(combination1);
        subscription1.setStrategy(this.strategy1);

        this.session.save(this.strategy1);
        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(combination1);
        this.session.save(subscription1);

        combination1.addSubscriptions(subscription1);

        Component component1 = new ComponentImpl();
        component1.setSecurity(this.forex1);
        component1.setCombination(combination1);

        this.session.save(component1);
        this.session.flush();

        List<Component> components1 = this.dao.findSubscribedByStrategyAndSecurityInclSecurity("Dummy", this.forex1.getId());

        Assert.assertEquals(0, components1.size());

        List<Component> components2 = this.dao.findSubscribedByStrategyAndSecurityInclSecurity("Strategy1", 0);

        Assert.assertEquals(0, components2.size());

        List<Component> components3 = this.dao.findSubscribedByStrategyAndSecurityInclSecurity("Strategy1", this.forex1.getId());

        Assert.assertEquals(1, components3.size());

        Assert.assertSame(combination1, components3.get(0).getCombination());
        Assert.assertEquals(1, components3.get(0).getCombination().getSubscriptions().size());
        Assert.assertSame(this.forex1, components3.get(0).getSecurity());
    }

    @Test
    public void testFindNonPersistent() {

        Combination combination1 = new CombinationImpl();
        combination1.setSecurityFamily(this.family1);
        combination1.setUuid("521ds5ds2d");
        combination1.setType(CombinationType.BUTTERFLY);

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM);
        subscription1.setSecurity(combination1);
        subscription1.setStrategy(this.strategy1);

        Combination combination2 = new CombinationImpl();
        combination2.setSecurityFamily(this.family1);
        combination2.setUuid("521ds5ds2d");
        combination2.setType(CombinationType.STRADDLE);

        Subscription subscription2 = new SubscriptionImpl();
        subscription2.setFeedType(FeedType.BB);
        subscription2.setSecurity(combination2);
        subscription2.setStrategy(this.strategy1);

        this.session.save(this.strategy1);
        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(combination1);
        this.session.save(subscription1);
        this.session.save(combination2);
        this.session.save(subscription2);

        combination1.addSubscriptions(subscription1);
        combination2.addSubscriptions(subscription2);

        Component component1 = new ComponentImpl();
        component1.setSecurity(this.forex1);
        component1.setCombination(combination1);

        Component component2 = new ComponentImpl();
        component2.setSecurity(this.forex1);
        component2.setCombination(combination2);


        this.session.save(component1);
        this.session.save(component2);

        this.session.flush();

        component1.setPersistent(true);
        component2.setPersistent(true);
        this.session.flush();

        List<Component> components1 = this.dao.findNonPersistent();

        Assert.assertEquals(0, components1.size());

        component1.setPersistent(false);
        component2.setPersistent(true);
        this.session.flush();

        List<Component> components2 = this.dao.findNonPersistent();

        Assert.assertEquals(1, components2.size());

        Assert.assertSame(component1, components2.get(0));

        component1.setPersistent(true);
        component2.setPersistent(false);
        this.session.flush();

        List<Component> components3 = this.dao.findNonPersistent();

        Assert.assertEquals(1, components3.size());

        Assert.assertSame(component2, components3.get(0));

        component1.setPersistent(false);
        component2.setPersistent(false);
        this.session.flush();

        List<Component> components4 = this.dao.findNonPersistent();

        Assert.assertEquals(2, components4.size());

        Assert.assertSame(combination1, components4.get(0).getCombination());
        Assert.assertSame(component1, components4.get(0));
        Assert.assertSame(combination2, components4.get(1).getCombination());
        Assert.assertSame(component2, components4.get(1));
    }

}
