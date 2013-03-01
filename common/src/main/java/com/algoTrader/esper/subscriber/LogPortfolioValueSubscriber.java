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
package com.algoTrader.esper.subscriber;

import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.strategy.PortfolioValue;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.metric.MetricsUtil;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class LogPortfolioValueSubscriber {

    private static Logger logger = MyLogger.getLogger(LogPortfolioValueSubscriber.class.getName());

    private static long initialBalance = ServiceLocator.instance().getConfiguration().getInitialBalance();

    private static boolean initialized = false;

    public void update(PortfolioValue portfolioValue) {

        long startTime = System.nanoTime();

        // dont log anything while initialising macd
        if (portfolioValue.getNetLiqValue().longValue() != initialBalance) {
            initialized = true;
        }

        if (initialized) {
            //@formatter:off
            logger.info(portfolioValue.getCashBalance() + "," +
                    portfolioValue.getSecuritiesCurrentValue() + "," +
                    portfolioValue.getMaintenanceMargin() + "," +
                    portfolioValue.getLeverage());
            //@formatter:on
        }

        MetricsUtil.accountEnd("LogPortfolioValueSubscriber", startTime);
    }
}
