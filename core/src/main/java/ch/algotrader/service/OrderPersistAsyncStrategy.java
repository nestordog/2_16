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

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;

import ch.algotrader.entity.BaseEntityI;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.util.MyLogger;

/**
 * {@link ch.algotrader.service.OrderPersistStrategy} implementation that directly
 * commits orders and order events to a persistent store.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class OrderPersistAsyncStrategy implements OrderPersistStrategy, InitializingBean, DisposableBean, Lifecycle {

    private static Logger LOGGER = MyLogger.getLogger(OrderServiceImpl.class.getName());

    private final OrderPersistStrategy orderPersistStrategy;
    private final LinkedBlockingQueue<BaseEntityI> eventQueue;

    private final Object lifecycleMutex = new Object();
    private BackgroundThread backgroundThread;

    public OrderPersistAsyncStrategy(final OrderPersistStrategy orderPersistStrategy) {

        Validate.notNull(orderPersistStrategy, "OrderPersistStrategy is null");

        this.orderPersistStrategy = orderPersistStrategy;
        this.eventQueue = new LinkedBlockingQueue<BaseEntityI>();
    }

    @Override
    public void persistOrder(final Order order) {

        Validate.notNull(order, "Order is null");

        try {
            this.eventQueue.put(order);
        } catch (InterruptedException ex) {
            // Theoretically should never happen as unbounded queue should never block
            LOGGER.error("Interrupted while trying to enqueue event " + order, ex);
        }
    }

    @Override
    public void persistOrderStatus(final OrderStatus orderStatus) {

        Validate.notNull(orderStatus, "Order status is null");
        try {
            this.eventQueue.put(orderStatus);
        } catch (InterruptedException ex) {
            // Theoretically should never happen as unbounded queue should never block
            LOGGER.error("Interrupted while trying to enqueue event " + orderStatus, ex);
        }
    }

    @Override
    public void start() {

        Thread local = null;
        synchronized (lifecycleMutex) {
            if (backgroundThread == null) {

                backgroundThread = new BackgroundThread();
                backgroundThread.setDaemon(true);
                local = backgroundThread;
            }
        }
        if (local != null) {

            local.start();
        }
    }

    @Override
    public void stop() {

        Thread local = null;
        synchronized (lifecycleMutex) {
            if (backgroundThread != null) {

                local = backgroundThread;
                backgroundThread = null;
            }
        }
        if (local != null) {

            local.interrupt();
            try {
                local.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public boolean isRunning() {

        Thread local;
        synchronized (lifecycleMutex) {

            local = backgroundThread;
            backgroundThread = null;
        }
        return local != null && local.isAlive();
    }

    public void waitForTermination(long time, final TimeUnit tunit) throws InterruptedException {

        Thread local;
        synchronized (lifecycleMutex) {

            local = backgroundThread;
            backgroundThread = null;
        }
        if (local != null) {

            local.join(tunit.toMillis(time));
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        start();
    }

    @Override
    public void destroy() throws Exception {

        stop();
    }

    class BackgroundThread extends Thread {

        @Override
        public void run() {

            while (!Thread.currentThread().isInterrupted()) {

                try {
                    BaseEntityI event = eventQueue.take();
                    if (event instanceof OrderStatus) {

                        orderPersistStrategy.persistOrderStatus((OrderStatus) event);
                    } else if (event instanceof Order) {

                        orderPersistStrategy.persistOrder((Order) event);
                    } else {

                        LOGGER.error("Unexpected event " + event);
                    }

                } catch (InterruptedException ex) {
                    break;
                }
            }
        }
    }

}
