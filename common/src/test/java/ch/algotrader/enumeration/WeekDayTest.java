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

public class WeekDayTest {

    @Test
    public void testFromValue() {

        Assert.assertEquals(WeekDay.MONDAY, WeekDay.fromValue(WeekDay.MONDAY.getValue()));
        Assert.assertEquals(WeekDay.TUESDAY, WeekDay.fromValue(WeekDay.TUESDAY.getValue()));
        Assert.assertEquals(WeekDay.WEDNESDAY, WeekDay.fromValue(WeekDay.WEDNESDAY.getValue()));
        Assert.assertEquals(WeekDay.THURSDAY, WeekDay.fromValue(WeekDay.THURSDAY.getValue()));
        Assert.assertEquals(WeekDay.FRIDAY, WeekDay.fromValue(WeekDay.FRIDAY.getValue()));
        Assert.assertEquals(WeekDay.SATURDAY, WeekDay.fromValue(WeekDay.SATURDAY.getValue()));
        Assert.assertEquals(WeekDay.SUNDAY, WeekDay.fromValue(WeekDay.SUNDAY.getValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromInvalidValue() {

        WeekDay.fromValue(12345);
    }


}
