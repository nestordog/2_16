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
package ch.algotrader.future;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.util.DateTimeUtil;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class FutureSymbolTest {

    private SecurityFamily securityFamily;

    @Before
    public void setup() {

        this.securityFamily = new SecurityFamilyImpl();
        this.securityFamily.setName("CrudeOil");
        this.securityFamily.setSymbolRoot("CL");
        this.securityFamily.setCurrency(Currency.USD);
        this.securityFamily.setContractSize(1000);
    }

    @Test
    public void testGetSymbol() throws Exception {

        String pattern1 = "N SR CS C MMMM MMM MM MR YYYY YY YR";
        Assert.assertEquals("CrudeOil CL 1000 USD JANUARY JAN 01 F 2015 15 F", FutureSymbol.getSymbol(this.securityFamily, DateTimeUtil.parseLocalDate("2015-01-01"), pattern1));
        Assert.assertEquals("CrudeOil CL 1000 USD JUNE JUN 06 M 2016 16 G", FutureSymbol.getSymbol(this.securityFamily, DateTimeUtil.parseLocalDate("2016-06-01"), pattern1));

        String pattern2 = "SRMRYYYY";
        Assert.assertEquals("CLF2015", FutureSymbol.getSymbol(this.securityFamily, DateTimeUtil.parseLocalDate("2015-01-01"), pattern2));
        Assert.assertEquals("CLM2016", FutureSymbol.getSymbol(this.securityFamily, DateTimeUtil.parseLocalDate("2016-06-01"), pattern2));

    }

    @Test
    public void testGetIsin() throws Exception {

        Assert.assertEquals("0FCLFF00000", FutureSymbol.getIsin(this.securityFamily, DateTimeUtil.parseLocalDate("2015-01-01")));
        Assert.assertEquals("0FCLMF00000", FutureSymbol.getIsin(this.securityFamily, DateTimeUtil.parseLocalDate("2015-06-01")));
    }

    @Test
    public void testGetRic() throws Exception {

        Assert.assertEquals("CLF5:VE", FutureSymbol.getRic(this.securityFamily, DateTimeUtil.parseLocalDate("2015-01-01")));
        Assert.assertEquals("CLM5:VE", FutureSymbol.getRic(this.securityFamily, DateTimeUtil.parseLocalDate("2015-06-01")));
    }
}
