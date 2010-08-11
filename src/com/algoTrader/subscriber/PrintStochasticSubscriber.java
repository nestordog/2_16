package com.algoTrader.subscriber;

import org.apache.log4j.Logger;

import com.algoTrader.entity.DSlow;
import com.algoTrader.entity.KFast;
import com.algoTrader.entity.KSlow;
import com.algoTrader.util.MyLogger;

public class PrintStochasticSubscriber {

    private static Logger logger = MyLogger.getLogger(PrintStochasticSubscriber.class.getName());

    public void update(KFast kFast, KSlow kSlow, DSlow dSlow)  {

        logger.debug(kFast.getSecurity().getSymbol() + "," +
                kFast.getCall() + "," + kSlow.getCall() + "," + dSlow.getCall() + "," +
                kFast.getPut() + "," + kSlow.getPut() + "," + dSlow.getPut());
    }
}
