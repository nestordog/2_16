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

package ch.algotrader.entity;

import java.io.IOException;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.dao.AccountDao;
import ch.algotrader.dao.AccountDaoImpl;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.hibernate.InMemoryDBTest;

/**
* Unit tests for {@link ch.algotrader.entity.Account}.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*
* @version $Revision$ $Date$
*/
public class AccountDaoTest extends InMemoryDBTest {

    private AccountDao dao;

    public AccountDaoTest() throws IOException {

        super();
    }

    @Override
    @Before
    public void setup() throws Exception {

        super.setup();

        this.dao = new AccountDaoImpl(this.sessionFactory);
    }

    @Test
    public void testFindByName() {

        Account account1 = this.dao.findByName("name1");

        Assert.assertNull(account1);

        Account account2 = new AccountImpl();

        account2.setName("name2");
        account2.setBroker(Broker.CNX);
        account2.setOrderServiceType(OrderServiceType.CNX_FIX);

        this.dao.save(account2);
        this.dao.flush();

        Account account3 = this.dao.findByName("name2");

        Assert.assertNotNull(account3);
        Assert.assertEquals("name2", account3.getName());
        Assert.assertEquals(Broker.CNX, account3.getBroker());
        Assert.assertEquals(OrderServiceType.CNX_FIX, account3.getOrderServiceType());
    }

}
