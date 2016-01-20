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
package ch.algotrader.ordermgmt;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.ExecutionStatusVO;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderDetailsVO;
import ch.algotrader.enumeration.Status;

public class DefaultOrderBookTest {

    private DefaultOrderBook impl;

    @Before
    public void setup() {
        this.impl = new DefaultOrderBook();
    }

    @Test
    public void testAddRemoveGet() {

        Order order = MarketOrder.Factory.newInstance();
        order.setIntId("Blah");
        order.setQuantity(123L);

        this.impl.add(order);
        final Order order1 = this.impl.getByIntId("Blah");
        Assert.assertSame(order, order1);
        final ExecutionStatusVO status1 = this.impl.getStatusByIntId("Blah");
        Assert.assertNotNull(status1);
        Assert.assertEquals(Status.OPEN, status1.getStatus());
        Assert.assertEquals("Blah", status1.getIntId());
        Assert.assertEquals(0L, status1.getFilledQuantity());
        Assert.assertEquals(123L, status1.getRemainingQuantity());

        this.impl.remove("Blah");
        Assert.assertNull(this.impl.getByIntId("Blah"));
        Assert.assertNull(this.impl.getStatusByIntId("Blah"));
    }

    @Test(expected = OrderRegistryException.class)
    public void testInvalidAdd() {

        Order order = MarketOrder.Factory.newInstance();
        order.setIntId("Blah");
        order.setQuantity(123L);

        this.impl.add(order);
        this.impl.add(order);
    }

    @Test
    public void testUpdateExecution() {

        Order order = MarketOrder.Factory.newInstance();
        order.setIntId("Blah");
        order.setQuantity(123L);

        this.impl.add(order);
        this.impl.updateExecutionStatus("Blah", Status.SUBMITTED, 0L, 123L);
        final ExecutionStatusVO status1 = this.impl.getStatusByIntId("Blah");
        Assert.assertNotNull(status1);
        Assert.assertEquals(Status.SUBMITTED, status1.getStatus());
        Assert.assertEquals("Blah", status1.getIntId());
        Assert.assertEquals(0L, status1.getFilledQuantity());
        Assert.assertEquals(123L, status1.getRemainingQuantity());

        this.impl.updateExecutionStatus("Blah", Status.PARTIALLY_EXECUTED, 23L, 100L);
        final ExecutionStatusVO status2 = this.impl.getStatusByIntId("Blah");
        Assert.assertNotNull(status2);
        Assert.assertEquals(Status.PARTIALLY_EXECUTED, status2.getStatus());
        Assert.assertEquals("Blah", status2.getIntId());
        Assert.assertEquals(23L, status2.getFilledQuantity());
        Assert.assertEquals(100L, status2.getRemainingQuantity());

        this.impl.updateExecutionStatus("Blah", Status.EXECUTED, 123L, 0L);
        Assert.assertNull(this.impl.getStatusByIntId("Blah"));
    }

    @Test(expected = OrderRegistryException.class)
    public void testInvalidUpdateExecution() {

        Order order = MarketOrder.Factory.newInstance();
        order.setIntId("Blah");
        order.setQuantity(123L);

        this.impl.add(order);
        this.impl.updateExecutionStatus("Blah", Status.SUBMITTED, 0L, 123L);
        this.impl.updateExecutionStatus("Blah", Status.PARTIALLY_EXECUTED, 23L, 100L);
        this.impl.updateExecutionStatus("Blah", Status.EXECUTED, 123L, 0L);
        this.impl.updateExecutionStatus("Blah", Status.EXECUTED, 123L, 0L);
    }

    @Test
    public void testGetRecentOrders() {

        Order order1 = MarketOrder.Factory.newInstance();
        order1.setIntId("Blah");
        order1.setQuantity(123L);
        this.impl.add(order1);

        Order order2 = MarketOrder.Factory.newInstance();
        order2.setIntId("Yada");
        order2.setQuantity(123L);

        this.impl.add(order2);

        List<OrderDetailsVO> recentOrderDetails1 = this.impl.getRecentOrderDetails();
        Assert.assertNotNull(recentOrderDetails1);
        Assert.assertEquals(0, recentOrderDetails1.size());

        this.impl.updateExecutionStatus("Yada", Status.CANCELED, 123L, 0L);
        List<OrderDetailsVO> recentOrderDetails2 = this.impl.getRecentOrderDetails();
        Assert.assertNotNull(recentOrderDetails2);
        Assert.assertEquals(1, recentOrderDetails2.size());
        Assert.assertSame(order2, recentOrderDetails2.get(0).getOrder());

        this.impl.updateExecutionStatus("Blah", Status.REJECTED, 123L, 0L);
        List<OrderDetailsVO> recentOrderDetails3 = this.impl.getRecentOrderDetails();
        Assert.assertNotNull(recentOrderDetails3);
        Assert.assertEquals(2, recentOrderDetails3.size());
        Assert.assertSame(order1, recentOrderDetails3.get(0).getOrder());
        Assert.assertSame(order2, recentOrderDetails3.get(1).getOrder());
    }

