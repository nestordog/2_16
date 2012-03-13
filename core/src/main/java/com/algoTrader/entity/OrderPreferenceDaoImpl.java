package com.algoTrader.entity;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;

import com.algoTrader.entity.trade.MarketOrder;
import com.algoTrader.entity.trade.Order;

public class OrderPreferenceDaoImpl extends OrderPreferenceDaoBase {

    @SuppressWarnings("rawtypes")
    @Override
    protected Order handleCreateOrder(String strategyName, Class securityClass) {

        String securityType = StringUtils.remove(ClassUtils.getShortClassName(securityClass), "Impl");
        OrderPreference orderPreference = findByStrategyAndType(strategyName, securityType);

        if (orderPreference != null) {
            try {
                Class orderClazz = Class.forName("com.algoTrader.entity.trade." + orderPreference.getOrderType() + "Impl");
                return (Order) orderClazz.newInstance();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            return MarketOrder.Factory.newInstance();
        }
    }
}
