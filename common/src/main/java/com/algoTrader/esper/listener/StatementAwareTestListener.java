package com.algoTrader.esper.listener;

import org.apache.log4j.Logger;

import com.algoTrader.util.MyLogger;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.StatementAwareUpdateListener;

public class StatementAwareTestListener implements StatementAwareUpdateListener {

    private static Logger logger = MyLogger.getLogger(StatementAwareTestListener.class.getName());

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement statement, EPServiceProvider epServiceProvider) {
        for (EventBean event : newEvents) {
            logger.info(statement.getName() + " " + event.getUnderlying());
        }
    }
}
