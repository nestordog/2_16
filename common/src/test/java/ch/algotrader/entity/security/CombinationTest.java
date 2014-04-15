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

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CombinationTest {

    @Test
    public void testEqualsId() {

        Security security1 = new CombinationImpl();
        Security security2 = new CombinationImpl();

        Assert.assertNotEquals(security1, security2);

        security1.setId(1);

        Assert.assertNotEquals(security1, security2);

        security2.setId(2);

        Assert.assertNotEquals(security1, security2);

        security2.setId(1);

        Assert.assertEquals(security1, security2);
    }
}
