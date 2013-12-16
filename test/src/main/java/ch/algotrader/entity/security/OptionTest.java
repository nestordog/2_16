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
import ch.algotrader.entity.security.OptionDao;

/**
 * @author <a href="mailto:amasood@algotrader.ch">Ahmad Masood</a>
 *
 * @version $Revision$ $Date$
 */
public class OptionTest extends EntityTest {

    private OptionDao optionDao;

    @Before
    public void before() {

        this.optionDao = ServiceLocator.instance().getService("optionDao", OptionDao.class);
    }

    @Test
    public void testFindByExpirationStrikeAndType() {

        this.optionDao.findByExpirationStrikeAndType(0, null, null, null);
    }

    @Test
    public void testFindByMinExpirationAndMinStrikeDistance() {

        this.optionDao.findByMinExpirationAndMinStrikeDistance(0, null, null, null);
    }

    @Test
    public void testFindByMinExpirationAndMinStrikeDistanceWithTicks() {

        this.optionDao.findByMinExpirationAndMinStrikeDistanceWithTicks(0, null, null,null, null);
    }

    @Test
    public void testFindByMinExpirationAndStrikeLimit() {

        this.optionDao.findByMinExpirationAndStrikeLimit(0, null, null, null);
    }

    @Test
    public void testFindByMinExpirationAndStrikeLimitWithTicks() {

        this.optionDao.findByMinExpirationAndStrikeLimitWithTicks(0, null, null, null, null);
    }

    @Test
    public void testFindBySecurityFamily() {

        this.optionDao.findBySecurityFamily(0);
    }

    @Test
    public void testFindExpirationsByUnderlyingAndDate() {

        this.optionDao.findExpirationsByUnderlyingAndDate(0, null);
    }

    @Test
    public void testFindSubscribedOptions() {

        this.optionDao.findSubscribedOptions();
    }
}
