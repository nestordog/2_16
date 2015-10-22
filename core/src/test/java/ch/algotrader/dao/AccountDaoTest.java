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

package ch.algotrader.dao;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.entity.Account;
import ch.algotrader.entity.AccountImpl;
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
        account2.setBroker(Broker.CNX.name());
        account2.setOrderServiceType(OrderServiceType.CNX_FIX.name());

        this.dao.save(account2);
        this.dao.flush();

        Account account3 = this.dao.findByName("name2");

        Assert.assertNotNull(account3);
        Assert.assertEquals("name2", account3.getName());
        Assert.assertEquals(Broker.CNX.name(), account3.getBroker());
        Assert.assertEquals(OrderServiceType.CNX_FIX.name(), account3.getOrderServiceType());
    }

    @Test
    public void testFindByOrderServiceType() {

        List<Account> accounts1 = this.dao.findByByOrderServiceType(OrderServiceType.CNX_FIX.name());
        Assert.assertNotNull(accounts1);
        Assert.assertEquals(0, accounts1.size());

        Account account1 = new AccountImpl();

        account1.setName("name2");
        account1.setBroker(Broker.CNX.name());
        account1.setOrderServiceType(OrderServiceType.CNX_FIX.name());

        this.dao.save(account1);
        this.dao.flush();

        List<Account> accounts2 = this.dao.findByByOrderServiceType(OrderServiceType.LMAX_FIX.name());
        Assert.assertNotNull(accounts2);
        Assert.assertEquals(0, accounts2.size());

        List<Account> accounts3 = this.dao.findByByOrderServiceType(OrderServiceType.CNX_FIX.name());
        Assert.assertNotNull(accounts3);
        Assert.assertEquals(1, accounts3.size());

        Account account2 = accounts3.get(0);
        Assert.assertNotNull(account2);
        Assert.assertEquals("name2", account2.getName());
        Assert.assertEquals(Broker.CNX.name(), account2.getBroker());
        Assert.assertEquals(OrderServiceType.CNX_FIX.name(), account2.getOrderServiceType());
    }

    @Test
    public void testFindByExtAccount() {

        Account account1 = this.dao.findByExtAccount("blah");

        Assert.assertNull(account1);

        Account account2 = new AccountImpl();

        account2.setName("name");
        account2.setExtAccount("some-ext-id");
        account2.setBroker(Broker.TT.name());
        account2.setOrderServiceType(OrderServiceType.TT_FIX.name());

        this.dao.save(account2);
        this.dao.flush();

        Account account3 = this.dao.findByExtAccount("some-ext-id");

        Assert.assertNotNull(account3);
        Assert.assertEquals("name", account3.getName());
        Assert.assertEquals(Broker.TT.name(), account3.getBroker());
        Assert.assertEquals(OrderServiceType.TT_FIX.name(), account3.getOrderServiceType());
    }

}
