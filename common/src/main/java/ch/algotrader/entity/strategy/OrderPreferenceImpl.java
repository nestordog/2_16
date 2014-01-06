/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.entity.strategy;

import ch.algotrader.entity.trade.AlgoOrder;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.util.BeanUtil;
import ch.algotrader.util.ObjectUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class OrderPreferenceImpl extends OrderPreference {

    private static final long serialVersionUID = -755368809250236972L;

    @Override
    public Order createOrder() {
        try {

            // create an order instance
            Class<?> orderClazz = Class.forName(getOrderType().getValue());
            Order order = (Order) orderClazz.newInstance();

            // populate the order with the properities
            BeanUtil.populate(order, getPropertyNameValueMap());

            // set the account if defined
            if (getDefaultAccount() != null) {
                order.setAccount(getDefaultAccountInitialized());
            }

            // set allocations if defined
            if (getAllocationsInitialized().size() > 0) {

                if (!(order instanceof AlgoOrder)) {
                    throw new IllegalStateException("allocations cannot be assigned to " + orderClazz + " (only AlgoOrders can have allocations)");
                } else {

                    double totalAllocation = 0;
                    for (Allocation allocation : getAllocationsInitialized()) {
                        totalAllocation += allocation.getValue();
                    }

                    if (totalAllocation != 1.0) {
                        throw new IllegalStateException("sum of allocations are not 1.0 for " + toString());
                    }

                    AlgoOrder algoOrder = (AlgoOrder) order;
                    algoOrder.setAllocations(getAllocationsInitialized());
                }
            }

            return order;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {

        return getName() + ":" + getOrderType();
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj instanceof OrderPreference) {
            OrderPreference that = (OrderPreference) obj;
            return ObjectUtil.equalsNonNull(this.getName(), that.getName());

        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {

        int hash = 17;
        hash = hash * 37 + ObjectUtil.hashCode(getName());
        return hash;
    }
}
