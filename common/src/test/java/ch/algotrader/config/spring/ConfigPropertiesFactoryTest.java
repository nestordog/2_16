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
package ch.algotrader.config.spring;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import ch.algotrader.config.ConfigParams;
import ch.algotrader.config.ConfigProvider;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class ConfigPropertiesFactoryTest {

    @Test
    public void testBasicGetObject() throws Exception {

        ConfigProvider configProvider = Mockito.mock(ConfigProvider.class);
        ConfigParams configParams = new ConfigParams(configProvider);
        ConfigPropertiesFactoryBean factoryBean = new ConfigPropertiesFactoryBean(configParams);

        Mockito.when(configProvider.getNames()).thenReturn(new HashSet<String>(Arrays.asList("p1", "p2")));
        Mockito.when(configProvider.getParameter("p1", String.class)).thenReturn("blah");
        Mockito.when(configProvider.getParameter("p2", String.class)).thenReturn(null);

        Properties properties = factoryBean.getObject();
        Assert.assertNotNull(properties);
        Assert.assertEquals("blah", properties.get("p1"));
        Assert.assertEquals(null, properties.get("p2"));
        Assert.assertEquals(null, properties.get("p3"));
    }

}
