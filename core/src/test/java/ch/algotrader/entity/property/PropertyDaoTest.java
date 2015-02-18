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
package ch.algotrader.entity.property;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.SubscriptionImpl;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.hibernate.InMemoryDBTest;

/**
* Unit tests for {@link ch.algotrader.entity.property.PropertyDaoImpl}.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*
* @version $Revision$ $Date$
*/
public class PropertyDaoTest extends InMemoryDBTest {

    private PropertyDao dao;

    public PropertyDaoTest() throws IOException {

        super();
    }

    @Override
    @Before
    public void setup() throws Exception {

        super.setup();

        this.dao = new PropertyDaoImpl(this.sessionFactory);
    }

    @Test
    public void testFindNonPersistent() {

        SecurityFamily family = new SecurityFamilyImpl();

        family.setName("Forex1");
        family.setTickSizePattern("0<0.1");
        family.setCurrency(Currency.USD);

        Forex forex = new ForexImpl();

        forex.setSymbol("EUR.USD");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        Strategy strategy = new StrategyImpl();
        strategy.setName("Strategy1");

        Subscription subscription = new SubscriptionImpl();
        subscription.setFeedType(FeedType.SIM);
        subscription.setSecurity(forex);
        subscription.setStrategy(strategy);

        Property property1 = new PropertyImpl();
        property1.setName("Property1");
        property1.setPropertyHolder(subscription);
        property1.setPersistent(true);

        Property property2 = new PropertyImpl();
        property2.setName("Property2");
        property2.setPropertyHolder(subscription);
        property2.setPersistent(true);

        this.session.save(family);
        this.session.save(forex);
        this.session.save(strategy);
        this.session.save(subscription);
        this.session.save(property1);
        this.session.save(property2);

        this.session.flush();

        List<Property> properties1 = this.dao.findNonPersistent();
        Assert.assertEquals(0, properties1.size());

        property1.setPersistent(false);
        this.session.flush();

        List<Property> properties2 = this.dao.findNonPersistent();

        Assert.assertEquals(1, properties2.size());

        Assert.assertSame(property1, properties2.get(0));

        property2.setPersistent(false);
        this.session.flush();

        List<Property> properties3 = this.dao.findNonPersistent();

        Assert.assertEquals(2, properties3.size());

        Assert.assertEquals("Property1", properties3.get(0).getName());
        Assert.assertSame(property1, properties3.get(0));
        Assert.assertEquals("Property2", properties3.get(1).getName());
        Assert.assertSame(property2, properties3.get(1));
    }

}
