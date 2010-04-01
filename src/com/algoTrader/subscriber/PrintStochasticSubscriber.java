package com.algoTrader.subscriber;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.supercsv.exception.SuperCSVException;

import com.algoTrader.entity.Characteristic;
import com.algoTrader.util.MyLogger;

public class PrintStochasticSubscriber {

    private static Logger logger = MyLogger.getLogger(PrintStochasticSubscriber.class.getName());

    public void update(Characteristic kFast, Characteristic kSlow, Characteristic dSlow) throws SuperCSVException, IOException {

        logger.debug(kFast.getSecurity().getSymbol() + " kFast=" + kFast.getValue() + " kSlow=" + kSlow.getValue() + " dSlow=" + dSlow.getValue());
    }
}
