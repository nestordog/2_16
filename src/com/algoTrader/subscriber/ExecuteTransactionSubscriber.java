package com.algoTrader.subscriber;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Security;

public class ExecuteTransactionSubscriber {

    private static Logger logger = Logger.getLogger(ExecuteTransactionSubscriber.class.getName());

    public void update(Date date, Security security, BigDecimal currentValue) {

        logger.info(date + " " + security.getSymbol() + " sell at " + currentValue);

        ServiceLocator serviceLocator = ServiceLocator.instance();
        serviceLocator.getCepService().stop("sellOption");
        serviceLocator.getCepService().stop("setExitValue");
    }
}
