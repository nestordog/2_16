package com.algoTrader.entity.strategy;

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
            BeanUtil.populate(order, getPropertyValueMap());

            return order;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {

        return getName() + " " + getOrderType() + " " + getPropertyValueMap();
    }
}
