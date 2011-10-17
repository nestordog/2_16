package com.algoTrader.entity.marketData;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.marketData.Tick;

public abstract class FirstTickCallback {

    public void update(String strategyName, Tick tick) throws Exception {

        // undeploy the statement
        ServiceLocator.commonInstance().getRuleService().undeployRule(strategyName, "FIRST_TICK_" + tick.getSecurity().getId());

        // call orderCompleted
        onFirstTick(strategyName, tick);
    }

    public abstract void onFirstTick(String strategyName, Tick tick) throws Exception;
}
