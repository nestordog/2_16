package com.algoTrader.subscriber;

import org.apache.log4j.Logger;

import com.algoTrader.entity.Tick;
import com.algoTrader.util.MyLogger;

public class PrintTickSubscriber {

    private static Logger logger = MyLogger.getLogger(PrintTickSubscriber.class.getName());

    public void update(Tick tick) {

        logger.info("received tick for " + tick.getSecurity().getSymbol() + " " + tick);
    }
}
