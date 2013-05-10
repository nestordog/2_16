/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package com.algoTrader.util;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

/**
 * Factory for {@link MyLogger}.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class MyLoggerFactory implements LoggerFactory {

    /**
     * The constructor should be public as it will be called by configurators in
     * different packages.
     */
    public MyLoggerFactory() {

        String commandLineLevel = System.getProperty("logLevel");

        if (commandLineLevel != null) {
            Level level = Level.toLevel(commandLineLevel);
            LogManager.getRootLogger().setLevel(level);
        }
    }

    @Override
    public Logger makeNewLoggerInstance(String name) {
        return new MyLogger(name);
    }
}
