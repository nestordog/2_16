package com.algoTrader.listener;

import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Account;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.StatementAwareUpdateListener;

public class PrintPortfolioValueListener  implements StatementAwareUpdateListener {


    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement statement,
            EPServiceProvider epServiceProvider) {

        Logger logger = MyLogger.getLogger(PrintPortfolioValueListener.class.getName());


        Account[] accounts = ServiceLocator.instance().getLookupService().getAllAccounts();

        double portfolioValue = 0;
        for (Account account : accounts) {
            portfolioValue += account.getPortfolioValue().doubleValue();
        }

        logger.info(RoundUtil.getBigDecimal(portfolioValue));
    }
}
