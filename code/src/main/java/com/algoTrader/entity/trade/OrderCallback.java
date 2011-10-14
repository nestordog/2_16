package com.algoTrader.entity.trade;

import com.algoTrader.ServiceLocator;

public abstract class OrderCallback {

    public void update(OrderStatus orderStatus) throws Exception {

        String strategyName = orderStatus.getParentOrder().getStrategy().getName();
        int securityId = orderStatus.getParentOrder().getSecurity().getId();
        String alias = "AFTER_TRADE_" + securityId;

        ServiceLocator.commonInstance().getRuleService().undeployRule(strategyName, alias);

        orderCompleted(orderStatus);
    }

    public abstract void orderCompleted(OrderStatus orderStatus) throws Exception;
}
