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
import ch.algotrader.enumeration.OptionType;
import ch.algotrader.hibernate.InMemoryDBTest;

/**
 * Unit tests for {@link ch.algotrader.entity.security.IntrestRateDaoImpl}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class ImpliedVolatilityDaoTest extends InMemoryDBTest {

    private ImpliedVolatilityDao dao;

    private SecurityFamily family;

    public ImpliedVolatilityDaoTest() throws IOException {

        super();
    }

    @Override
    @Before
    public void setup() throws Exception {

        super.setup();
        this.dao = new ImpliedVolatilityDaoImpl(this.sessionFactory);

        this.family = new SecurityFamilyImpl();
        this.family.setName("Forex1");
        this.family.setTickSizePattern("0<0.1");
        this.family.setCurrency(Currency.USD);
    }

    @Test
    public void testFindByDurationDeltaAndType() {

        ImpliedVolatility impliedVolatility1 = new ImpliedVolatilityImpl();
        impliedVolatility1.setSecurityFamily(this.family);
        impliedVolatility1.setDuration(Duration.DAY_1);
        impliedVolatility1.setType(OptionType.CALL);
        impliedVolatility1.setDelta(100.5);

        this.session.save(this.family);
        this.session.save(impliedVolatility1);
        this.session.flush();

        ImpliedVolatility impliedVolatility2 = this.dao.findByDurationDeltaAndType(Duration.DAY_2, 100.5, OptionType.CALL);

        Assert.assertNull(impliedVolatility2);

        ImpliedVolatility impliedVolatility3 = this.dao.findByDurationDeltaAndType(Duration.DAY_1, 100.2, OptionType.CALL);

        Assert.assertNull(impliedVolatility3);

        ImpliedVolatility impliedVolatility4 = this.dao.findByDurationDeltaAndType(Duration.DAY_1, 100.5, OptionType.PUT);

        Assert.assertNull(impliedVolatility4);

        ImpliedVolatility impliedVolatility5 = this.dao.findByDurationDeltaAndType(Duration.DAY_1, 100.5, OptionType.CALL);

        Assert.assertNotNull(impliedVolatility5);
        Assert.assertSame(impliedVolatility1, impliedVolatility5);
    }

    @Test
    public void testFindByDurationMoneynessAndType() {

        ImpliedVolatility impliedVolatility1 = new ImpliedVolatilityImpl();
        impliedVolatility1.setSecurityFamily(this.family);
        impliedVolatility1.setDuration(Duration.DAY_1);
        impliedVolatility1.setType(OptionType.CALL);
        impliedVolatility1.setMoneyness(100.5);

        this.session.save(this.family);
        this.session.save(impliedVolatility1);
        this.session.flush();

        ImpliedVolatility impliedVolatility2 = this.dao.findByDurationMoneynessAndType(Duration.DAY_2, 100.5, OptionType.CALL);

        Assert.assertNull(impliedVolatility2);

        ImpliedVolatility impliedVolatility3 = this.dao.findByDurationMoneynessAndType(Duration.DAY_1, 100.2, OptionType.CALL);

        Assert.assertNull(impliedVolatility3);

        ImpliedVolatility impliedVolatility4 = this.dao.findByDurationMoneynessAndType(Duration.DAY_1, 100.5, OptionType.PUT);

        Assert.assertNull(impliedVolatility4);

        ImpliedVolatility impliedVolatility5 = this.dao.findByDurationMoneynessAndType(Duration.DAY_1, 100.5, OptionType.CALL);

        Assert.assertNotNull(impliedVolatility5);
        Assert.assertSame(impliedVolatility1, impliedVolatility5);
    }

}