    @Test
    public void testGetNextOrderIdVersion() {

        Assert.assertEquals("blah.1", this.impl.getNextOrderIdRevision("blah.0"));
        Assert.assertEquals("blah.2", this.impl.getNextOrderIdRevision("blah.0"));
        Assert.assertEquals("blah.3", this.impl.getNextOrderIdRevision("blah.0"));
        Assert.assertEquals("blah.155", this.impl.getNextOrderIdRevision("blah.154"));
        Assert.assertEquals("yada.102", this.impl.getNextOrderIdRevision("yada.101"));
        Assert.assertEquals("yada.103", this.impl.getNextOrderIdRevision("yada.101"));
        Assert.assertEquals("yada.10101", this.impl.getNextOrderIdRevision("yada.10100"));
    }

    @Test(expected = OrderRegistryException.class)
    public void testGetNextOrderIdVersionUnexpectedFormat() {

        this.impl.getNextOrderIdRevision("blah-blah");
    }

    @Test(expected = OrderRegistryException.class)
    public void testGetNextOrderIdVersionUnexpectedFormat2() {

        this.impl.getNextOrderIdRevision("blah.blah");
    }

    @Test
    public void testEvictExecutedOrders() {

        Order order1 = MarketOrder.Factory.newInstance();
        order1.setIntId("Blah");
        order1.setQuantity(123L);
        this.impl.add(order1);

        Order order2 = MarketOrder.Factory.newInstance();
        order2.setIntId("Yada");
        order2.setQuantity(123L);
        this.impl.add(order2);

        Order order3 = MarketOrder.Factory.newInstance();
        order3.setIntId("Booh");
        order3.setQuantity(123L);
        this.impl.add(order3);

        this.impl.evictCompleted();
        Assert.assertSame(order1, this.impl.getByIntId("Blah"));
        Assert.assertSame(order2, this.impl.getByIntId("Yada"));
        Assert.assertSame(order3, this.impl.getByIntId("Booh"));

        this.impl.updateExecutionStatus("Yada", Status.CANCELED, 123L, 0L);
        this.impl.updateExecutionStatus("Blah", Status.REJECTED, 123L, 0L);

        this.impl.evictCompleted();
        Assert.assertNull(this.impl.getByIntId("Blah"));
        Assert.assertNull(this.impl.getByIntId("Yada"));
        Assert.assertSame(order3, this.impl.getByIntId("Booh"));
    }

    @Test
    public void testExtIdLookup() {

        Order order = MarketOrder.Factory.newInstance();
        order.setIntId("Blah");
        order.setQuantity(123L);

        this.impl.add(order);
        Assert.assertEquals(null, this.impl.lookupIntId("Blah"));
        this.impl.updateExecutionStatus("Blah", "Blah-Ext", Status.SUBMITTED, 0L, 123L);
        Assert.assertEquals("Blah", this.impl.lookupIntId("Blah-Ext"));
        this.impl.updateExecutionStatus("Blah", "---", Status.PARTIALLY_EXECUTED, 23L, 100L);
        Assert.assertEquals("Blah", this.impl.lookupIntId("Blah-Ext"));
    }

    @Test
    public void testGetOpenOrdersByCriteria() {

        Strategy strategy1 = Strategy.Factory.newInstance();
        strategy1.setId(1);
        Strategy strategy2 = Strategy.Factory.newInstance();
        strategy2.setId(2);
        Forex forex1 = Forex.Factory.newInstance();
        forex1.setId(3);
        Forex forex2 = Forex.Factory.newInstance();
        forex2.setId(4);
        Forex forex3 = Forex.Factory.newInstance();
        forex3.setId(5);

        Order order1 = MarketOrder.Factory.newInstance();
        order1.setIntId("1");
        order1.setQuantity(123L);
        order1.setSecurity(forex1);
        order1.setStrategy(strategy1);

        Order order2 = MarketOrder.Factory.newInstance();
        order2.setIntId("2");
        order2.setQuantity(123L);
        order2.setSecurity(forex2);
        order2.setStrategy(strategy2);

        Order order3 = MarketOrder.Factory.newInstance();
        order3.setIntId("3");
        order3.setQuantity(123L);
        order3.setSecurity(forex3);
        order3.setStrategy(strategy2);

        this.impl.add(order1);
        this.impl.add(order2);
        this.impl.add(order3);

        Assert.assertEquals(Arrays.asList(order1), this.impl.getOpenOrdersByStrategy(1));
        Assert.assertEquals(Arrays.asList(order2), this.impl.getOpenOrdersBySecurity(4));
        Assert.assertEquals(Arrays.asList(order2, order3), this.impl.getOpenOrdersByStrategy(2));
        Assert.assertEquals(Arrays.asList(order3), this.impl.getOpenOrdersByStrategyAndSecurity(2, 5));
    }


}
