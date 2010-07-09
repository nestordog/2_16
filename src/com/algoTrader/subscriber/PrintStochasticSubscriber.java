package com.algoTrader.subscriber;

import org.apache.log4j.Logger;

import com.algoTrader.entity.Characteristic;
import com.algoTrader.util.MyLogger;

public class PrintStochasticSubscriber {

    private static Logger logger = MyLogger.getLogger(PrintStochasticSubscriber.class.getName());

    public void update(Characteristic kFast, Characteristic kSlow, Characteristic dSlow)  {

        logger.debug(kFast.getSecurity().getSymbol() + "," + kFast.getValue() + "," + kSlow.getValue() + "," + dSlow.getValue());
    }
}
