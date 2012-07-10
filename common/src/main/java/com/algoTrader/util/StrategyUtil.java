package com.algoTrader.util;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.StrategyImpl;

public class StrategyUtil {

    private static boolean simulation;
    private static String strategyName;

    public static String getStartedStrategyName() {

        if (strategyName == null) {

            simulation = ServiceLocator.instance().getConfiguration().getSimulation();
            if (simulation) {
                strategyName = StrategyImpl.BASE;
            } else {
                strategyName = ServiceLocator.instance().getConfiguration().getStrategyName();
                if (strategyName == null) {
                    throw new IllegalStateException("no strategy defined on commandline");
                }
            }
        }
        return strategyName;
    }

    public static boolean isStartedStrategyBASE() {

        return StrategyImpl.BASE.equals(getStartedStrategyName());
    }
}
