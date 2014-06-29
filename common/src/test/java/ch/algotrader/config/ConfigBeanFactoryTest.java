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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.convert.support.DefaultConversionService;

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

    @Test
    public void testBaseConfigConstruction() throws Exception {

        Map<String, String> map = new HashMap<String, String>();
        map.put("strategyName", "SOME_STRATEGY");
        map.put("dataSource.dataSet", "someDataSet");
        map.put("dataSource.dataSetType", "BAR");
        map.put("dataSource.barSize", "MIN_5");
        map.put("simulation", "true");
        map.put("simulation.initialBalance", "500.5");
        map.put("misc.singleVM", "true");
        map.put("misc.portfolioDigits", "5");
        map.put("misc.portfolioBaseCurrency", "EUR");
        map.put("misc.initialMarginMarkup", "1.52");
        map.put("misc.validateCrossedSpread", "true");

        ConfigProvider configProvider = new DefaultConfigProvider(map, new DefaultConversionService());
        ConfigParams configParams = new ConfigParams(configProvider);

        ConfigBeanFactory factory = new ConfigBeanFactory();
        CommonConfig atConfig = factory.create(configParams, CommonConfig.class);
        Assert.assertNotNull(atConfig);
        Assert.assertEquals("SOME_STRATEGY", atConfig.getStrategyName());
        Assert.assertEquals("someDataSet", atConfig.getDataSet());
        Assert.assertEquals(MarketDataType.BAR, atConfig.getDataSetType());
        Assert.assertEquals(Duration.MIN_5, atConfig.getBarSize());
        Assert.assertTrue(atConfig.isSimulation());
        Assert.assertEquals(new BigDecimal("500.5"), atConfig.getSimulationInitialBalance());
        Assert.assertTrue(atConfig.isSingleVM());
        Assert.assertEquals(5, atConfig.getPortfolioDigits());
        Assert.assertEquals(Currency.EUR, atConfig.getPortfolioBaseCurrency());
        Assert.assertEquals(new BigDecimal("1.52"), atConfig.getInitialMarginMarkup());
        Assert.assertEquals(true, atConfig.isValidateCrossedSpread());
    }

    @Test(expected = ConfigBeanCreationException.class)
    public void testBaseConfigConstructionMissingParam() throws Exception {

        Map<String, String> map = new HashMap<String, String>();
        map.put("strategyName", "SOME_STRATEGY");

        ConfigProvider configProvider = new DefaultConfigProvider(map, new DefaultConversionService());
        ConfigParams configParams = new ConfigParams(configProvider);

        ConfigBeanFactory factory = new ConfigBeanFactory();
        try {
            factory.create(configParams, CommonConfig.class);
        } catch (ConfigBeanCreationException ex) {
            Assert.assertEquals("Config parameter 'dataSource.dataSet' is undefined", ex.getMessage());
            throw ex;
        }

    }

}
