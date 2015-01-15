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
package ch.algotrader.entity.strategy;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.OrderType;
import ch.algotrader.hibernate.InMemoryDBTest;

/**
* Unit tests for {@link ch.algotrader.entity.strategy.DefaultOrderPreferenceDaoImpl}.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*
* @version $Revision$ $Date$
*/
public class DefaultOrderPreferenceDaoTest extends InMemoryDBTest {

    private DefaultOrderPreferenceDao dao;

    public DefaultOrderPreferenceDaoTest() throws IOException {

        super();
    }

    @Override
    @Before
    public void setup() throws Exception {

        super.setup();
        this.dao = new DefaultOrderPreferenceDaoImpl(this.sessionFactory);
    }

    @Test
    public void testFindByStrategyAndSecurityFamilyInclOrderPreference() {

        OrderPreference orderPreference = new OrderPreferenceImpl();
        orderPreference.setName("Primary");
        orderPreference.setOrderType(OrderType.DISTRIBUTIONAL);

        SecurityFamily family = new SecurityFamilyImpl();
        family.setName("Forex1");
        family.setTickSizePattern("0<0.1");
        family.setCurrency(Currency.USD);

        Strategy strategy = new StrategyImpl();
        strategy.setName("Strategy1");

        DefaultOrderPreference defaultOrderPreference = new DefaultOrderPreferenceImpl();
        defaultOrderPreference.setOrderPreference(orderPreference);
        defaultOrderPreference.setSecurityFamily(family);
        defaultOrderPreference.setStrategy(strategy);

        this.session.save(orderPreference);
        this.session.save(family);
        this.session.save(strategy);
        this.session.save(defaultOrderPreference);
        this.session.flush();

        DefaultOrderPreference defaultOrderPreference1 = this.dao.findByStrategyAndSecurityFamilyInclOrderPreference("Dummy", family.getId());

        Assert.assertNull(defaultOrderPreference1);

        DefaultOrderPreference defaultOrderPreference2 = this.dao.findByStrategyAndSecurityFamilyInclOrderPreference("Dummy", 0);

        Assert.assertNull(defaultOrderPreference2);

        DefaultOrderPreference defaultOrderPreference3 = this.dao.findByStrategyAndSecurityFamilyInclOrderPreference("Strategy1", family.getId());

        Assert.assertNotNull(defaultOrderPreference3);
        Assert.assertSame(orderPreference, defaultOrderPreference3.getOrderPreference());
        Assert.assertSame(family, defaultOrderPreference3.getSecurityFamily());
        Assert.assertSame(strategy, defaultOrderPreference3.getStrategy());
    }

}
