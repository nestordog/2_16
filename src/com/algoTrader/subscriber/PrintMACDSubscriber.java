package com.algoTrader.subscriber;

import org.apache.log4j.Logger;

import com.algoTrader.entity.Security;
import com.algoTrader.util.MyLogger;

public class PrintMACDSubscriber {

    private static Logger logger = MyLogger.getLogger(PrintMACDSubscriber.class.getName());

    public void update(Security security, Double macd, Double signal)  {

        logger.debug(security.getSymbol() + "," + macd + "," + signal);
    }
}
