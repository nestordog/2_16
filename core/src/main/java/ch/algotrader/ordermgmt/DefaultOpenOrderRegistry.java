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

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;

import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.ExecutionStatusVO;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderDetailsVO;
import ch.algotrader.enumeration.Status;

/**
* Default implementation of {@link OpenOrderRegistry}.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*/
public class DefaultOpenOrderRegistry implements OpenOrderRegistry {

    private final ConcurrentMap<String, OrderDetailsVO> mapByIntId;

    public DefaultOpenOrderRegistry() {
        this.mapByIntId = new ConcurrentHashMap<>();
    }

    @Override
    public void add(final Order order) {

        Validate.notNull(order, "Order is null");
        String intId = order.getIntId();
        Validate.notNull(intId, "Order IntId is null");

        OrderDetailsVO entry = this.mapByIntId.putIfAbsent(intId,
                new OrderDetailsVO(order, new ExecutionStatusVO(intId, Status.OPEN, 0L, order.getQuantity())));
        if (entry != null) {
            throw new IllegalStateException("Entry with IntId " + intId + " already present");
        }
    }

    @Override
    public Order remove(final String intId) {

        Validate.notNull(intId, "Order IntId is null");

        OrderDetailsVO entry =  this.mapByIntId.remove(intId);
        return entry != null ? entry.getOrder() : null;
    }

    @Override
    public Order getByIntId(final String intId) {

        Validate.notNull(intId, "Order IntId is null");

        OrderDetailsVO entry = this.mapByIntId.get(intId);
        return entry != null ? entry.getOrder() : null;
    }

    @Override
    public ExecutionStatusVO getStatusByIntId(String intId) {

        Validate.notNull(intId, "Order IntId is null");

        OrderDetailsVO entry = this.mapByIntId.get(intId);
        return entry != null ? entry.getExecutionStatus() : null;
    }

    @Override
    public OrderDetailsVO getDetailsByIntId(final String intId) {

        Validate.notNull(intId, "Order IntId is null");
        return this.mapByIntId.get(intId);
    }

    @Override
    public void updateExecutionStatus(final String intId, final Status status, final long filledQuantity, final long remainingQuantity) {

        Validate.notNull(intId, "Order IntId is null");
        OrderDetailsVO entry = this.mapByIntId.get(intId);
        if (entry == null) {
            throw new IllegalStateException("Entry with IntId " + intId + " not found");
        }
        this.mapByIntId.replace(intId, entry, new OrderDetailsVO(entry.getOrder(),
                new ExecutionStatusVO(intId, status, filledQuantity, remainingQuantity)));
    }

    @Override
    public List<Order> getAllOrders() {
        return this.mapByIntId.values().stream()
                .map(OrderDetailsVO::getOrder)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDetailsVO> getAllOrderDetails() {
        return this.mapByIntId.values().stream()
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDetailsVO> getOrderDetailsForStrategy(String strategyName) {
        return this.mapByIntId.values().stream()
                .filter(entry -> {
                    Strategy strategy = entry.getOrder().getStrategy();
                    return strategy != null && strategyName.equals(strategy.getName());
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByParentIntId(final String parentIntId) {
        return mapByIntId.values().stream()
                .filter(entry -> {
                    Order order = entry.getOrder();
                    return !order.isAlgoOrder() && order.getParentOrder() != null && order.getParentOrder().getIntId().equals(parentIntId);
                })
                .map(OrderDetailsVO::getOrder)
                .collect(Collectors.toList());
    }

}
