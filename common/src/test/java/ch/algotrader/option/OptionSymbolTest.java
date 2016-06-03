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
package ch.algotrader.option;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.entity.security.OptionFamily;
import ch.algotrader.entity.security.OptionFamilyImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.OptionType;
import ch.algotrader.util.DateTimeUtil;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class OptionSymbolTest {

    private OptionFamily securityFamily;

    @Before
    public void setup() {

        this.securityFamily = new OptionFamilyImpl();
        this.securityFamily.setName("CrudeOil");
        this.securityFamily.setSymbolRoot("CL");
        this.securityFamily.setCurrency(Currency.USD);
        this.securityFamily.setContractSize(1000);
    }

    @Test
    public void testGetSymbol() throws Exception {

        String pattern1 = "N SR CS C MMMM MMM MM MR YYYY YY YR W T TT S";
        Assert.assertEquals("CrudeOil CL 1000 USD JANUARY JAN 01 M 2015 15 F 1 P PUT 500",
                OptionSymbol.getSymbol(this.securityFamily, DateTimeUtil.parseLocalDate("2015-01-01"), OptionType.PUT, new BigDecimal("500"), pattern1));
        Assert.assertEquals("CrudeOil CL 1000 USD JUNE JUN 06 F 2016 16 G 3 C CALL 500",
                OptionSymbol.getSymbol(this.securityFamily, DateTimeUtil.parseLocalDate("2016-06-20"), OptionType.CALL, new BigDecimal("500"), pattern1));

        String pattern2 = "SRMRYYYY TS";
        Assert.assertEquals("CLM2015 P500", OptionSymbol.getSymbol(this.securityFamily, DateTimeUtil.parseLocalDate("2015-01-01"), OptionType.PUT, new BigDecimal("500"), pattern2));
        Assert.assertEquals("CLF2016 C500", OptionSymbol.getSymbol(this.securityFamily, DateTimeUtil.parseLocalDate("2016-06-20"), OptionType.CALL, new BigDecimal("500"), pattern2));

    }

    @Test
    public void testGetIsin() throws Exception {

        Assert.assertEquals("1OCLAF1002S", OptionSymbol.getIsin(
                this.securityFamily, DateTimeUtil.parseLocalDate("2015-01-01"), OptionType.CALL, new BigDecimal("10.0")));
        Assert.assertEquals("1OCLMF1003C", OptionSymbol.getIsin(
                this.securityFamily, DateTimeUtil.parseLocalDate("2015-01-01"), OptionType.PUT, new BigDecimal("12.0")));
        Assert.assertEquals("1OCLAF1003C", OptionSymbol.getIsin(
                this.securityFamily, DateTimeUtil.parseLocalDate("2015-01-01"), OptionType.CALL, new BigDecimal("12.0")));
        Assert.assertEquals("1OCLFF1002S", OptionSymbol.getIsin(
                this.securityFamily, DateTimeUtil.parseLocalDate("2015-06-01"), OptionType.CALL, new BigDecimal("10.0")));
    }

    @Test
    public void testGetRic() throws Exception {

        Assert.assertEquals("CLA011501000.U", OptionSymbol.getRic(
                this.securityFamily, DateTimeUtil.parseLocalDate("2015-01-01"), OptionType.CALL, new BigDecimal("10.0")));
        Assert.assertEquals("CLM011501200.U", OptionSymbol.getRic(
                this.securityFamily, DateTimeUtil.parseLocalDate("2015-01-01"), OptionType.PUT, new BigDecimal("12.0")));
        Assert.assertEquals("CLA011501200.U", OptionSymbol.getRic(
                this.securityFamily, DateTimeUtil.parseLocalDate("2015-01-01"), OptionType.CALL, new BigDecimal("12.0")));
        Assert.assertEquals("CLF011501000.U", OptionSymbol.getRic(
                this.securityFamily, DateTimeUtil.parseLocalDate("2015-06-01"), OptionType.CALL, new BigDecimal("10.0")));
    }

}
