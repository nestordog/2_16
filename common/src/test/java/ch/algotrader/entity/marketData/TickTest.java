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
package ch.algotrader.entity.marketData;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.StockImpl;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class TickTest {

    @Test
    public void testEquals() {

        Tick tick1 = new TickImpl();
        Tick tick2 = new TickImpl();

        Assert.assertNotEquals(tick1, tick2);

        Date date = new Date();

        Security security1 = new StockImpl();
        security1.setIsin("AAA");

        tick1.setDateTime(date);
        tick1.setSecurity(security1);

        Assert.assertNotEquals(tick1, tick2);

        tick2.setDateTime(date);

        Assert.assertNotEquals(tick1, tick2);

        Security security2 = new StockImpl();

        tick2.setSecurity(security2);

        Assert.assertNotEquals(tick1, tick2);

        security1.setIsin("BBB");

        Assert.assertNotEquals(tick1, tick2);

        security1.setIsin("AAA");

        Assert.assertEquals(tick1, tick1);
    }
}
