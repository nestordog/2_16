package com.algoTrader.util;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyImpl;

public class StrategyUtil {

    private static boolean simulation = ConfigurationUtil.getBaseConfig().getBoolean("simulation");
    private static Strategy strategy;
    private static String strategyName;

    /**
     * returns the "main" started startegy. in simulation this is always BASE in realtime this is whatever has been specified on the command-line
     */
    public static Strategy getStartedStrategy() {

        if (strategy == null) {
            String strategyName = getStartedStrategyName();
            strategy = ServiceLocator.commonInstance().getLookupService().getStrategyByNameFetched(strategyName);
        }
        return strategy;
    }

    public static String getStartedStrategyName() {

        if (strategyName == null) {
            if (simulation) {
                strategyName = StrategyImpl.BASE;
            } else {
                strategyName = ConfigurationUtil.getBaseConfig().getString("strategyName");
                if (strategyName == null) {
                    throw new RuntimeException("no strategy defined on commandline");
                }
            }
        }
        return strategyName;
    }

    public static boolean isStartedStrategyBASE() {

        return StrategyImpl.BASE.equals(getStartedStrategyName());
    }
}
