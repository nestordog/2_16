/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.util;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;

import ch.algotrader.ServiceLocator;
import ch.algotrader.esper.EngineLocator;

/**
 * Custom Log4J Logger that replaces the System Time with the current Esper Time.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
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

        // in simulation get date from the Esper Engine belonging to the startedStrateggy
        if (ServiceLocator.instance().isInitialized()) {
            if (ServiceLocator.instance().getConfiguration().getSimulation()) {
                String strategyName = ServiceLocator.instance().getConfiguration().getStartedStrategyName();
                if (EngineLocator.instance().hasEngine(strategyName)) {

                    long engineTime = EngineLocator.instance().getEngine(strategyName).getCurrentTime();
                    if (engineTime != 0) {

                        callAppenders(new LoggingEvent(fqcn, this, engineTime, level, message, t));
                        return;
                    }
                }
            }
        }

        // fall back to default behaviour
        super.forcedLog(fqcn, level, message, t);
    }
}
