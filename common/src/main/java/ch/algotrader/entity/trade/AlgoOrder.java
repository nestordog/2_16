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
package ch.algotrader.entity.trade;


import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.ClassUtils;

import ch.algotrader.entity.marketData.TickI;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public abstract class AlgoOrder extends OrderImpl {

    private static final long serialVersionUID = 5310975560518020161L;

    private Collection<Allocation> allocations = new HashSet<>();

    public Collection<Allocation> getAllocations() {
        return this.allocations;
    }

    public void setAllocations(Collection<Allocation> allocationsIn) {
        this.allocations = allocationsIn;
    }

    public boolean addAllocations(Allocation elementToAdd) {
        return this.allocations.add(elementToAdd);
    }

    public boolean removeAllocations(Allocation elementToRemove) {
        return this.allocations.remove(elementToRemove);
    }

    /**
     * Gets initial child {@link SimpleOrder SimpleOrders} that should be placed on the market right
     * away.
     * @param tick
     * @return List<SimpleOrder>
     */
    public abstract List<SimpleOrder> getInitialOrders(TickI tick);

    @Override
    public boolean isAlgoOrder() {
        return true;
    }

    /**
     * Modifies an existing {@link SimpleOrder SimpleOrders} (if applicable for this {@link
     * AlgoOrder})
     * @param tick
     * @return SimpleOrder
     */
    public SimpleOrder modifyOrder(TickI tick) {
        throw new UnsupportedOperationException("modify order not supported by " + ClassUtils.getShortClassName(this.getClass()));
    }

    /**
     * Returns the next {@link SimpleOrder SimpleOrders} (if applicable for this {@link AlgoOrder})
     * @param remainingQuantity
     * @param tick
     * @return SimpleOrder
     */
    public SimpleOrder nextOrder(long remainingQuantity, TickI tick) {
        throw new UnsupportedOperationException("next order not supported by " + ClassUtils.getShortClassName(this.getClass()));
    }
}
