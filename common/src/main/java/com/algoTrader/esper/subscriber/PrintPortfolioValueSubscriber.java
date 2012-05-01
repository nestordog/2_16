package com.algoTrader.esper.subscriber;

import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Transaction;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.util.metric.MetricsUtil;
import com.algoTrader.vo.PortfolioValueVO;

public class PrintPortfolioValueSubscriber {

    private static Logger logger = MyLogger.getLogger(PrintPortfolioValueSubscriber.class.getName());

    private static long initialBalance = ServiceLocator.instance().getConfiguration().getInitialBalance();

    private static boolean initialized = false;

    public void update(long timestamp, PortfolioValueVO portfolioValue, Transaction transaction) {

        long startTime = System.nanoTime();

        // dont log anything while initialising macd
        if (portfolioValue.getNetLiqValue() != initialBalance) {
            initialized = true;
        }

        if (initialized) {
            //@formatter:off
            logger.info(RoundUtil.getBigDecimal(portfolioValue.getCashBalance()) + "," +
                    RoundUtil.getBigDecimal(portfolioValue.getSecuritiesCurrentValue()) + "," +
                    RoundUtil.getBigDecimal(portfolioValue.getMaintenanceMargin()) + "," +
                    RoundUtil.getBigDecimal(portfolioValue.getLeverage())
                + ((transaction != null) ? ("," + transaction.getNetValue()) : ""));
            //@formatter:on
        }

        MetricsUtil.accountEnd("PrintPortfolioValueSubscriber", startTime);
    }
}
