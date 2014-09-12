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
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.config.spring.DefaultConfigProvider;
import ch.algotrader.enumeration.FeedType;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class CoreConfigBeanFactoryTest {

    private Map<String, String> map;

    @Before
    public void setup() {
        map = new HashMap<String, String>();
        map.put("statement.simulateOptions", "true");
        map.put("statement.simulateFuturesByUnderlying", "true");
        map.put("statement.simulateFuturesByGenericFutures", "true");
        map.put("misc.transactionDisplayCount", "20");
        map.put("misc.intervalDays", "4");
        map.put("misc.rebalanceMinAmount", "1000");
        map.put("misc.defaultFeedType", "IB");
        map.put("fx.futureHedgeEnabled", "true");
        map.put("fx.futureHedgeMinTimeToExpiration", "604800000");
        map.put("fx.hedgeMinAmount", "8000");
        map.put("fx.hedgeBatchSize", "100");
        map.put("delta.hedgeMinTimeToExpiration", "5");
    }

    @Test
    public void testCoreConfigConstruction() throws Exception {

        ConfigProvider configProvider = new DefaultConfigProvider(map);
        ConfigParams configParams = new ConfigParams(configProvider);

        ConfigBeanFactory factory = new ConfigBeanFactory();
        CoreConfig coreConfig = factory.create(configParams, CoreConfig.class);
        Assert.assertNotNull(coreConfig);
        Assert.assertEquals(true, coreConfig.isSimulateOptions());
        Assert.assertEquals(true, coreConfig.isSimulateFuturesByUnderlying());
        Assert.assertEquals(true, coreConfig.isSimulateFuturesByGenericFutures());
        Assert.assertEquals(20, coreConfig.getTransactionDisplayCount());
        Assert.assertEquals(4, coreConfig.getIntervalDays());
        Assert.assertEquals(new BigDecimal("1000"), coreConfig.getRebalanceMinAmount());
        Assert.assertEquals(FeedType.IB, coreConfig.getDefaultFeedType());
        Assert.assertEquals(true, coreConfig.isFxFutureHedgeEnabled());
        Assert.assertEquals(604800000, coreConfig.getFxFutureHedgeMinTimeToExpiration());
        Assert.assertEquals(8000, coreConfig.getFxHedgeMinAmount());
        Assert.assertEquals(100, coreConfig.getFxHedgeBatchSize());
        Assert.assertEquals(5, coreConfig.getDeltaHedgeMinTimeToExpiration());
    }

    @Test(expected = ConfigBeanCreationException.class)
    public void testCoreConfigConstructionMissingParam() throws Exception {

        map.remove("statement.simulateOptions");
        ConfigProvider configProvider = new DefaultConfigProvider(map);
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
