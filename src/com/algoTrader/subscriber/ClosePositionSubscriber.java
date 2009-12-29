package com.algoTrader.subscriber;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Security;

public class ClosePositionSubscriber {

    private static Logger logger = Logger.getLogger(ClosePositionSubscriber.class.getName());

    public void update(Date date, Security security, BigDecimal current) {

        int quantity = - security.getPosition().getQuantity();

        ServiceLocator serviceLocator = ServiceLocator.instance();
        serviceLocator.getTransactionService().executeTransaction(quantity, security, current);

        serviceLocator.getCepService().stop("closePosition");
        serviceLocator.getCepService().stop("setExitValue");

        logger.info(date + " close position " + security.getSymbol() + " at " + current);

    }
}
