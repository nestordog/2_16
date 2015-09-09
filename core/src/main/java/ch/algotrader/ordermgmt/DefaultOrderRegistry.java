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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;

import ch.algotrader.entity.trade.ExecutionStatusVO;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderDetailsVO;
import ch.algotrader.enumeration.Status;

/**
* Default implementation of {@link OrderRegistry}.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*/
public class DefaultOrderRegistry implements OrderRegistry {

    private static final int MAX_RECENT_COMPLETED_ORDERS = 10;

    private final ConcurrentMap<String, Order> orderMap;
    private final ConcurrentMap<String, OrderDetailsVO> orderExecMap;
    private final Deque<OrderDetailsVO> completedOrders;

    public DefaultOrderRegistry() {
        this.orderMap = new ConcurrentHashMap<>();
        this.orderExecMap = new ConcurrentHashMap<>();
        this.completedOrders = new ConcurrentLinkedDeque<>();
    }

    @Override
    public void add(final Order order) {

        Validate.notNull(order, "Order is null");
        String intId = order.getIntId();
        Validate.notNull(intId, "Order IntId is null");

        if (this.orderExecMap.putIfAbsent(intId, new OrderDetailsVO(order,
                new ExecutionStatusVO(intId, Status.OPEN, 0L, order.getQuantity(), LocalDateTime.now()))) != null) {
            throw new IllegalStateException("Entry with IntId " + intId + " already present");
        }
        if (this.orderMap.putIfAbsent(intId, order) != null) {
            throw new IllegalStateException("Entry with IntId " + intId + " already present");
        }
    }

    @Override
    public Order remove(final String intId) {

        Validate.notNull(intId, "Order IntId is null");

        this.orderExecMap.remove(intId);
        return this.orderMap.remove(intId);
    }

    @Override
    public Order getByIntId(final String intId) {

        Validate.notNull(intId, "Order IntId is null");

        return this.orderMap.get(intId);
    }

    @Override
    public Order getOpenOrderByIntId(final String intId) {

        Validate.notNull(intId, "Order IntId is null");

        OrderDetailsVO entry = this.orderExecMap.get(intId);
        return entry != null ? entry.getOrder() : null;
    }

    @Override
    public ExecutionStatusVO getStatusByIntId(final String intId) {

        Validate.notNull(intId, "Order IntId is null");

        OrderDetailsVO entry = this.orderExecMap.get(intId);
        return entry != null ? entry.getExecutionStatus() : null;
    }

    @Override
    public void updateExecutionStatus(final String intId, final Status status, final long filledQuantity, final long remainingQuantity) {

        Validate.notNull(intId, "Order IntId is null");
        OrderDetailsVO entry = this.orderExecMap.get(intId);
        if (entry == null) {
            throw new IllegalStateException("Entry with IntId " + intId + " not found");
        }
        OrderDetailsVO updatedEntry = new OrderDetailsVO(entry.getOrder(),
                new ExecutionStatusVO(intId, status, filledQuantity, remainingQuantity, LocalDateTime.now()));
        if (status == Status.EXECUTED || status == Status.CANCELED || status == Status.REJECTED) {
            this.completedOrders.addFirst(updatedEntry);
            this.orderExecMap.remove(intId);
            if (this.completedOrders.size() > MAX_RECENT_COMPLETED_ORDERS) {
                this.completedOrders.pollLast();
            }
        } else {
            this.orderExecMap.replace(intId, entry, updatedEntry);
        }
    }

    @Override
    public List<Order> getAllOpenOrders() {
        return this.orderExecMap.values().stream()
                .map(OrderDetailsVO::getOrder)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDetailsVO> getOpenOrderDetails() {
        return this.orderExecMap.values().stream()
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDetailsVO> getRecentOrderDetails() {
        return new ArrayList<>(this.completedOrders);
    }

    @Override
    public List<Order> getOpenOrdersByParentIntId(final String parentIntId) {
        return this.orderExecMap.values().stream()
                .filter(entry -> {
                    Order order = entry.getOrder();
                    return !order.isAlgoOrder() && order.getParentOrder() != null && order.getParentOrder().getIntId().equals(parentIntId);
                })
                .map(OrderDetailsVO::getOrder)
                .collect(Collectors.toList());
    }

}
