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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.ExpirationType;
import ch.algotrader.hibernate.InMemoryDBTest;

/**
 * Unit tests for {@link ch.algotrader.entity.security.OptionFamilyDaoImpl}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class OptionFamilyDaoTest extends InMemoryDBTest {

    private OptionFamilyDao dao;

    public OptionFamilyDaoTest() throws IOException {

        super();
    }

    @Override
    @Before
    public void setup() throws Exception {

        super.setup();

        this.dao = new OptionFamilyDaoImpl(this.sessionFactory);
    }

    @Test
    public void testFindByUnderlying() {

        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);

        Forex forex1 = new ForexImpl();
        forex1.setSymbol("EUR.USD");
        forex1.setBaseCurrency(Currency.EUR);
        forex1.setSecurityFamily(family1);

        OptionFamily optionFamily1 = new OptionFamilyImpl();
        optionFamily1.setName("OptionFamily");
        optionFamily1.setCurrency(Currency.INR);
        optionFamily1.setTickSizePattern("TickSizePattern");
        optionFamily1.setExpirationType(ExpirationType.NEXT_3_RD_FRIDAY);
        optionFamily1.setExpirationDistance(Duration.DAY_1);
        optionFamily1.setUnderlying(forex1);

        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(optionFamily1);
        this.session.flush();

        OptionFamily optionFamily2 = this.dao.findByUnderlying(0);

        Assert.assertNull(optionFamily2);

        OptionFamily optionFamily3 = this.dao.findByUnderlying(forex1.getId());

        Assert.assertNotNull(optionFamily3);

        Assert.assertSame(forex1, optionFamily3.getUnderlying());
        Assert.assertSame(optionFamily1, optionFamily3);
    }

}
