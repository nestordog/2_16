/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.entity.security;

import org.junit.Before;
import org.junit.Test;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.EntityTest;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.OptionType;

/**
 * @author <a href="mailto:amasood@algotrader.ch">Ahmad Masood</a>
 *
 * @version $Revision$ $Date$
 */
public class ImpliedVolatilityTest extends EntityTest {

    private ImpliedVolatilityDao impliedVolatilityDao;

    @Before
    public void before() {

        this.impliedVolatilityDao = ServiceLocator.instance().getService("impliedVolatilityDao", ImpliedVolatilityDao.class);
    }

    @Test
    public void testFindByDurationDeltaAndType() {

        this.impliedVolatilityDao.findByDurationDeltaAndType(Duration.DAY_1, new Double(0), OptionType.CALL);
    }

    @Test
    public void testFindByDurationMoneynessAndType() {

        this.impliedVolatilityDao.findByDurationMoneynessAndType(Duration.DAY_1, new Double(0), OptionType.CALL);
    }

}
