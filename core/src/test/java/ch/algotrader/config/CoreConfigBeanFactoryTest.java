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

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.config.spring.DefaultConfigProvider;
import ch.algotrader.enumeration.FeedType;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class CoreConfigBeanFactoryTest {

    private Map<String, String> map;

    @Before
    public void setup() {
        this.map = new HashMap<>();
        this.map.put("statement.simulateOptions", "true");
        this.map.put("statement.simulateFuturesByUnderlying", "true");
        this.map.put("statement.simulateFuturesByGenericFutures", "true");
        this.map.put("misc.transactionDisplayCount", "20");
        this.map.put("misc.intervalDays", "4");
        this.map.put("misc.rebalanceMinAmount", "1000");
        this.map.put("misc.defaultFeedType", "IB");
        this.map.put("misc.defaultOrderPreference", "FX");
        this.map.put("misc.positionCheckDisabled", "false");
        this.map.put("fx.futureHedgeEnabled", "true");
        this.map.put("fx.futureHedgeMinTimeToExpiration", "604800000");
        this.map.put("fx.hedgeMinAmount", "8000");
        this.map.put("fx.hedgeBatchSize", "100");
        this.map.put("fx.hedgeOrderPreference", "FX");
        this.map.put("delta.hedgeMinTimeToExpiration", "5");
        this.map.put("delta.hedgeOrderPreference", "FUT");
    }

    @Test
    public void testCoreConfigConstruction() throws Exception {

        ConfigProvider configProvider = new DefaultConfigProvider(this.map);
        ConfigParams configParams = new ConfigParams(configProvider);

        ConfigBeanFactory factory = new ConfigBeanFactory();
        CoreConfig coreConfig = factory.create(configParams, CoreConfig.class);
        Assert.assertNotNull(coreConfig);
        Assert.assertEquals(true, coreConfig.isSimulateOptions());
        Assert.assertEquals(true, coreConfig.isSimulateFuturesByUnderlying());
        Assert.assertEquals(true, coreConfig.isSimulateFuturesByGenericFutures());
        Assert.assertEquals(20, coreConfig.getTransactionDisplayCount());
        Assert.assertEquals(4, coreConfig.getIntervalDays());
        Assert.assertEquals(FeedType.IB.name(), coreConfig.getDefaultFeedType());
        Assert.assertEquals(true, coreConfig.isFxFutureHedgeEnabled());
        Assert.assertEquals(604800000, coreConfig.getFxFutureHedgeMinTimeToExpiration());
        Assert.assertEquals(8000, coreConfig.getFxHedgeMinAmount());
        Assert.assertEquals(100, coreConfig.getFxHedgeBatchSize());
        Assert.assertEquals(5, coreConfig.getDeltaHedgeMinTimeToExpiration());
        Assert.assertEquals(false, coreConfig.isPositionCheckDisabled());
    }

    @Test(expected = ConfigBeanCreationException.class)
    public void testCoreConfigConstructionMissingParam() throws Exception {

        this.map.remove("statement.simulateOptions");
        ConfigProvider configProvider = new DefaultConfigProvider(this.map);
        ConfigParams configParams = new ConfigParams(configProvider);

        ConfigBeanFactory factory = new ConfigBeanFactory();
        try {
            factory.create(configParams, CoreConfig.class);
        } catch (ConfigBeanCreationException ex) {
            Assert.assertEquals("Config parameter 'statement.simulateOptions' is undefined", ex.getMessage());
            throw ex;
        }

    }

}
