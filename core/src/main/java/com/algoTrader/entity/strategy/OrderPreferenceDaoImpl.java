package com.algoTrader.entity.strategy;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.algoTrader.entity.trade.MarketOrder;
import com.algoTrader.entity.trade.Order;
import com.algoTrader.util.MyLogger;

public class OrderPreferenceDaoImpl extends OrderPreferenceDaoBase {

    private static Logger logger = MyLogger.getLogger(OrderPreferenceDaoImpl.class.getName());

    @SuppressWarnings("rawtypes")
    @Override
    protected Order handleCreateOrder(String strategyName, Class securityClass) {

        String securityType = StringUtils.remove(ClassUtils.getShortClassName(securityClass), "Impl");
        OrderPreference orderPreference = findByStrategyAndType(strategyName, securityType);

        if (orderPreference != null) {
            try {
                Class orderClazz = Class.forName("com.algoTrader.entity.trade." + orderPreference.getOrderType() + "Impl");
                return (Order) orderClazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            logger.info("no orderPreference has been defined for: " + strategyName + " " + securityType);
            return MarketOrder.Factory.newInstance();
        }
    }
}
