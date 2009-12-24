package com.algoTrader.subscriber;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.log4j.Logger;

import com.algoTrader.entity.Position;

public class SetExitLevelSubscriber {

    private static Logger logger = Logger.getLogger(SetExitLevelSubscriber.class.getName());

    public void update(Date date, Position position, BigDecimal exitValue) {

        position.setExitValue(exitValue);

        logger.info(date + " " + position.getSecurity().getSymbol() + " changed Exit Value to " + exitValue);
    }
}
