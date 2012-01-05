package com.algoTrader.util;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;

import com.algoTrader.ServiceLocator;
import com.algoTrader.service.RuleService;

public class MyLogger extends Logger {

    // It's usually a good idea to add a dot suffix to the fully
    // qualified class name. This makes caller localization to work
    // properly even from classes that have almost the same fully
    // qualified class name as MyLogger, e.g. MyLoggerTest.
    static String FQCN = MyLogger.class.getName() + ".";

    // It's enough to instantiate a factory once and for all.
    private static MyLoggerFactory myFactory = new MyLoggerFactory();

    /**
     * Just calls the parent constuctor.
     */
    public MyLogger(String name) {
        super(name);
    }

    /**
     * This method overrides {@link Logger#getLogger} by supplying its own
     * factory type as a parameter.
     */
    public static Logger getLogger(String name) {
        return Logger.getLogger(name, myFactory);
    }

    /**
     * tries to get Esper-Time
     */
    @Override
    protected void forcedLog(String fqcn, Priority level, Object message, Throwable t) {

        if (ServiceLocator.instance().isInitialized()) {
            RuleService ruleService = ServiceLocator.instance().getRuleService();

            String strategyName = StrategyUtil.getStartedStrategyName();
            if (ruleService.isInitialized(strategyName) && !ruleService.isInternalClock(strategyName)) {

                long engineTime = ruleService.getCurrentTime(strategyName);
                if (engineTime != 0) {

                    callAppenders(new LoggingEvent(fqcn, this, engineTime, level, message, t));
                    return;
                }
            }
        }

        // fall back to default behaviour
        super.forcedLog(fqcn, level, message, t);
    }
}
