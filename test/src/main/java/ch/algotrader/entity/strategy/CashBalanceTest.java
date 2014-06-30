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

import org.junit.Before;
import org.junit.Test;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.EntityTest;

/**
 * @author <a href="mailto:amasood@algotrader.ch">Ahmad Masood</a>
 *
 * @version $Revision$ $Date$
 */
public class CashBalanceTest extends EntityTest {

    private CashBalanceDao cashBalanceDao;

    @Before
    public void before() {

        this.cashBalanceDao = ServiceLocator.instance().getService("cashBalanceDao", CashBalanceDao.class);
    }

    @Test
    public void testFindindByStrategyAndCurrency() {

        this.cashBalanceDao.findByStrategyAndCurrency(null, null);
    }

    @Test
    public void testFindindByStrategyAndCurrencyLocked() {

        this.cashBalanceDao.findByStrategyAndCurrencyLocked(null, null);
    }

    @Test
    public void testFindCashBalancesByStrategy() {

        this.cashBalanceDao.findCashBalancesByStrategy(null);
    }

    @Test
    public void testFindHeldCurrencies() {

        this.cashBalanceDao.findHeldCurrencies();
    }

    @Test
    public void testFindHeldCurrenciesByStrategy() {

        this.cashBalanceDao.findHeldCurrenciesByStrategy(null);
    }

}
