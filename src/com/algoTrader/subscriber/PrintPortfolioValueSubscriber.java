package com.algoTrader.subscriber;

import java.math.BigDecimal;

import org.apache.log4j.Logger;

import com.algoTrader.entity.Transaction;
import com.algoTrader.util.MyLogger;

public class PrintPortfolioValueSubscriber {

    private static Logger logger = MyLogger.getLogger(PrintPortfolioValueSubscriber.class.getName());

    public void update(long timestamp, BigDecimal cashBalance, BigDecimal securitiesCurrentValue, BigDecimal maintenanceMargin, BigDecimal netLiqValue, Transaction transaction) {

        logger.info(cashBalance + "," + securitiesCurrentValue + "," + maintenanceMargin + ((transaction != null) ? ("," + transaction.getValueDouble()) : ""));
    }
}
