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
package ch.algotrader.enumeration;

import org.junit.Assert;
import org.junit.Test;

public class TransactionTypeTest {

    @Test
    public void testFromValue() {

        Assert.assertEquals(TransactionType.BUY, TransactionType.fromValue(TransactionType.BUY.getValue()));
        Assert.assertEquals(TransactionType.SELL, TransactionType.fromValue(TransactionType.SELL.getValue()));
        Assert.assertEquals(TransactionType.CREDIT, TransactionType.fromValue(TransactionType.CREDIT.getValue()));
        Assert.assertEquals(TransactionType.DEBIT, TransactionType.fromValue(TransactionType.DEBIT.getValue()));
        Assert.assertEquals(TransactionType.DIVIDEND, TransactionType.fromValue(TransactionType.DIVIDEND.getValue()));
        Assert.assertEquals(TransactionType.INTREST_PAID, TransactionType.fromValue(TransactionType.INTREST_PAID.getValue()));
        Assert.assertEquals(TransactionType.INTREST_RECEIVED, TransactionType.fromValue(TransactionType.INTREST_RECEIVED.getValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromInvalidValue() {

        TransactionType.fromValue("blah");
    }

}
