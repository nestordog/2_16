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
package ch.algotrader.integration;

import java.util.Map;

import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.service.LocalServiceTest;
import ch.algotrader.service.OrderService;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class OrderStatusTest extends LocalServiceTest {

    protected SessionFactory sessionFactory;
    protected org.hibernate.Session session;

    @Before
    public void setup() throws Exception {

        this.sessionFactory = ServiceLocator.instance().getContext().getBean("sessionFactory", SessionFactory.class);
        this.session = SessionFactoryUtils.getNewSession(this.sessionFactory);
        TransactionSynchronizationManager.bindResource(this.sessionFactory, new SessionHolder(this.session));
    }

    @After
    public void cleanup() throws Exception {
        if (this.session != null) {
            this.session.close();
        }
        if (this.sessionFactory != null) {
            TransactionSynchronizationManager.unbindResource(this.sessionFactory);
            this.sessionFactory.close();
        }
    }

    @Test
    public void testLoadPendingOrders() throws Exception {

        OrderService orderService = ServiceLocator.instance().getContext().getBean("orderService", OrderService.class);

        Map<Order, OrderStatus> pendingOrderMap = orderService.loadPendingOrders();
        for (Map.Entry<Order, OrderStatus> entry: pendingOrderMap.entrySet()) {
            System.out.println("----------");
            Order order = entry.getKey();
            OrderStatus orderStatus = entry.getValue();
            System.out.println("order id: " + order.getIntId());
            System.out.println("security: " + order.getSecurity().getSymbol() +
                    "/" + order.getSecurity().getSecurityFamily().getName());
            System.out.println("strategy: " + order.getStrategy().getName());
            System.out.println("account: " + order.getAccount().getName());
            if (orderStatus != null) {
                System.out.println("order status: " + orderStatus.getStatus());
                System.out.println("filled: " + orderStatus.getFilledQuantity());
                System.out.println("remaining: " + orderStatus.getRemainingQuantity());
            }
        }
    }

}
