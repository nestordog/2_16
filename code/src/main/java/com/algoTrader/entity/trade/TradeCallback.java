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

public abstract class TradeCallback {

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
        String alias = "ON_TRADE_COMPLETED_" + StringUtils.join(sortedSecurityIds, "_");

        // undeploy the statement
        ServiceLocator.commonInstance().getRuleService().undeployRule(strategyName, alias);

        long startTime = System.currentTimeMillis();
        logger.debug("onTradeCompleted start");

        // call orderCompleted
        onTradeCompleted(orderStati);

        logger.debug("onTradeCompleted end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
    }

    public abstract void onTradeCompleted(OrderStatus[] orderStatus) throws Exception;
}
