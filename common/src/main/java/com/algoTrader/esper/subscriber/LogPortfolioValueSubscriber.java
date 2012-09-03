package com.algoTrader.esper.subscriber;

import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.strategy.PortfolioValue;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.metric.MetricsUtil;

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
