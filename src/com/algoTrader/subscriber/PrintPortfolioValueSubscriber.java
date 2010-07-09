package com.algoTrader.subscriber;

import org.apache.log4j.Logger;

import com.algoTrader.util.MyLogger;

public class PrintPortfolioValueSubscriber {

    private static Logger logger = MyLogger.getLogger(PrintPortfolioValueSubscriber.class.getName());

    public void update(long timestamp, double portfolioValue) {

        logger.info(portfolioValue);
    }
}
