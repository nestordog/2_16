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
package ch.algotrader.util.spring;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

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
        if (levelName != null && !"".equals(levelName)) {
            Level level = Level.toLevel(levelName); // defaults to DEBUG
            if (levelName.toUpperCase().equals(level.toString())) {
                LoggerContext coreLoggerContext = (LoggerContext) LogManager.getContext(false);
                Configuration configuration = coreLoggerContext.getConfiguration();
                LoggerConfig rootLoggerConfig = configuration.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
                rootLoggerConfig.setLevel(level);
                coreLoggerContext.updateLoggers();
            } else {
                throw new IllegalStateException("unrecognized log4j log level " + levelName);
            }
        }
    }
}
