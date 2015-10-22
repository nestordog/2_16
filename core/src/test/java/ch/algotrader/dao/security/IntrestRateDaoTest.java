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

import ch.algotrader.dao.security.IntrestRateDao;
import ch.algotrader.dao.security.IntrestRateDaoImpl;
import ch.algotrader.entity.security.IntrestRate;
import ch.algotrader.entity.security.IntrestRateImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.hibernate.InMemoryDBTest;

/**
 * Unit tests for {@link IntrestRateDaoImpl}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class IntrestRateDaoTest extends InMemoryDBTest {

    private IntrestRateDao dao;

    public IntrestRateDaoTest() throws IOException {

        super();
    }

    @Override
    @Before
    public void setup() throws Exception {

        super.setup();

        this.dao = new IntrestRateDaoImpl(this.sessionFactory);
    }

    @Test
    public void testFindByCurrencyAndDuration() {

        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("Forex1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.INR);

        IntrestRate intrestRate1 = new IntrestRateImpl();
        intrestRate1.setSecurityFamily(family1);
        intrestRate1.setDuration(Duration.DAY_2);

        this.session.save(family1);
        this.session.save(intrestRate1);
        this.session.flush();

        IntrestRate intrestRate2 = this.dao.findByCurrencyAndDuration(Currency.USD, Duration.DAY_2);

        Assert.assertNull(intrestRate2);

        IntrestRate intrestRate3 = this.dao.findByCurrencyAndDuration(Currency.INR, Duration.DAY_1);

        Assert.assertNull(intrestRate3);

        IntrestRate intrestRate4 = this.dao.findByCurrencyAndDuration(Currency.INR, Duration.DAY_2);

        Assert.assertNotNull(intrestRate4);

        Assert.assertSame(family1, intrestRate4.getSecurityFamily());
        Assert.assertSame(intrestRate1, intrestRate4);
    }

}
