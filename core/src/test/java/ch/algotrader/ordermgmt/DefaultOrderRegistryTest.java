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
package ch.algotrader.ordermgmt;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.entity.trade.ExecutionStatusVO;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderDetailsVO;
import ch.algotrader.enumeration.Status;

public class DefaultOrderRegistryTest {

    private DefaultOrderRegistry impl;

    @Before
    public void setup() {
        impl = new DefaultOrderRegistry();
    }

    @Test
    public void testAddRemoveGet() {

        Order order = MarketOrder.Factory.newInstance();
        order.setIntId("Blah");
        order.setQuantity(123L);

        impl.add(order);
        final Order order1 = impl.getByIntId("Blah");
        Assert.assertSame(order, order1);
        final ExecutionStatusVO status1 = impl.getStatusByIntId("Blah");
        Assert.assertNotNull(status1);
        Assert.assertEquals(Status.OPEN, status1.getStatus());
        Assert.assertEquals("Blah", status1.getIntId());
        Assert.assertEquals(0L, status1.getFilledQuantity());
        Assert.assertEquals(123L, status1.getRemainingQuantity());

        impl.remove("Blah");
        Assert.assertNull(impl.getByIntId("Blah"));
        Assert.assertNull(impl.getStatusByIntId("Blah"));
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidAdd() {

        Order order = MarketOrder.Factory.newInstance();
        order.setIntId("Blah");
        order.setQuantity(123L);

        impl.add(order);
        impl.add(order);
    }

    @Test
    public void testUpdateExecution() {

        Order order = MarketOrder.Factory.newInstance();
        order.setIntId("Blah");
        order.setQuantity(123L);

        impl.add(order);
        impl.updateExecutionStatus("Blah", Status.SUBMITTED, 0L, 123L);
        final ExecutionStatusVO status1 = impl.getStatusByIntId("Blah");
        Assert.assertNotNull(status1);
        Assert.assertEquals(Status.SUBMITTED, status1.getStatus());
        Assert.assertEquals("Blah", status1.getIntId());
        Assert.assertEquals(0L, status1.getFilledQuantity());
        Assert.assertEquals(123L, status1.getRemainingQuantity());

        impl.updateExecutionStatus("Blah", Status.PARTIALLY_EXECUTED, 23L, 100L);
        final ExecutionStatusVO status2 = impl.getStatusByIntId("Blah");
        Assert.assertNotNull(status2);
        Assert.assertEquals(Status.PARTIALLY_EXECUTED, status2.getStatus());
        Assert.assertEquals("Blah", status2.getIntId());
        Assert.assertEquals(23L, status2.getFilledQuantity());
        Assert.assertEquals(100L, status2.getRemainingQuantity());

        impl.updateExecutionStatus("Blah", Status.EXECUTED, 123L, 0L);
        Assert.assertNull(impl.getStatusByIntId("Blah"));
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidUpdateExecution() {

        Order order = MarketOrder.Factory.newInstance();
        order.setIntId("Blah");
        order.setQuantity(123L);

        impl.add(order);
        impl.updateExecutionStatus("Blah", Status.SUBMITTED, 0L, 123L);
        impl.updateExecutionStatus("Blah", Status.PARTIALLY_EXECUTED, 23L, 100L);
        impl.updateExecutionStatus("Blah", Status.EXECUTED, 123L, 0L);
        impl.updateExecutionStatus("Blah", Status.EXECUTED, 123L, 0L);
    }

    @Test
    public void testGetRecentOrders() {

        Order order1 = MarketOrder.Factory.newInstance();
        order1.setIntId("Blah");
        order1.setQuantity(123L);
        impl.add(order1);

        Order order2 = MarketOrder.Factory.newInstance();
        order2.setIntId("Yada");
        order2.setQuantity(123L);

        impl.add(order2);

        List<OrderDetailsVO> recentOrderDetails1 = impl.getRecentOrderDetails();
        Assert.assertNotNull(recentOrderDetails1);
        Assert.assertEquals(0, recentOrderDetails1.size());

        impl.updateExecutionStatus("Yada", Status.CANCELED, 123L, 0L);
        List<OrderDetailsVO> recentOrderDetails2 = impl.getRecentOrderDetails();
        Assert.assertNotNull(recentOrderDetails2);
        Assert.assertEquals(1, recentOrderDetails2.size());
        Assert.assertSame(order2, recentOrderDetails2.get(0).getOrder());

        impl.updateExecutionStatus("Blah", Status.REJECTED, 123L, 0L);
        List<OrderDetailsVO> recentOrderDetails3 = impl.getRecentOrderDetails();
        Assert.assertNotNull(recentOrderDetails3);
        Assert.assertEquals(2, recentOrderDetails3.size());
        Assert.assertSame(order1, recentOrderDetails3.get(0).getOrder());
        Assert.assertSame(order2, recentOrderDetails3.get(1).getOrder());
    }

    @Test
    public void testEvictExecutedOrders() {

        Order order1 = MarketOrder.Factory.newInstance();
        order1.setIntId("Blah");
        order1.setQuantity(123L);
        impl.add(order1);

        Order order2 = MarketOrder.Factory.newInstance();
        order2.setIntId("Yada");
        order2.setQuantity(123L);
        impl.add(order2);

        Order order3 = MarketOrder.Factory.newInstance();
        order3.setIntId("Booh");
        order3.setQuantity(123L);
        impl.add(order3);

        impl.evictCompleted();
        Assert.assertSame(order1, impl.getByIntId("Blah"));
        Assert.assertSame(order2, impl.getByIntId("Yada"));
        Assert.assertSame(order3, impl.getByIntId("Booh"));

        impl.updateExecutionStatus("Yada", Status.CANCELED, 123L, 0L);
        impl.updateExecutionStatus("Blah", Status.REJECTED, 123L, 0L);

        impl.evictCompleted();
        Assert.assertNull(impl.getByIntId("Blah"));
        Assert.assertNull(impl.getByIntId("Yada"));
        Assert.assertSame(order3, impl.getByIntId("Booh"));
    }

}
