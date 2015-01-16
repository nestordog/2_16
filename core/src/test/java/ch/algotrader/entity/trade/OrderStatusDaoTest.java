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

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import ch.algotrader.esper.NoopEngine;
import ch.algotrader.hibernate.InMemoryDBTest;

/**
* Unit tests for {@link ch.algotrader.entity.trade.OrderStatusDaoImpl}.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*
* @version $Revision$ $Date$
*/
public class OrderStatusDaoTest extends InMemoryDBTest {

    private OrderStatusDao dao;

    public OrderStatusDaoTest() throws IOException {

        super();
    }

    @Override
    @Before
    public void setup() throws Exception {

        super.setup();

        this.dao = new OrderStatusDaoImpl(this.sessionFactory, NoopEngine.SERVER);
    }

    @Test
    public void testFindPending() {

        // Could not execute query in test environment
        // Caused by: org.h2.jdbc.JdbcSQLException: Function "substring_index" not found
    }

    @Test
    public void testFindAllOrderStati() {

        // Could not test the method due to EngineLocator dependency
    }

    @Test
    public void testFindOrderStatusByIntId() {

        // Could not test the method due to EngineLocator dependency
    }

    @Test
    public void testFindOrderStatiByStrategy() {

        // Could not test the method due to EngineLocator dependency
    }

}
