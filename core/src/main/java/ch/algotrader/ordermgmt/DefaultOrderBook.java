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

import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;

import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderDetailsVO;
import ch.algotrader.entity.trade.OrderStatusVO;
import ch.algotrader.enumeration.Status;

/**
* Default implementation of {@link OrderBook}.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*/
public class DefaultOrderBook implements OrderBook {

    private final ConcurrentMap<String, Order> orderMap;
    private final ConcurrentMap<String, String> extIdToIntIdMap;
    private final ConcurrentMap<String, OrderDetailsVO> orderExecMap;
    private final ConcurrentMap<String, AtomicLong> revisionMap;
    private final Deque<OrderDetailsVO> completedOrders;

    public DefaultOrderBook() {
        this.orderMap = new ConcurrentHashMap<>();
        this.extIdToIntIdMap = new ConcurrentHashMap<>();
        this.orderExecMap = new ConcurrentHashMap<>();
        this.revisionMap = new ConcurrentHashMap<>();
        this.completedOrders = new ConcurrentLinkedDeque<>();
    }

    @Override
    public void add(final Order order) {

        Validate.notNull(order, "Order is null");
        String intId = order.getIntId();
        Validate.notNull(intId, "Order IntId is null");

        if (this.orderExecMap.putIfAbsent(intId, new OrderDetailsVO(order,
                new OrderStatusVO(0, new Date(), Status.OPEN, 0L, order.getQuantity(), 0L, intId, 0L, 0L))) != null) {
            throw new OrderRegistryException("Entry with IntId " + intId + " already present");
        }
        if (this.orderMap.putIfAbsent(intId, order) != null) {
            throw new OrderRegistryException("Entry with IntId " + intId + " already present");
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
    public OrderDetailsVO getOpenOrderDetailsByIntId(String intId) {

        return this.orderExecMap.get(intId);
    }

    @Override
    public Order getOpenOrderByIntId(final String intId) {

        Validate.notNull(intId, "Order IntId is null");

        OrderDetailsVO entry = this.orderExecMap.get(intId);
        return entry != null ? entry.getOrder() : null;
    }

    @Override
    public OrderStatusVO getStatusByIntId(final String intId) {

        Validate.notNull(intId, "Order IntId is null");

        OrderDetailsVO entry = this.orderExecMap.get(intId);
        return entry != null ? entry.getOrderStatus() : null;
    }

    @Override
    public void updateExecutionStatus(final String intId, final String extId, final Status status, final long filledQuantity, final long remainingQuantity) {

        Validate.notNull(intId, "Order IntId is null");
        OrderDetailsVO entry = this.orderExecMap.get(intId);
        if (entry == null) {
            throw new OrderRegistryException("Entry with IntId " + intId + " not found");
        }
        OrderDetailsVO updatedEntry = new OrderDetailsVO(entry.getOrder(),
                new OrderStatusVO(0, new Date(), status, filledQuantity, remainingQuantity, 0L, intId, 0L, 0L));
        if (status == Status.EXECUTED || status == Status.CANCELED || status == Status.REJECTED) {
            this.completedOrders.addFirst(updatedEntry);
            this.orderExecMap.remove(intId);
        } else {
            this.orderExecMap.replace(intId, entry, updatedEntry);
        }
        if (status == Status.SUBMITTED && extId != null) {
            this.extIdToIntIdMap.put(extId, intId);
        }
    }

    @Override
    public String lookupIntId(final String extId) {
        if (extId != null) {
            return this.extIdToIntIdMap.get(extId);
        }
        return null;
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

    @Override
    public List<Order> getOpenOrdersByStrategy(final long strategyId) {
        return this.orderExecMap.values().stream()
                .filter(entry -> {
                    Order order = entry.getOrder();
                    Strategy strategy = order.getStrategy();
                    return strategy != null && strategy.getId() == strategyId;
                })
                .map(OrderDetailsVO::getOrder)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> getOpenOrdersBySecurity(long securityId) {
        return this.orderExecMap.values().stream()
                .filter(entry -> {
                    Order order = entry.getOrder();
                    Security security = order.getSecurity();
                    return security != null && security.getId() == securityId;
                })
                .map(OrderDetailsVO::getOrder)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> getOpenOrdersByStrategyAndSecurity(long strategyId, long securityId) {
        return this.orderExecMap.values().stream()
                .filter(entry -> {
                    Order order = entry.getOrder();
                    Strategy strategy = order.getStrategy();
                    Security security = order.getSecurity();
                    return strategy != null && strategy.getId() == strategyId && security != null && security.getId() == securityId;
                })
                .map(OrderDetailsVO::getOrder)
                .collect(Collectors.toList());
    }

    @Override
    public void evictCompleted() {
        this.completedOrders.clear();
        this.revisionMap.clear();
        for (Iterator<Map.Entry<String, Order>> it = this.orderMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Order> entry = it.next();
            String intId = entry.getKey();
            if (!this.orderExecMap.containsKey(intId)) {
                it.remove();
            }
        }
        for (Iterator<Map.Entry<String, String>> it = this.extIdToIntIdMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, String> entry = it.next();
            String intId = entry.getKey();
            if (!this.orderExecMap.containsKey(intId)) {
                it.remove();
            }
        }
    }

    @Override
    public String getNextOrderIdRevision(final String intId) {
        if (intId == null) {
            return null;
        }
        int i = intId.indexOf('.');
        String baseId = i != -1 ? intId.substring(0, i) : null;
        if (baseId == null) {
            throw new OrderRegistryException("Unexpected internal order ID format: " + intId);
        }
        String s = intId.substring(baseId.length() + 1);
        long revision;
        try {
            revision = Long.parseLong(s);
        } catch (NumberFormatException ex) {
            throw new OrderRegistryException("Unexpected internal order ID format: " + intId);
        }
        AtomicLong count = this.revisionMap.compute(baseId, (key, existing) -> {
            if (existing != null) {
                if (existing.get() < revision) {
                    existing.set(revision);
                }
                return existing;
            }
                return new AtomicLong(revision);
            });
        long nextRevision = count.incrementAndGet();
        return baseId + '.' + nextRevision;
    }

}
