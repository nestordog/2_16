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
package ch.algotrader.starter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.ServiceLocator;
import ch.algotrader.config.ConfigLocator;
import ch.algotrader.config.ConfigParams;

/**
 * Abstract Base Class for starting the AlgoTrader Server in Live Trading Mode
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public abstract class StrategyStarter {

    private static final Logger LOGGER = LogManager.getLogger(StrategyStarter.class);

    private static final String ACTIVEMQ_SERIALIZABLE_PACKAGES = "org.apache.activemq.SERIALIZABLE_PACKAGES";

    public static void main(String[] args) throws Exception {

        // Compensate for ActiveMQ security configuration awkwardness
        if (System.getProperty(ACTIVEMQ_SERIALIZABLE_PACKAGES) == null) {
            System.setProperty(ACTIVEMQ_SERIALIZABLE_PACKAGES,
                    "java.lang,java.math,javax.security,java.util,org.apache.activemq,ch.algotrader");
        }

        ConfigParams configParams = ConfigLocator.instance().getConfigParams();
        String strategyName = configParams.getString("strategyName");

        ServiceLocator serviceLocator = ServiceLocator.instance();
        try {
            LOGGER.info("Starting {} strategy in distributed mode", strategyName);
            serviceLocator.runStrategy();
            LOGGER.info("{} strategy started", strategyName);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOGGER.info("{} strategy terminated", strategyName);
            }));
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException ignore) {
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            serviceLocator.shutdown();
        }
    }

}
