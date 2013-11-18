/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.entity;

import org.junit.Before;
import org.junit.Test;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.AccountDao;
import ch.algotrader.enumeration.OrderServiceType;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class AccountTest extends EntityTest {

    private AccountDao accountDao;

    @Before
    public void before() {

        this.accountDao = ServiceLocator.instance().getService("accountDao", AccountDao.class);
    }

    @Test
    public void testFindByName() {

        this.accountDao.findByName("DC");
    }

    @Test
    public void testFindActiveSessionsByOrderServiceType() {

        this.accountDao.findActiveSessionsByOrderServiceType(OrderServiceType.DC_FIX);

    }
}
