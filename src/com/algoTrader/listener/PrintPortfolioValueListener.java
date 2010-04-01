package com.algoTrader.listener;

import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.util.MyLogger;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.StatementAwareUpdateListener;

public class PrintPortfolioValueListener implements StatementAwareUpdateListener {

    private static Logger logger = MyLogger.getLogger(PrintPortfolioValueListener.class.getName());

    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement statement, EPServiceProvider epServiceProvider) {

        logger.debug(ServiceLocator.instance().getLookupService().getPortfolioValueAllAccounts());
    }
}
