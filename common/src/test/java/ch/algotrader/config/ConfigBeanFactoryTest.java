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
 */
public class ConfigBeanFactoryTest {

    private Map<String, String> map;

    @Before
    public void setup() {
        this.map = new HashMap<>();
        this.map.put("dataSource.dataSet", "someDataSet");
        this.map.put("dataSource.dataSetType", "BAR");
        this.map.put("dataSource.dataSetLocation", "stuff/more-stuff");
        this.map.put("dataSource.barSize", "MIN_5");
        this.map.put("dataSource.feedCSV", "false");
        this.map.put("dataSource.feedDB", "false");
        this.map.put("dataSource.feedGenericEvents", "true");
        this.map.put("dataSource.feedAllMarketDataFiles", "true");
        this.map.put("dataSource.feedBatchSize", "20");
        this.map.put("report.reportLocation", "stuff/report-stuff");
        this.map.put("simulation", "true");
        this.map.put("simulation.initialBalance", "500.5");
        this.map.put("simulation.logTransactions", "true");
        this.map.put("misc.embedded", "true");
        this.map.put("misc.portfolioBaseCurrency", "EUR");
        this.map.put("misc.portfolioDigits", "5");
        this.map.put("misc.defaultAccountName", "IB_NATIVE_TEST");
        this.map.put("misc.validateCrossedSpread", "true");
        this.map.put("misc.displayClosedPositions", "true");
    }

    @Test
    public void testCommonConfigConstruction() throws Exception {

        ConfigProvider configProvider = new DefaultConfigProvider(this.map);
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
        Assert.assertEquals(true, atConfig.isValidateCrossedSpread());
        Assert.assertEquals(true, atConfig.isDisplayClosedPositions());
    }

    @Test(expected = ConfigBeanCreationException.class)
    public void testCommonConfigConstructionMissingParam() throws Exception {

        this.map.remove("dataSource.dataSet");
        ConfigProvider configProvider = new DefaultConfigProvider(this.map);
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
        final Set<String> keys = new LinkedHashSet<>(this.map.keySet());//copy map to avoid concurrent modification problems
        for (final String key : keys) {
            this.map.remove(key);
            ConfigProvider configProvider = new DefaultConfigProvider(this.map);
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
