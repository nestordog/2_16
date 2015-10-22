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

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class CombinationTest {

    @Test
    public void testEqualsId() {

        Combination security1 = new CombinationImpl();
        Combination security2 = new CombinationImpl();

        Assert.assertEquals(security1, security2);

        String uuid1 = UUID.randomUUID().toString();
        security1.setUuid(uuid1);

        Assert.assertNotEquals(security1, security2);

        String uuid2 = UUID.randomUUID().toString();
        security2.setUuid(uuid2);

        Assert.assertNotEquals(security1, security2);

        security2.setUuid(uuid1);

        Assert.assertEquals(security1, security2);
    }
}
