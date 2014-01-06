/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.entity;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class TransactionTest {

    @Test
    public void testEquals() {

        Transaction order1 = new TransactionImpl();
        Transaction order2 = new TransactionImpl();

        Assert.assertNotEquals(order1, order2);

        order1.setId(1);

        Assert.assertNotEquals(order1, order2);

        order2.setId(2);

        Assert.assertNotEquals(order1, order2);

        Transaction order3 = new TransactionImpl();
        order3.setId(1);

        Assert.assertEquals(order1, order3);
    }
}
