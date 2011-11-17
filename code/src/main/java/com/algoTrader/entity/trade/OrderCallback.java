package com.algoTrader.entity.trade;

import java.util.Arrays;
import java.util.TreeSet;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.esper.subscriber.Subscriber;
import com.algoTrader.util.MyLogger;

public abstract class OrderCallback {

    private static Logger logger = MyLogger.getLogger(Subscriber.class.getName());

    public void update(String strategyName, OrderStatus[] orderStati) throws Exception {

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

        long startTime = System.currentTimeMillis();
        logger.debug("orderCallback start");

        // call orderCompleted
        orderCompleted(orderStati);

        logger.debug("orderCallback end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
    }

    public abstract void orderCompleted(OrderStatus[] orderStatus) throws Exception;
}
