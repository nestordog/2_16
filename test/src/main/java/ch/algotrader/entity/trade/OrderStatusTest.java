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
package ch.algotrader.entity.trade;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.EntityTest;

/**
 * @author <a href="mailto:amasood@algotrader.ch">Ahmad Masood</a>
 *
 * @version $Revision$ $Date$
 */
public class OrderStatusTest extends EntityTest {

    private OrderStatusDao orderStatusDao;

    @Before
    public void before() {

        this.orderStatusDao = ServiceLocator.instance().getService("orderStatusDao", OrderStatusDao.class);
    }

    @Test
    @Ignore
    public void testFindAllOrderStati() {

        this.orderStatusDao.findAllOrderStati();
    }

    @Test
    public void testFindOrderStatiByStrategy() {

        this.orderStatusDao.findOrderStatiByStrategy(new String());
    }

    @Test
    @Ignore
    public void testFindOrderStatusByIntId() {

        this.orderStatusDao.findOrderStatusByIntId(new String());
    }

}
