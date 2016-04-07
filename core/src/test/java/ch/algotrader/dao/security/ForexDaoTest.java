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
package ch.algotrader.dao.security;

import java.io.IOException;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.dao.marketData.TickDao;
import ch.algotrader.dao.marketData.TickDaoImpl;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.hibernate.InMemoryDBTest;

/**
 * Unit tests for {@link ForexDaoImpl}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
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

        TickDao tickDao = new TickDaoImpl(this.sessionFactory);

        this.dao = new ForexDaoImpl(this.sessionFactory, tickDao, 4);
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

    @Test
    public void testGetForexRateNotFound() {

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

        Double fxRate = this.dao.getRateDoubleByDate(Currency.USD, Currency.AUD, new Date());
        Assert.assertNull(fxRate);

    }

    @Test
    public void testGetForexRateNotAvailable() {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setName("Forex1");
        family.setTickSizePattern("0<0.1");
        family.setCurrency(Currency.AUD);

        this.session.save(family);
        this.session.flush();

        Forex forex1 = new ForexImpl();
        forex1.setSecurityFamily(family);
        forex1.setBaseCurrency(Currency.USD);

        this.session.save(forex1);
        this.session.flush();

        Double fxRate = this.dao.getRateDoubleByDate(Currency.USD, Currency.AUD, new Date());
        Assert.assertNull(fxRate);

    }

}
