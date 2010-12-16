package com.algoTrader.service;

import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.util.MyLogger;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public abstract class AccountServiceImpl extends AccountServiceBase {

    private static Logger logger = MyLogger.getLogger(AccountServiceImpl.class.getName());

    public static class ProcessCashTransactionsListener implements UpdateListener {

        public void update(EventBean[] newEvents, EventBean[] oldEvents) {

            long startTime = System.currentTimeMillis();
            logger.debug("retrieveTicks start");

            ServiceLocator.serverInstance().getDispatcherService().getAccountService().processCashTransactions();

            logger.debug("retrieveTicks end (" + (System.currentTimeMillis() - startTime) + "ms execution)");

        }
    }
}
