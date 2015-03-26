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
package ch.algotrader.config;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.config.spring.DefaultConfigProvider;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.MarketDataType;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class ConfigBeanFactoryTest {

    private Map<String, String> map;

    @Before
    public void setup() {
        map = new HashMap<>();
        map.put("dataSource.dataSet", "someDataSet");
        map.put("dataSource.dataSetType", "BAR");
        map.put("dataSource.dataSetLocation", "stuff/more-stuff");
        map.put("dataSource.barSize", "MIN_5");
        map.put("dataSource.feedCSV", "false");
        map.put("dataSource.feedDB", "false");
        map.put("dataSource.feedGenericEvents", "true");
        map.put("dataSource.feedAllMarketDataFiles", "true");
        map.put("dataSource.feedBatchSize", "20");
        map.put("report.reportLocation", "stuff/report-stuff");
        map.put("simulation", "true");
        map.put("simulation.initialBalance", "500.5");
        map.put("simulation.logTransactions", "true");
        map.put("misc.embedded", "true");
        map.put("misc.portfolioDigits", "5");
        map.put("misc.portfolioBaseCurrency", "EUR");
        map.put("misc.initialMarginMarkup", "1.52");
        map.put("misc.validateCrossedSpread", "true");
        map.put("misc.displayClosedPositions", "true");
    }

    @Test
    public void testCommonConfigConstruction() throws Exception {

        ConfigProvider configProvider = new DefaultConfigProvider(map);
        ConfigParams configParams = new ConfigParams(configProvider);

        ConfigBeanFactory factory = new ConfigBeanFactory();
        CommonConfig atConfig = factory.create(configParams, CommonConfig.class);
        Assert.assertNotNull(atConfig);
        Assert.assertEquals("someDataSet", atConfig.getDataSet());
        Assert.assertEquals(MarketDataType.BAR, atConfig.getDataSetType());
        Assert.assertEquals(new File("stuff/more-stuff"), atConfig.getDataSetLocation());
        Assert.assertEquals(Duration.MIN_5, atConfig.getBarSize());
        Assert.assertEquals(true, atConfig.isFeedGenericEvents());
        Assert.assertEquals(true, atConfig.isFeedAllMarketDataFiles());
        Assert.assertEquals(new File("stuff/report-stuff"), atConfig.getReportLocation());
        Assert.assertTrue(atConfig.isSimulation());
        Assert.assertEquals(new BigDecimal("500.5"), atConfig.getSimulationInitialBalance());
        Assert.assertEquals(true, atConfig.isSimulationLogTransactions());
        Assert.assertTrue(atConfig.isEmbedded());
        Assert.assertEquals(5, atConfig.getPortfolioDigits());
        Assert.assertEquals(Currency.EUR, atConfig.getPortfolioBaseCurrency());
        Assert.assertEquals(new BigDecimal("1.52"), atConfig.getInitialMarginMarkup());
        Assert.assertEquals(true, atConfig.isValidateCrossedSpread());
        Assert.assertEquals(true, atConfig.isDisplayClosedPositions());
    }

    @Test(expected = ConfigBeanCreationException.class)
    public void testCommonConfigConstructionMissingParam() throws Exception {

        map.remove("dataSource.dataSet");
        ConfigProvider configProvider = new DefaultConfigProvider(map);
        ConfigParams configParams = new ConfigParams(configProvider);

        ConfigBeanFactory factory = new ConfigBeanFactory();
        try {
            factory.create(configParams, CommonConfig.class);
        } catch (ConfigBeanCreationException ex) {
            Assert.assertEquals("Config parameter 'dataSource.dataSet' is undefined", ex.getMessage());
            throw ex;
        }

    }

    @Test
    public void testCommonConfigConstructionAllParamsRequired() throws Exception {
        final Set<String> keys = new LinkedHashSet<>(map.keySet());//copy map to avoid concurrent modification problems
        for (final String key : keys) {
            map.remove(key);
            ConfigProvider configProvider = new DefaultConfigProvider(map);
            ConfigParams configParams = new ConfigParams(configProvider);

            ConfigBeanFactory factory = new ConfigBeanFactory();
            try {
                factory.create(configParams, CommonConfig.class);
            } catch (ConfigBeanCreationException ex) {
                Assert.assertEquals("Config parameter '" + key + "' is undefined", ex.getMessage());
            }
            setup();//re-construct the original map
        }

    }
}
