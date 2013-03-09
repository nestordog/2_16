/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package com.algoTrader.util;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.strategy.StrategyImpl;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
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
