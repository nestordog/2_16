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
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Duration;

/**
 * @author <a href="mailto:amasood@algotrader.ch">Ahmad Masood</a>
 *
 * @version $Revision$ $Date$
 */
public class IntrestRateTest extends EntityTest {

    private IntrestRateDao intrestRateDao;

    @Before
    public void befor() {

        this.intrestRateDao = ServiceLocator.instance().getService("intrestRateDao", IntrestRateDao.class);
    }

    @Test
    public void testFindByCurrencyAndDuration() {

        this.intrestRateDao.findByCurrencyAndDuration(Currency.AUD, Duration.DAY_1);
    }

}
