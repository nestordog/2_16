package com.algoTrader.subscriber;

import org.apache.log4j.Logger;

import com.algoTrader.entity.Tick;

public class PrintTickSubscriber {

    private static Logger logger = Logger.getLogger(PrintTickSubscriber.class.getName());

    public void update(Tick tick) {

        logger.info(tick);
    }
}
