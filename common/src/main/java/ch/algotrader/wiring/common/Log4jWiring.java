/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.wiring.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.util.log4j.Log4JRewriter;
import ch.algotrader.util.log4j.LogLevelSetter;

/**
 * Log4j configuration.
 */
@Configuration
public class Log4jWiring {

    @Bean(name = "logLevelSetter", initMethod = "init")
    public LogLevelSetter createLogLevelSetter() {

        return new LogLevelSetter();
    }

    @Bean(name = "log4JRewriter")
    public Log4JRewriter createLog4JRewriter(final CommonConfig commonConfig, final EngineManager engineManager) {

        return new Log4JRewriter(engineManager, commonConfig);
    }

}

