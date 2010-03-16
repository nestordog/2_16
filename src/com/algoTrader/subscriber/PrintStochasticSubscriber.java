package com.algoTrader.subscriber;

import org.apache.log4j.Logger;

import com.algoTrader.util.MyLogger;

public class PrintStochasticSubscriber {

    private static Logger logger = MyLogger.getLogger(PrintStochasticSubscriber.class.getName());

    public void update(Double kFast, Double kSlow, Double dSlow) {

        logger.debug("kFast=" + kFast + " kSlow=" + kSlow + " dSlow=" + dSlow);
    }
}
