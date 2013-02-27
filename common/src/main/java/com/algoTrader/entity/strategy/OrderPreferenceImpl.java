package com.algoTrader.entity.strategy;

import com.algoTrader.entity.trade.AlgoOrder;
import com.algoTrader.entity.trade.Order;
import com.algoTrader.util.BeanUtil;

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
                order.setAccount(getDefaultAccount());
            }

            // set allocations if defined
            if (getAllocationsInitialized().size() > 0) {

                if (!(order instanceof AlgoOrder)) {
                    throw new IllegalStateException("allocations cannot be assigned to " + orderClazz + " (only AlgoOrders can have allocations)");
                } else {

                    double totalAllocation = 0;
                    for (Allocation allocation : getAllocations()) {
                        totalAllocation += allocation.getValue();
                    }

                    if (totalAllocation != 1.0) {
                        throw new IllegalStateException("sum of allocations are not 1.0 for " + toString());
                    }

                    AlgoOrder algoOrder = (AlgoOrder) order;
                    algoOrder.setAllocations(getAllocations());
                }
            }

            return order;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {

        return getName() + " " + getOrderType() + " " + getPropertyNameValueMap();
    }
}
