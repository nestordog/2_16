package com.algoTrader.util;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.TriggeringEventEvaluator;

public class CustomEvaluator implements TriggeringEventEvaluator {

    @Override
    public boolean isTriggeringEvent(LoggingEvent event) {
        return event.getLevel().isGreaterOrEqual(Level.INFO);
    }
}
