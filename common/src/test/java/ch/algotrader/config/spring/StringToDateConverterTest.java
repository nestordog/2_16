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

package ch.algotrader.config.spring;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.util.DateTimeLegacy;

public class StringToDateConverterTest {

    private StringToDateConverter converter;

    @Before
    public void setup() {
        converter = new StringToDateConverter();
    }

    @Test
    public void testBasicConversion() throws Exception {

        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeGMT("2014-01-01 00:00:00"), converter.convert("2014-01-01"));
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeGMT("2014-01-01 00:00:00"), converter.convert("2014-1-1"));
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeGMT("1970-01-01 01:12:00"), converter.convert("1:12:00"));
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeGMT("2014-01-01 12:12:12"), converter.convert("2014-01-01 12:12:12"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBasicConversionException1() throws Exception {

        converter.convert("stuff");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBasicConversionException2() throws Exception {

        converter.convert("2014-111-1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBasicConversionException3() throws Exception {

        converter.convert("12:00");
    }
}
