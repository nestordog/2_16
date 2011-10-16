package com.algoTrader.entity.trade;

import java.util.Arrays;
import java.util.TreeSet;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.StringUtils;

import com.algoTrader.ServiceLocator;

public abstract class OrderCallback {

    public void update(OrderStatus[] orderStati) throws Exception {

        String strategyName = orderStati[0].getParentOrder().getStrategy().getName();

        // get the securityIds sorted asscending
        TreeSet<Integer> sortedSecurityIds = new TreeSet<Integer>(CollectionUtils.collect(Arrays.asList(orderStati), new Transformer<OrderStatus, Integer>() {
            @Override
            public Integer transform(OrderStatus order) {
                return order.getParentOrder().getSecurity().getId();
            }
        }));

        // get the statement alias based on all security ids
        String alias = "AFTER_TRADE_" + StringUtils.join(sortedSecurityIds, "_");

        // undeploy the statement
        ServiceLocator.commonInstance().getRuleService().undeployRule(strategyName, alias);

        // call orderCompleted
        orderCompleted(orderStati);
    }

    public abstract void orderCompleted(OrderStatus[] orderStatus) throws Exception;
}
