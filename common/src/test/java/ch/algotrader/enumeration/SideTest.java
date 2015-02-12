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
package ch.algotrader.enumeration;

import org.junit.Assert;
import org.junit.Test;

public class SideTest {

    @Test
    public void testFromValue() {

        Assert.assertEquals(Side.BUY, Side.fromValue(Side.BUY.getValue()));
        Assert.assertEquals(Side.SELL, Side.fromValue(Side.SELL.getValue()));
        Assert.assertEquals(Side.SELL_SHORT, Side.fromValue(Side.SELL_SHORT.getValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromInvalidValue() {

        Side.fromValue("blah");
    }

}
