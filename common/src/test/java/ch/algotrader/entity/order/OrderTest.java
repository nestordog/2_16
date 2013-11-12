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
package ch.algotrader.entity.order;

import org.junit.Assert;
import org.junit.Test;

import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.MarketOrderImpl;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class OrderTest {

    @Test
    public void testEquals() {

        MarketOrder order1 = new MarketOrderImpl();
        MarketOrder order2 = new MarketOrderImpl();

        Assert.assertEquals(order1, order1);
        Assert.assertNotEquals(order1, order2);

        order1.setIntId("one");

        Assert.assertNotEquals(order1, order2);

        order2.setIntId("two");

        Assert.assertNotEquals(order1, order2);

        MarketOrder order3 = new MarketOrderImpl();
        order3.setIntId("one");

        Assert.assertEquals(order1, order3);
    }
}
