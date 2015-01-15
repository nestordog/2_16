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
package ch.algotrader.entity.security;

import java.io.IOException;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ch.algotrader.entity.DataConsistencyException;
import ch.algotrader.entity.SubscriptionDao;
import ch.algotrader.entity.SubscriptionDaoImpl;
import ch.algotrader.entity.marketData.TickDao;
import ch.algotrader.entity.marketData.TickDaoImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.hibernate.InMemoryDBTest;

/**
 * Unit tests for {@link ch.algotrader.entity.security.ForexDaoImpl}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class ForexDaoTest extends InMemoryDBTest {

    private ForexDao dao;

    public ForexDaoTest() throws IOException {

        super();
    }

    @Override
    @Before
    public void setup() throws Exception {

        super.setup();

        SubscriptionDao subscriptionDao = new SubscriptionDaoImpl(this.sessionFactory);
        TickDao tickDao = new TickDaoImpl(this.sessionFactory, subscriptionDao);

        this.dao = new ForexDaoImpl(this.sessionFactory, tickDao, 4);
    }

    @Rule public ExpectedException exception = ExpectedException.none();

    @Test
    public void testGetRateDoubleException() {

        // Could not test the method for IllegalStateException due to EngineLocator dependency in forex.getCurrentMarketDataEvent() method
    }

    @Test
    public void testGetRateDouble() {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setName("Forex1");
        family.setTickSizePattern("0<0.1");
        family.setCurrency(Currency.INR);

        Forex forex1 = new ForexImpl();
        forex1.setSecurityFamily(family);
        forex1.setBaseCurrency(Currency.USD);

        this.session.save(family);
        this.session.save(forex1);
        this.session.flush();

        double rate = this.dao.getRateDouble(Currency.USD, Currency.USD);
        Assert.assertEquals(1.0, rate, 0);

        // Could not test further due to EngineLocator dependency in forex.getCurrentMarketDataEvent() method
    }

    @Test
    public void testGetRateDoubleByDateException() {

        // Could not test
        // Caused by: org.h2.jdbc.JdbcSQLException: Function "FROM_UNIXTIME" not found
        // Because of TickDao.findTicksBySecurityAndMaxDate call
    }

    @Test
    public void testGetRateDoubleByDate() {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setName("Forex1");
        family.setTickSizePattern("0<0.1");
        family.setCurrency(Currency.INR);

        Forex forex1 = new ForexImpl();
        forex1.setSecurityFamily(family);
        forex1.setBaseCurrency(Currency.USD);

        this.session.save(family);
        this.session.save(forex1);
        this.session.flush();

        double rate = this.dao.getRateDoubleByDate(Currency.USD, Currency.USD, new Date());
        Assert.assertEquals(1.0, rate, 0);

        // Could not test further
        // Caused by: org.h2.jdbc.JdbcSQLException: Function "FROM_UNIXTIME" not found
        // Because of TickDao.findTicksBySecurityAndMaxDate call
    }

    @Test
    public void testGetForexException1() {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setName("Forex1");
        family.setTickSizePattern("0<0.1");
        family.setCurrency(Currency.INR);

        Forex forex1 = new ForexImpl();
        forex1.setSecurityFamily(family);
        forex1.setBaseCurrency(Currency.USD);

        this.session.save(family);
        this.session.save(forex1);
        this.session.flush();

        this.exception.expect(DataConsistencyException.class);
        this.dao.getForex(Currency.AUD, Currency.INR);
    }

    @Test
    public void testGetForexException2() {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setName("Forex1");
        family.setTickSizePattern("0<0.1");
        family.setCurrency(Currency.INR);

        this.session.save(family);
        this.session.flush();

        Forex forex1 = new ForexImpl();
        forex1.setSecurityFamily(family);
        forex1.setBaseCurrency(Currency.USD);

        this.session.save(forex1);
        this.session.flush();

        this.exception.expect(DataConsistencyException.class);
        this.dao.getForex(Currency.USD, Currency.AUD);
    }

    @Test
    public void testGetForex() {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setName("Forex1");
        family.setTickSizePattern("0<0.1");
        family.setCurrency(Currency.INR);

        Forex forex1 = new ForexImpl();
        forex1.setSecurityFamily(family);
        forex1.setBaseCurrency(Currency.USD);

        this.session.save(family);
        this.session.save(forex1);
        this.session.flush();

        Forex forex2 = this.dao.getForex(Currency.USD, Currency.INR);
        Assert.assertNotNull(forex2);
        Assert.assertSame(forex1, forex2);
        Assert.assertSame(family, forex2.getSecurityFamily());

        Forex forex3 = this.dao.getForex(Currency.INR, Currency.USD);
        Assert.assertNotNull(forex3);
        Assert.assertSame(forex1, forex3);
        Assert.assertSame(family, forex3.getSecurityFamily());
    }

}
