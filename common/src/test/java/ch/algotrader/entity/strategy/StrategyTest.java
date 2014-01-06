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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class StrategyTest {

    @Test
    public void testEquals() {

        Strategy strategy1 = new StrategyImpl();
        Strategy strategy2 = new StrategyImpl();

        Assert.assertNotEquals(strategy1, strategy2);

        strategy1.setName("A");

        Assert.assertNotEquals(strategy1, strategy2);

        strategy2.setName("B");

        Assert.assertNotEquals(strategy1, strategy2);

        Strategy strategy3 = new StrategyImpl();
        strategy3.setName("A");

        Assert.assertEquals(strategy1, strategy3);
    }
}
