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
package ch.algotrader.option;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.entity.security.OptionFamily;
import ch.algotrader.entity.security.OptionFamilyImpl;
import ch.algotrader.enumeration.OptionType;
import ch.algotrader.util.DateTimeLegacy;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class OptionSymbolTest {

    private OptionFamily securityFamily;

    @Before
    public void setup() {

        securityFamily = new OptionFamilyImpl();
        securityFamily.setSymbolRoot("CL");
    }

    @Test
    public void testGetSymbol() throws Exception {

        Assert.assertEquals("CL JAN/15-C 10.0", OptionSymbol.getSymbol(
                securityFamily, DateTimeLegacy.parseAsDateGMT("2015-01-01"), OptionType.CALL, new BigDecimal("10.0"), false));
        Assert.assertEquals("CL 01/Jan/15-P 12.0", OptionSymbol.getSymbol(
                securityFamily, DateTimeLegacy.parseAsDateGMT("2015-01-01"), OptionType.PUT, new BigDecimal("12.0"), true));
        Assert.assertEquals("CL 01/Jan/15-C 12.0", OptionSymbol.getSymbol(
                securityFamily, DateTimeLegacy.parseAsDateGMT("2015-01-01"), OptionType.CALL, new BigDecimal("12.0"), true));
        Assert.assertEquals("CL JUN/15-C 10.0", OptionSymbol.getSymbol(
                securityFamily, DateTimeLegacy.parseAsDateGMT("2015-06-01"), OptionType.CALL, new BigDecimal("10.0"), false));
    }

    @Test
    public void testGetIsin() throws Exception {

        Assert.assertEquals("1OCLAF1002S", OptionSymbol.getIsin(
                securityFamily, DateTimeLegacy.parseAsDateGMT("2015-01-01"), OptionType.CALL, new BigDecimal("10.0")));
        Assert.assertEquals("1OCLMF1003C", OptionSymbol.getIsin(
                securityFamily, DateTimeLegacy.parseAsDateGMT("2015-01-01"), OptionType.PUT, new BigDecimal("12.0")));
        Assert.assertEquals("1OCLAF1003C", OptionSymbol.getIsin(
                securityFamily, DateTimeLegacy.parseAsDateGMT("2015-01-01"), OptionType.CALL, new BigDecimal("12.0")));
        Assert.assertEquals("1OCLFF1002S", OptionSymbol.getIsin(
                securityFamily, DateTimeLegacy.parseAsDateGMT("2015-06-01"), OptionType.CALL, new BigDecimal("10.0")));
    }

    @Test
    public void testGetRic() throws Exception {

        Assert.assertEquals("CLA011501000.U", OptionSymbol.getRic(
                securityFamily, DateTimeLegacy.parseAsDateGMT("2015-01-01"), OptionType.CALL, new BigDecimal("10.0")));
        Assert.assertEquals("CLM011501200.U", OptionSymbol.getRic(
                securityFamily, DateTimeLegacy.parseAsDateGMT("2015-01-01"), OptionType.PUT, new BigDecimal("12.0")));
        Assert.assertEquals("CLA011501200.U", OptionSymbol.getRic(
                securityFamily, DateTimeLegacy.parseAsDateGMT("2015-01-01"), OptionType.CALL, new BigDecimal("12.0")));
        Assert.assertEquals("CLF011501000.U", OptionSymbol.getRic(
                securityFamily, DateTimeLegacy.parseAsDateGMT("2015-06-01"), OptionType.CALL, new BigDecimal("10.0")));
    }

}
