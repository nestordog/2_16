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
package ch.algotrader.entity.marketData;

import org.junit.Before;
import org.junit.Test;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.EntityTest;
import ch.algotrader.entity.marketData.BarDao;

/**
 * @author <a href="mailto:amasood@algotrader.ch">Ahmad Masood</a>
 *
 * @version $Revision$ $Date$
 */
public class BarTest extends EntityTest {

    private BarDao barDao;

    @Before
    public void before() {

        this.barDao = ServiceLocator.instance().getService("barDao",BarDao.class );
    }

    @Test
    public void testFindBarsBySecurityAndBarSize() {

        this.barDao.findBarsBySecurityAndBarSize(0, null);
    }

    @Test
    public void testFindBarsBySecurityBarSizeAndMinDate() {

        this.barDao.findBarsBySecurityBarSizeAndMinDate(0, null, null);
    }

    @Test
    public void testFindDailyBarsFromTicks() {

        this.barDao.findDailyBarsFromTicks(0, null, null);
    }

}
