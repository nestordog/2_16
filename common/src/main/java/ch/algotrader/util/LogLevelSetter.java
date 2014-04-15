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
package ch.algotrader.util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Sets the log-level based on the commandline argument "logLevel"
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class LogLevelSetter {

    public void init() {

        String levelName = System.getProperty("logLevel");
        Level level = Level.toLevel(levelName); // defaults to DEBUG
        if (!"".equals(levelName) && !levelName.toUpperCase().equals(level.toString())) {
            throw new IllegalStateException("unrecognized log4j log level " + levelName);
        } else {
            Logger.getRootLogger().setLevel(level);
        }
    }
}
