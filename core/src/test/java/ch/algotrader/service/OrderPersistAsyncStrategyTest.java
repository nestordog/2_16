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
package ch.algotrader.service;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import ch.algotrader.entity.BaseEntityI;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.LimitOrderImpl;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.MarketOrderImpl;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.OrderStatusImpl;

public class OrderPersistAsyncStrategyTest {

    @Test
    public void testBasicAsyncPersistence() throws Exception {

        final LinkedBlockingQueue<BaseEntityI> eventQueue = new LinkedBlockingQueue<BaseEntityI>();
        final OrderPersistenceService directStrategy = new OrderPersistenceService() {


            @Override
            public void persistOrder(final Order order) {

                try {
                    eventQueue.put(order);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }

            @Override
            public void persistOrderStatus(final OrderStatus orderStatus) {

                try {
                    eventQueue.put(orderStatus);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        directStrategy.persistOrderStatus(new OrderStatusImpl());
        directStrategy.persistOrder(new MarketOrderImpl());
        directStrategy.persistOrder(new LimitOrderImpl());
        directStrategy.persistOrderStatus(new OrderStatusImpl());

        BaseEntityI event1 = eventQueue.poll(1, TimeUnit.SECONDS);
        Assert.assertNotNull(event1);
        Assert.assertTrue(event1 instanceof OrderStatus);

        BaseEntityI event2 = eventQueue.poll(1, TimeUnit.SECONDS);
        Assert.assertNotNull(event2);
        Assert.assertTrue(event2 instanceof MarketOrder);

        BaseEntityI event3 = eventQueue.poll(1, TimeUnit.SECONDS);
        Assert.assertNotNull(event3);
        Assert.assertTrue(event3 instanceof LimitOrder);

        BaseEntityI event4 = eventQueue.poll(1, TimeUnit.SECONDS);
        Assert.assertNotNull(event4);
        Assert.assertTrue(event4 instanceof OrderStatus);
    }

}
