package com.algoTrader.entity.trade;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.util.MyLogger;

public abstract class TradeCallback {

    private static Logger logger = MyLogger.getLogger(TradeCallback.class.getName());

    public void update(String strategyName, OrderStatus[] orderStati) throws Exception {

        List<OrderStatus> orderStatusList = Arrays.asList(orderStati);

        // get the securityIds sorted asscending
        TreeSet<Integer> sortedSecurityIds = new TreeSet<Integer>(CollectionUtils.collect(orderStatusList, new Transformer<OrderStatus, Integer>() {
            @Override
            public Integer transform(OrderStatus order) {
                return order.getParentOrder().getSecurity().getId();
            }
        }));

        // get the statement alias based on all security ids
        String alias = "ON_TRADE_COMPLETED_" + StringUtils.join(sortedSecurityIds, "_");

        // undeploy the statement
        ServiceLocator.instance().getEventService().undeployStatement(strategyName, alias);

        long startTime = System.currentTimeMillis();
        logger.debug("onTradeCompleted start " + sortedSecurityIds);

        // call orderCompleted
        onTradeCompleted(orderStatusList);

        logger.debug("onTradeCompleted end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
    }

    public abstract void onTradeCompleted(List<OrderStatus> orderStatus) throws Exception;
}
