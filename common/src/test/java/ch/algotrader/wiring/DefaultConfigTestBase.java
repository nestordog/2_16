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
package ch.algotrader.wiring;

import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.ConfigBeanFactory;
import ch.algotrader.config.ConfigLocator;
import ch.algotrader.config.ConfigParams;
import ch.algotrader.config.spring.DefaultConfigProvider;

public class DefaultConfigTestBase {

    private static Map<String, String> CONFIG_MAP;

    @BeforeClass
    public static void setupConfig() {

        CONFIG_MAP = new HashMap<>();
        CONFIG_MAP.put("dataSource.dataSet", "someDataSet");
        CONFIG_MAP.put("dataSource.dataSetType", "BAR");
        CONFIG_MAP.put("dataSource.dataSetLocation", "stuff/more-stuff");
        CONFIG_MAP.put("dataSource.barSize", "MIN_5");
        CONFIG_MAP.put("dataSource.feedCSV", "false");
        CONFIG_MAP.put("dataSource.feedDB", "false");
        CONFIG_MAP.put("dataSource.feedGenericEvents", "true");
        CONFIG_MAP.put("dataSource.feedAllMarketDataFiles", "true");
        CONFIG_MAP.put("dataSource.feedBatchSize", "20");
        CONFIG_MAP.put("report.reportLocation", "stuff/report-stuff");
        CONFIG_MAP.put("simulation", "true");
        CONFIG_MAP.put("simulation.initialBalance", "500.5");
        CONFIG_MAP.put("simulation.logTransactions", "true");
        CONFIG_MAP.put("simulation.simulateOptions", "false");
        CONFIG_MAP.put("misc.embedded", "true");
        CONFIG_MAP.put("misc.portfolioBaseCurrency", "EUR");
        CONFIG_MAP.put("misc.portfolioDigits", "5");
        CONFIG_MAP.put("misc.defaultAccountName", "IB_NATIVE_TEST");
        CONFIG_MAP.put("misc.validateCrossedSpread", "true");
        CONFIG_MAP.put("misc.displayClosedPositions", "true");

        DefaultConfigProvider configProvider = new DefaultConfigProvider(CONFIG_MAP);
        ConfigParams configParams = new ConfigParams(configProvider);
        CommonConfig commonConfig = new ConfigBeanFactory().create(configParams, CommonConfig.class);
        ConfigLocator.initialize(configParams, commonConfig);
    }

    @AfterClass
    public static void cleanupConfig() {

        ConfigLocator.reset();
    }

}
