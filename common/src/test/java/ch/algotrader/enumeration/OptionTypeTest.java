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
package ch.algotrader.enumeration;

import org.junit.Assert;
import org.junit.Test;

public class OptionTypeTest {

    @Test
    public void testFromValue() {

        Assert.assertEquals(OptionType.CALL, OptionType.fromValue(OptionType.CALL.getValue()));
        Assert.assertEquals(OptionType.PUT, OptionType.fromValue(OptionType.PUT.getValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromInvalidValue() {

        OptionType.fromValue("blah");
    }

}
