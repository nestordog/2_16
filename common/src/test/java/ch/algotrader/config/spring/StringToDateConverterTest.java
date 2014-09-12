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

package ch.algotrader.config.spring;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StringToDateConverterTest {

    private DateFormat dateFormat;
    private StringToDateConverter converter;

    @Before
    public void setup() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        converter = new StringToDateConverter();
    }

    @Test
    public void testBasicConversion() throws Exception {

        Assert.assertEquals(dateFormat.parse("2014-01-01 00:00:00"), converter.convert("2014-01-01"));
        Assert.assertEquals(dateFormat.parse("2014-01-01 00:00:00"), converter.convert("2014-1-1"));
        Assert.assertEquals(dateFormat.parse("1970-01-01 01:12:00"), converter.convert("1:12:00"));
        Assert.assertEquals(dateFormat.parse("2014-01-01 12:12:12"), converter.convert("2014-01-01 12:12:12"));
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
