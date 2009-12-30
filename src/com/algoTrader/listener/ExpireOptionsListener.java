package com.algoTrader.listener;

import java.util.Date;

import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.StatementAwareUpdateListener;

public class ExpireOptionsListener implements StatementAwareUpdateListener {

    private static Logger logger = Logger.getLogger(ExpireOptionsListener.class.getName());

    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement statement,
            EPServiceProvider epServiceProvider) {

        ServiceLocator.instance().getTransactionService().expireOptions();

        logger.info(new Date(epServiceProvider.getEPRuntime().getCurrentTime()) + " expired options");
    }

}
