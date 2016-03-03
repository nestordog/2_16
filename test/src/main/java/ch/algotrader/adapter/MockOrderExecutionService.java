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
package ch.algotrader.adapter;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import ch.algotrader.entity.trade.ExternalFill;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderCompletionVO;
import ch.algotrader.entity.trade.OrderDetailsVO;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.OrderStatusVO;
import ch.algotrader.ordermgmt.OrderBook;
import ch.algotrader.service.OrderExecutionService;
import ch.algotrader.service.ServiceException;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class MockOrderExecutionService implements OrderExecutionService {

    private final LinkedBlockingQueue<Object> eventQueue;
    private final OrderBook orderBook;

    public MockOrderExecutionService(final LinkedBlockingQueue<Object> eventQueue, final OrderBook orderBook) {
        this.eventQueue = eventQueue;
        this.orderBook = orderBook;
    }

    private void putEvent(final Object event) {
        try {
            eventQueue.put(event);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void handleOrderStatus(OrderStatus orderStatus) {
        String intId = orderStatus.getIntId();
        Order order = this.orderBook.getOpenOrderByIntId(intId);
        if (order == null) {
            throw new ServiceException("Open order with IntID " + intId + " not found");
        }

        this.orderBook.updateExecutionStatus(order.getIntId(), orderStatus.getExtId(), orderStatus.getStatus(),
                orderStatus.getFilledQuantity(), orderStatus.getRemainingQuantity());

        putEvent(orderStatus);
    }

    @Override
    public void handleFill(Fill fill) {
        putEvent(fill);
    }

    @Override
    public void handleFill(ExternalFill fill) {
        putEvent(fill);
    }

    @Override
    public void handleOrderCompletion(OrderCompletionVO orderCompletion) {
        putEvent(orderCompletion);
    }

    @Override
    public void handleRestatedOrder(final Order initialOrder, final Order restatedOrder) {

        String previousIntId = initialOrder.getIntId();
        if (previousIntId != null) {
            this.orderBook.remove(previousIntId);
        }
        this.orderBook.add(restatedOrder);
    }

    @Override
    public String lookupIntId(final String extId) {
        return this.orderBook.lookupIntId(extId);
    }

    @Override
    public List<Order> getOpenOrdersByParentIntId(final String parentIntId) {
        return this.orderBook.getOpenOrdersByParentIntId(parentIntId);
    }

    @Override
    public OrderDetailsVO getOpenOrderDetailsByIntId(final String intId) {
        return this.orderBook.getOpenOrderDetailsByIntId(intId);
    }

    @Override
    public OrderStatusVO getStatusByIntId(final String intId) {
        return this.orderBook.getStatusByIntId(intId);
    }

    @Override
    public Order getOpenOrderByIntId(final String intId) {
        return this.orderBook.getOpenOrderByIntId(intId);
    }

    @Override
    public Order getOrderByIntId(String intId) {
        return this.orderBook.getByIntId(intId);
    }
}
