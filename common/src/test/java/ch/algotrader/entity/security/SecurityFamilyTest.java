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
package ch.algotrader.entity.security;

import org.junit.Assert;
import org.junit.Test;

import ch.algotrader.util.RoundUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class SecurityFamilyTest {

    @Test
    public void testRoundUp() {


        SecurityFamily family = SecurityFamily.Factory.newInstance();
        family.setTickSizePattern("0<0.05 | 3<0.1");
        family.setScale(2);

        Assert.assertEquals(2.05, family.roundDown(null, 2.07788), 0.000000001);
        Assert.assertEquals(2.10, family.roundUp(null, 2.07456), 0.000000001);

        Assert.assertEquals(5.0, family.roundDown(null, 5.05), 0.000000001);
        Assert.assertEquals(5.1, family.roundUp(null, 5.05), 0.000000001);

        Assert.assertEquals(RoundUtil.getBigDecimal(2.05), family.roundDown(null, RoundUtil.getBigDecimal(2.075)));
        Assert.assertEquals(RoundUtil.getBigDecimal(2.10), family.roundUp(null, RoundUtil.getBigDecimal(2.075)));

        Assert.assertEquals(RoundUtil.getBigDecimal(5.0, 1), family.roundDown(null, RoundUtil.getBigDecimal(5.05)));
        Assert.assertEquals(RoundUtil.getBigDecimal(5.1, 1), family.roundUp(null, RoundUtil.getBigDecimal(5.05)));

    }
}
