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
package ch.algotrader.entity;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class TransactionTest {

    @Test
    public void testEquals() {

        Transaction order1 = new TransactionImpl();
        Transaction order2 = new TransactionImpl();

        String uuid1 = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();

        Assert.assertEquals(order1, order2);

        order1.setUuid(uuid1);

        Assert.assertNotEquals(order1, order2);

        order2.setUuid(uuid2);

        Assert.assertNotEquals(order1, order2);

        Transaction order3 = new TransactionImpl();
        order3.setUuid(uuid1);

        Assert.assertEquals(order1, order3);
    }
}
