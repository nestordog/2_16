package com.algoTrader.esper.subscriber;

import org.apache.log4j.Logger;

import com.algoTrader.util.MyLogger;

public class ExceptionSubscriber {

    private static Logger logger = MyLogger.getLogger(ExceptionSubscriber.class.getName());

    public void update(String reason) {

        logger.error(reason);
    }
}
