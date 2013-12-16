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
package ch.algotrader.entity.strategy;

import org.junit.Before;
import org.junit.Test;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.EntityTest;
import ch.algotrader.entity.strategy.DefaultOrderPreferenceDao;

/**
 * @author <a href="mailto:amasood@algotrader.ch">Ahmad Masood</a>
 *
 * @version $Revision$ $Date$
 */
public class DefaultOrderPreferenceTest extends EntityTest {

    private DefaultOrderPreferenceDao defaultOrderPreferenceDao;

    @Before
    public void before() {

        this.defaultOrderPreferenceDao = ServiceLocator.instance().getService("defaultOrderPreferenceDao", DefaultOrderPreferenceDao.class);
    }

    @Test
    public void testFindByStrategyAndSecurityFamilyInclOrderPreference() {

        this.defaultOrderPreferenceDao.findByStrategyAndSecurityFamilyInclOrderPreference(null, 0);
    }

}
