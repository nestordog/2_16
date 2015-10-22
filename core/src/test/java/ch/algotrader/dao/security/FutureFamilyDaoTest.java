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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.dao.security.FutureFamilyDao;
import ch.algotrader.dao.security.FutureFamilyDaoImpl;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.FutureFamily;
import ch.algotrader.entity.security.FutureFamilyImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.ExpirationType;
import ch.algotrader.hibernate.InMemoryDBTest;

/**
 * Unit tests for {@link FutureFamilyDaoImpl}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class FutureFamilyDaoTest extends InMemoryDBTest {

    private FutureFamilyDao dao;

    public FutureFamilyDaoTest() throws IOException {

        super();
    }

    @Override
    @Before
    public void setup() throws Exception {

        super.setup();

        this.dao = new FutureFamilyDaoImpl(this.sessionFactory);
    }

    @Test
    public void testFindByUnderlying() {

        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);

        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);

        FutureFamily futureFamily1 = new FutureFamilyImpl();
        futureFamily1.setName("FutureFamily");
        futureFamily1.setCurrency(Currency.INR);
        futureFamily1.setTickSizePattern("TickSizePattern");
        futureFamily1.setExpirationType(ExpirationType.NEXT_3_RD_FRIDAY);
        futureFamily1.setExpirationDistance(Duration.DAY_1);
        futureFamily1.setUnderlying(forex1);

        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(futureFamily1);
        this.session.flush();

        FutureFamily futureFamily2 = this.dao.findByUnderlying(0);

        Assert.assertNull(futureFamily2);

        FutureFamily futureFamily3 = this.dao.findByUnderlying(forex1.getId());

        Assert.assertNotNull(futureFamily3);

        Assert.assertSame(forex1, futureFamily3.getUnderlying());
        Assert.assertSame(futureFamily1, futureFamily3);
    }

}
