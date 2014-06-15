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

import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.support.DefaultConversionService;

import ch.algotrader.config.ConfigProvider;
import ch.algotrader.enumeration.Currency;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class DefaultConfigProviderTest {

    @Test
    public void testBasicTypeConversion() throws Exception {

        Map<String, String> map = new HashMap<String, String>();
        map.put("int.stuff", "5");
        map.put("url.stuff", "http://localhost/stuff");

        ConfigProvider configProvider = new DefaultConfigProvider(map, new DefaultConversionService());
        Assert.assertEquals(Integer.valueOf(5), configProvider.getParameter("int.stuff", Integer.class));
        Assert.assertEquals(new BigDecimal("5"), configProvider.getParameter("int.stuff", BigDecimal.class));
        Assert.assertEquals(new URL("http://localhost/stuff"), configProvider.getParameter("url.stuff", URL.class));
        Assert.assertEquals(new URI("http://localhost/stuff"), configProvider.getParameter("url.stuff", URI.class));
    }

    @Test(expected=ConversionFailedException.class)
    public void testInvalidTypeConversion1() throws Exception {

        Map<String, String> map = new HashMap<String, String>();
        map.put("int.stuff", "crap");

        ConfigProvider configProvider = new DefaultConfigProvider(map, new DefaultConversionService());
        configProvider.getParameter("int.stuff", Integer.class);
    }

    @Test(expected=ConversionFailedException.class)
    public void testInvalidTypeConversion2() throws Exception {

        Map<String, String> map = new HashMap<String, String>();
        map.put("url.stuff", "^&*(() sdfs sf");

        ConfigProvider configProvider = new DefaultConfigProvider(map, new DefaultConversionService());
        configProvider.getParameter("url.stuff", URI.class);
    }

    @Test
    public void testCurrencyTypeConversion() throws Exception {

        Map<String, String> map = new HashMap<String, String>();
        map.put("some.currency", "USD");

        ConfigProvider configProvider = new DefaultConfigProvider(map, new DefaultConversionService());
        Assert.assertEquals(Currency.USD, configProvider.getParameter("some.currency", Currency.class));
    }

    @Test(expected=ConversionFailedException.class)
    public void testInvalidTypeConversion3() throws Exception {

        Map<String, String> map = new HashMap<String, String>();
        map.put("some.currency", "CRAP");

        ConfigProvider configProvider = new DefaultConfigProvider(map, new DefaultConversionService());
        configProvider.getParameter("some.currency", Currency.class);
    }

}
