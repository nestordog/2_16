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

import org.junit.Before;
import org.junit.Test;

import ch.algotrader.ServiceLocator;
import ch.algotrader.dbunit.AbstractDaoDbUnitTemplateTestCase;
import ch.algotrader.enumeration.OrderServiceType;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class AccountTest extends AbstractDaoDbUnitTemplateTestCase {

    private AccountDao accountDao;

    @Before
    public void before() {

        this.accountDao = ServiceLocator.instance().getService("accountDao", AccountDao.class);
    }

    @Test
    public void testFindByName() {

        this.accountDao.findByName("");
    }

    @Test
    public void testFindActiveSessionsByOrderServiceType() {

        this.accountDao.findActiveSessionsByOrderServiceType(OrderServiceType.IB_NATIVE);
    }
}
