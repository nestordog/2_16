/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.entity.order;

import org.junit.Assert;
import org.junit.Test;

import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.MarketOrderImpl;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class OrderTest {

    @Test
    public void testEquals() {

        MarketOrder order1 = new MarketOrderImpl();
        MarketOrder order2 = new MarketOrderImpl();

        Assert.assertEquals(order1, order1);
        Assert.assertEquals(order1, order2);

        order1.setIntId("one");

        Assert.assertNotEquals(order1, order2);

        order2.setIntId("two");

        Assert.assertNotEquals(order1, order2);

        MarketOrder order3 = new MarketOrderImpl();
        order3.setIntId("one");

        Assert.assertEquals(order1, order3);
    }
}
