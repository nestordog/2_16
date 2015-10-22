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
package ch.algotrader.entity.security;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class SecurityTest {

    @Test
    public void testEqualsIsin() {

        Assert.assertNotEquals(getSecurity(0, null, null, null, null, null, null), getSecurity(0, "abc", null, null, null, null, null));
        Assert.assertEquals(getSecurity(0, "abc", null, null, null, null, null), getSecurity(0, "abc", null, null, null, null, null));

    }

    @Test
    public void testEqualsBbgdi() {

        Assert.assertNotEquals(getSecurity(0, null, null, null, null, null, null), getSecurity(0, null, "abc", null, null, null, null));
        Assert.assertEquals(getSecurity(0, null, "abc", null, null, null, null), getSecurity(0, null, "abc", null, null, null, null));

    }

    @Test
    public void testEqualsRic() {

        Assert.assertNotEquals(getSecurity(0, null, null, null, null, null, null), getSecurity(0, null, null, "abc", null, null, null));
        Assert.assertEquals(getSecurity(0, null, null, "abc", null, null, null), getSecurity(0, null, null, "abc", null, null, null));

    }

    @Test
    public void testEqualsConin() {

        Assert.assertNotEquals(getSecurity(0, null, null, null, null, null, null), getSecurity(0, null, null, null, "abc", null, null));
        Assert.assertEquals(getSecurity(0, null, null, null, "abc", null, null), getSecurity(0, null, null, null, "abc", null, null));
    }

    @Test
    public void testEqualsLmaxid() {

        Assert.assertNotEquals(getSecurity(0, null, null, null, null, null, null), getSecurity(0, null, null, null, null, "abc", null));
        Assert.assertEquals(getSecurity(0, null, null, null, null, "abc", null), getSecurity(0, null, null, null, null, "abc", null));

    }

    @Test
    public void testEqualsSymbol() {

        Assert.assertNotEquals(getSecurity(0, null, null, null, null, null, null), getSecurity(0, null, null, null, null, null, "abc"));
        Assert.assertEquals(getSecurity(0, null, null, null, null, null, "abc"), getSecurity(0, null, null, null, null, null, "abc"));
    }

    private Security getSecurity(int id, String isin, String bbgid, String ric, String conid, String lmaxid, String Symbol) {

        Security security = new StockImpl();
        security.setId(id);
        security.setIsin(isin);
        security.setBbgid(bbgid);
        security.setRic(ric);
        security.setConid(conid);
        security.setLmaxid(lmaxid);
        security.setSymbol(Symbol);
        return security;
    }
}
