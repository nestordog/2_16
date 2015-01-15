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
import java.math.BigDecimal;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.enumeration.Currency;
import ch.algotrader.hibernate.InMemoryDBTest;

/**
 * Unit tests for {@link ch.algotrader.entity.strategy.CashBalanceDaoImpl}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class CashBalanceDaoTest extends InMemoryDBTest {

    private CashBalanceDao dao;

    private Strategy strategy1;

    private Strategy strategy2;

    public CashBalanceDaoTest() throws IOException {

        super();
    }

    @Override
    @Before
    public void setup() throws Exception {

        super.setup();
        this.dao = new CashBalanceDaoImpl(this.sessionFactory);

        this.strategy1 = new StrategyImpl();
        this.strategy1.setName("Strategy1");

        this.strategy2 = new StrategyImpl();
        this.strategy2.setName("Strategy2");
    }

    @Test
    public void testFindCashBalancesByStrategy() {

        CashBalance cashBalance1 = new CashBalanceImpl();
        cashBalance1.setCurrency(Currency.USD);
        cashBalance1.setAmount(new BigDecimal(111));
        cashBalance1.setStrategy(this.strategy1);

        CashBalance cashBalance2 = new CashBalanceImpl();
        cashBalance2.setCurrency(Currency.AUD);
        cashBalance2.setAmount(new BigDecimal(222));
        cashBalance2.setStrategy(this.strategy2);

        this.session.save(this.strategy1);
        this.session.save(this.strategy2);
        this.session.save(cashBalance1);
        this.session.save(cashBalance2);
        this.session.flush();

        List<CashBalance> cashBalances1 = this.dao.findCashBalancesByStrategy("NOT_FOUND");

        Assert.assertEquals(0, cashBalances1.size());

        List<CashBalance> cashBalances2 = this.dao.findCashBalancesByStrategy("Strategy1");

        Assert.assertEquals(1, cashBalances2.size());
        Assert.assertSame(cashBalance1.getStrategy(), cashBalances2.get(0).getStrategy());
        Assert.assertSame(cashBalance1, cashBalances2.get(0));

        cashBalance2.setStrategy(this.strategy1);
        this.session.flush();

        List<CashBalance> cashBalances3 = this.dao.findCashBalancesByStrategy("Strategy1");

        Assert.assertEquals(2, cashBalances3.size());
        Assert.assertSame(cashBalance1.getStrategy(), cashBalances3.get(0).getStrategy());
        Assert.assertSame(cashBalance1, cashBalances3.get(0));
        Assert.assertSame(cashBalance2.getStrategy(), cashBalances3.get(1).getStrategy());
        Assert.assertSame(cashBalance2, cashBalances3.get(1));
    }

    @Test
    public void testFindByStrategyAndCurrency() {

        CashBalance cashBalance1 = new CashBalanceImpl();
        cashBalance1.setCurrency(Currency.USD);
        cashBalance1.setAmount(new BigDecimal(111));
        cashBalance1.setStrategy(this.strategy1);

        this.session.save(this.strategy1);
        this.session.save(this.strategy2);
        this.session.save(cashBalance1);
        this.session.flush();

        CashBalance cashBalance3 = this.dao.findByStrategyAndCurrency(this.strategy1, Currency.AUD);

        Assert.assertNull(cashBalance3);

        CashBalance cashBalance4 = this.dao.findByStrategyAndCurrency(this.strategy2, Currency.USD);

        Assert.assertNull(cashBalance4);

        CashBalance cashBalance5 = this.dao.findByStrategyAndCurrency(this.strategy1, Currency.USD);

        Assert.assertNotNull(cashBalance5);
        Assert.assertSame(cashBalance1.getStrategy(), cashBalance5.getStrategy());
        Assert.assertSame(cashBalance1, cashBalance5);
    }

    @Test
    public void testFindByStrategyAndCurrencyLocked() {

        CashBalance cashBalance1 = new CashBalanceImpl();
        cashBalance1.setCurrency(Currency.USD);
        cashBalance1.setAmount(new BigDecimal(111));
        cashBalance1.setStrategy(this.strategy1);

        this.session.save(this.strategy1);
        this.session.save(this.strategy2);
        this.session.save(cashBalance1);
        this.session.flush();

        CashBalance cashBalance3 = this.dao.findByStrategyAndCurrencyLocked(this.strategy1, Currency.AUD);

        Assert.assertNull(cashBalance3);

        CashBalance cashBalance4 = this.dao.findByStrategyAndCurrencyLocked(this.strategy2, Currency.USD);

        Assert.assertNull(cashBalance4);

        CashBalance cashBalance5 = this.dao.findByStrategyAndCurrencyLocked(this.strategy1, Currency.USD);

        Assert.assertNotNull(cashBalance5);
        Assert.assertSame(cashBalance1.getStrategy(), cashBalance5.getStrategy());
        Assert.assertSame(cashBalance1, cashBalance5);
    }

    @Test
    public void testFindHeldCurrencies() {

        CashBalance cashBalance1 = new CashBalanceImpl();
        cashBalance1.setCurrency(Currency.USD);
        cashBalance1.setAmount(new BigDecimal(111));
        cashBalance1.setStrategy(this.strategy1);

        CashBalance cashBalance2 = new CashBalanceImpl();
        cashBalance2.setCurrency(Currency.USD);
        cashBalance2.setAmount(new BigDecimal(222));
        cashBalance2.setStrategy(this.strategy2);

        this.session.save(this.strategy1);
        this.session.save(this.strategy2);
        this.session.save(cashBalance1);
        this.session.save(cashBalance2);
        this.session.flush();

        List<Currency> currencies1 = this.dao.findHeldCurrencies();

        Assert.assertEquals(1, currencies1.size());
        Assert.assertEquals(Currency.USD, currencies1.get(0));

        cashBalance2.setCurrency(Currency.AUD);
        this.session.flush();

        List<Currency> currencies2 = this.dao.findHeldCurrencies();

        Assert.assertEquals(2, currencies2.size());
        Assert.assertEquals(Currency.USD, currencies2.get(0));
        Assert.assertEquals(Currency.AUD, currencies2.get(1));
    }

    @Test
    public void tesFindHeldCurrenciesByStrategy() {

        CashBalance cashBalance1 = new CashBalanceImpl();
        cashBalance1.setCurrency(Currency.USD);
        cashBalance1.setAmount(new BigDecimal(111));
        cashBalance1.setStrategy(this.strategy1);

        CashBalance cashBalance2 = new CashBalanceImpl();
        cashBalance2.setCurrency(Currency.AUD);
        cashBalance2.setAmount(new BigDecimal(222));
        cashBalance2.setStrategy(this.strategy2);

        this.session.save(this.strategy1);
        this.session.save(this.strategy2);
        this.session.save(cashBalance1);
        this.session.save(cashBalance2);
        this.session.flush();

        List<Currency> currencies1 = this.dao.findHeldCurrenciesByStrategy("Strategy1");

        Assert.assertEquals(1, currencies1.size());
        Assert.assertEquals(Currency.USD, currencies1.get(0));

        cashBalance2.setStrategy(this.strategy1);
        this.session.flush();

        List<Currency> currencies2 = this.dao.findHeldCurrenciesByStrategy("Strategy1");

        Assert.assertEquals(2, currencies2.size());
        Assert.assertEquals(Currency.USD, currencies2.get(0));
        Assert.assertEquals(Currency.AUD, currencies2.get(1));
    }

}
