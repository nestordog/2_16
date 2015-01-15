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
import ch.algotrader.hibernate.InMemoryDBTest;

/**
 * Unit tests for {@link ch.algotrader.entity.security.SecurityFamilyDaoImpl}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class SecurityFamilyDaoTest extends InMemoryDBTest {

    private SecurityFamilyDao dao;

    public SecurityFamilyDaoTest() throws IOException {

        super();
    }

    @Override
    @Before
    public void setup() throws Exception {

        super.setup();
        this.dao = new SecurityFamilyDaoImpl(this.sessionFactory);
    }

    @Test
    public void testFindByName() {

        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);

        this.session.save(family1);
        this.session.flush();

        SecurityFamily family2 = this.dao.findByName("NOT_FOUND");

        Assert.assertNull(family2);

        SecurityFamily family3 = this.dao.findByName("family1");

        Assert.assertNotNull(family3);
        Assert.assertSame(family1, family3);
    }

}
