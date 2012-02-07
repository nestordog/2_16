package com.algoTrader.esper.listener;

import org.apache.log4j.Logger;

import com.algoTrader.esper.subscriber.TestSubscriber;
import com.algoTrader.util.MyLogger;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class TestListener implements UpdateListener {

    private static Logger logger = MyLogger.getLogger(TestSubscriber.class.getName());

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents) {
        for (EventBean event : newEvents) {
            logger.info(event);
        }
    }
}
