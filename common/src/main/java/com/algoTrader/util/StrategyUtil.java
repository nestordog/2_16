package com.algoTrader.util;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyImpl;

public class StrategyUtil {

    private static boolean simulation;
    private static Strategy strategy;
    private static String strategyName;

    /**
     * returns the "main" started startegy. in simulation this is always BASE in realtime this is whatever has been specified on the command-line
     */
    public static Strategy getStartedStrategy() {

        if (strategy == null) {
            String strategyName = getStartedStrategyName();
            strategy = ServiceLocator.instance().getLookupService().getStrategyByNameFetched(strategyName);
        }
        return strategy;
    }

    public static String getStartedStrategyName() {

        if (strategyName == null) {

            simulation = ServiceLocator.instance().getConfiguration().getSimulation();
            if (simulation) {
                strategyName = StrategyImpl.BASE;
            } else {
                strategyName = ServiceLocator.instance().getConfiguration().getStrategyName();
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
