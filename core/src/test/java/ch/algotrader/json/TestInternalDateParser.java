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
package ch.algotrader.json;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.Month;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class TestInternalDateParser {

    @Test
    public void testDateTimeParsing() throws Exception {

        LocalDateTime localDateTime = InternalDateDeserializer.parse("2015-11-12 13:14:15");
        Assert.assertEquals(LocalDateTime.of(2015, Month.NOVEMBER, 12, 13, 14, 15), localDateTime);
    }

    @Test
    public void testDateTimeParsingNoSeconds() throws Exception {

        LocalDateTime localDateTime = InternalDateDeserializer.parse("2015-11-12 13:14");
        Assert.assertEquals(LocalDateTime.of(2015, Month.NOVEMBER, 12, 13, 14, 0), localDateTime);
    }

    @Test
    public void testDateParsing() throws Exception {

        LocalDateTime localDateTime = InternalDateDeserializer.parse("2015-11-12");
        Assert.assertEquals(LocalDateTime.of(2015, Month.NOVEMBER, 12, 0, 0, 0), localDateTime);
    }

    @Test(expected = DateTimeException.class)
    public void testDateTimeParsingInvalid() throws Exception {

        InternalDateDeserializer.parse("2015-11-12 13");
    }

}
