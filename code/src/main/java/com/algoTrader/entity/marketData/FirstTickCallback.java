package com.algoTrader.entity.marketData;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.esper.subscriber.Subscriber;
import com.algoTrader.util.MyLogger;

public abstract class FirstTickCallback {

    private static Logger logger = MyLogger.getLogger(Subscriber.class.getName());

    public void update(String strategyName, Tick[] ticks) throws Exception {

        // get the securityIds sorted asscending
        Set<Integer> sortedSecurityIds = new TreeSet<Integer>(CollectionUtils.collect(Arrays.asList(ticks), new Transformer<Tick, Integer>() {
            @Override
            public Integer transform(Tick tick) {
                return tick.getSecurity().getId();
            }
        }));

        // get the statement alias based on all security ids
        String alias = "FIRST_TICK_" + StringUtils.join(sortedSecurityIds, "_");

        // undeploy the statement
        ServiceLocator.commonInstance().getRuleService().undeployRule(strategyName, alias);

        long startTime = System.currentTimeMillis();
        logger.debug("firstTickCallback start");

        // call orderCompleted
        onFirstTick(strategyName, ticks);

        logger.debug("firstTickCallback end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
    }

    public abstract void onFirstTick(String strategyName, Tick[] ticks) throws Exception;
}
