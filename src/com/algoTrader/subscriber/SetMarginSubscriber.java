package com.algoTrader.subscriber;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Position;

public class SetMarginSubscriber {

    private static Logger logger = Logger.getLogger(SetMarginSubscriber.class.getName());

    public void update(Date date, Position position, BigDecimal settlement, BigDecimal underlaying) {

        ServiceLocator serviceLocator = ServiceLocator.instance();
        serviceLocator.getTransactionService().setMargin(position, settlement, underlaying);

        logger.info(date + " setMargin for " + position);
    }
}
