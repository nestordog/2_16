package com.algoTrader.entity.trade;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.algoTrader.esper.EsperManager;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.metric.MetricsUtil;

public abstract class TradeCallback {

    private static Logger logger = MyLogger.getLogger(TradeCallback.class.getName());

    public void update(String strategyName, OrderStatus[] orderStati) throws Exception {

        List<OrderStatus> orderStatusList = Arrays.asList(orderStati);

        // get the securityIds sorted asscending
        TreeSet<Integer> sortedSecurityIds = new TreeSet<Integer>(CollectionUtils.collect(orderStatusList, new Transformer<OrderStatus, Integer>() {
            @Override
            public Integer transform(OrderStatus order) {
                return order.getOrd().getSecurity().getId();
            }
        }));

        String owningStrategyName = orderStati[0].getOrd().getStrategy().getName();

        // get the statement alias based on all security ids
        String alias = "ON_TRADE_COMPLETED_" + StringUtils.join(sortedSecurityIds, "_") + "_" + owningStrategyName;

        // undeploy the statement
        EsperManager.undeployStatement(strategyName, alias);

        long startTime = System.nanoTime();
        logger.debug("onTradeCompleted start " + sortedSecurityIds + " " + owningStrategyName);

        // call orderCompleted
        onTradeCompleted(orderStatusList);

        logger.debug("onTradeCompleted end " + sortedSecurityIds + " " + owningStrategyName);

        MetricsUtil.accountEnd("TradeCallback." + strategyName, startTime);
    }

    public abstract void onTradeCompleted(List<OrderStatus> orderStatus) throws Exception;
}
