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

import ch.algotrader.enumeration.OrderType;
import ch.algotrader.hibernate.InMemoryDBTest;

/**
* Unit tests for {@link ch.algotrader.entity.strategy.OrderPreferenceDaoImpl}.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*
* @version $Revision$ $Date$
*/
public class OrderPreferenceDaoTest extends InMemoryDBTest {

    private OrderPreferenceDao dao;

    public OrderPreferenceDaoTest() throws IOException {

        super();
    }

    @Override
    @Before
    public void setup() throws Exception {

        super.setup();
        this.dao = new OrderPreferenceDaoImpl(this.sessionFactory);
    }

    @Test
    public void testFindByName() {

        OrderPreference orderPreference1 = new OrderPreferenceImpl();
        orderPreference1.setName("Primary");
        orderPreference1.setOrderType(OrderType.DISTRIBUTIONAL);

        this.session.save(orderPreference1);
        this.session.flush();

        OrderPreference orderPreference2 = this.dao.findByName("Dummy");

        Assert.assertNull(orderPreference2);

        OrderPreference orderPreference3 = this.dao.findByName("Primary");

        Assert.assertNotNull(orderPreference3);
        Assert.assertSame(orderPreference1, orderPreference3);
    }

}
