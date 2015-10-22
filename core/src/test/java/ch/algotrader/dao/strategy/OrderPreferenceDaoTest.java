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
package ch.algotrader.dao.strategy;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.dao.trade.OrderPreferenceDao;
import ch.algotrader.dao.trade.OrderPreferenceDaoImpl;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.AccountImpl;
import ch.algotrader.entity.trade.OrderPreference;
import ch.algotrader.entity.trade.OrderPreferenceImpl;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.enumeration.OrderType;
import ch.algotrader.hibernate.InMemoryDBTest;

/**
* Unit tests for {@link OrderPreferenceDaoImpl}.
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
        this.session.clear();

        OrderPreference orderPreference2 = this.dao.findByName("Dummy");

        Assert.assertNull(orderPreference2);

        OrderPreference orderPreference3 = this.dao.findByName("Primary");

        Assert.assertNotNull(orderPreference3);

        Assert.assertEquals(orderPreference1, orderPreference3);
    }

    @Test
    public void testFindByNameDefaultAccount() {

        Account account1 = new AccountImpl();
        account1.setName("ACC1");
        account1.setBroker(Broker.IB.name());
        account1.setOrderServiceType(OrderServiceType.IB_NATIVE.name());
        this.session.save(account1);

        OrderPreference orderPreference1 = new OrderPreferenceImpl();
        orderPreference1.setName("Primary");
        orderPreference1.setOrderType(OrderType.DISTRIBUTIONAL);
        orderPreference1.setDefaultAccount(account1);

        this.session.save(orderPreference1);
        this.session.flush();
        this.session.clear();

        OrderPreference orderPreference2 = this.dao.findByName("Primary");

        Assert.assertNotNull(orderPreference2);
        Assert.assertEquals(orderPreference1, orderPreference2);

        this.session.close();

        Account account2 = orderPreference2.getDefaultAccount();
        Assert.assertEquals(account1, account2);
    }

}
