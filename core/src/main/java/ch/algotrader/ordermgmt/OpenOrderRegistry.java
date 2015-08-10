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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;

import ch.algotrader.entity.trade.SimpleOrder;

/**
* Registry of open orders.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*/
public class OpenOrderRegistry {

    private final Map<String, SimpleOrder> mapByIntId;

    public OpenOrderRegistry() {
        this.mapByIntId = new ConcurrentHashMap<>();
    }

    public void add(final SimpleOrder order) {

        Validate.notNull(order, "Order is null");
        Validate.notNull(order.getIntId(), "Order IntId is null");

        this.mapByIntId.put(order.getIntId(), order);
    }

    public SimpleOrder findByIntId(final String intId) {

        Validate.notNull(intId, "Order IntId is null");

        return this.mapByIntId.get(intId);
    }

    public SimpleOrder remove(final String intId) {

        Validate.notNull(intId, "Order IntId is null");

        return this.mapByIntId.remove(intId);
    }

    public List<SimpleOrder> getAll() {
        return new ArrayList<>(this.mapByIntId.values());
    }

    public SimpleOrder findOpenOrderByRootIntId(final String rootId) {
        return mapByIntId.values().stream()
                .filter(order -> order.getIntId().startsWith(rootId))
                .findFirst()
                .get();
    }

    public Collection<SimpleOrder> findOpenOrdersByParentIntId(final String parentIntId) {
        return mapByIntId.values().stream()
                .filter(order -> !order.isAlgoOrder() && order.getParentOrder() != null && order.getParentOrder().getIntId().equals(parentIntId))
                .collect(Collectors.toList());
    }

}
