package com.algoTrader.subscriber;

import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

public class TestSubscriber {

    private static Logger logger = Logger.getLogger(TestSubscriber.class.getName());

    public void update(Map map) {

        logger.info(map);
    }
}
