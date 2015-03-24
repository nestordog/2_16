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

import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.convert.ConversionFailedException;

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

        Map<String, String> map = new HashMap<>();
        map.put("int.stuff", "5");
        map.put("url.stuff", "http://localhost/stuff");
        map.put("date.stuff", "2014-02-02");
        map.put("datetime.stuff", "2014-02-02 11:12:13");

        ConfigProvider configProvider = new DefaultConfigProvider(map);
        Assert.assertEquals(Integer.valueOf(5), configProvider.getParameter("int.stuff", Integer.class));
        Assert.assertEquals(new BigDecimal("5"), configProvider.getParameter("int.stuff", BigDecimal.class));
        Assert.assertEquals(new URL("http://localhost/stuff"), configProvider.getParameter("url.stuff", URL.class));
        Assert.assertEquals(new URI("http://localhost/stuff"), configProvider.getParameter("url.stuff", URI.class));
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Assert.assertEquals(dateFormat.parse("2014-02-02 00:00:00"), configProvider.getParameter("date.stuff", Date.class));
        Assert.assertEquals(dateFormat.parse("2014-02-02 11:12:13"), configProvider.getParameter("datetime.stuff", Date.class));
    }

    @Test
    public void testTypeConversionNullValues() throws Exception {

        Map<String, String> map = new HashMap<>();
        ConfigProvider configProvider = new DefaultConfigProvider(map);
        Assert.assertEquals(null, configProvider.getParameter("int.stuff", BigDecimal.class));
        Assert.assertEquals(null, configProvider.getParameter("url.stuff", URL.class));
        Assert.assertEquals(null, configProvider.getParameter("file.stuff", File.class));
    }

    @Test
    public void testTypeConversionEmptyValues() throws Exception {

        Map<String, String> map = new HashMap<>();
        map.put("int.stuff", "");
        map.put("url.stuff", "");
        map.put("file.stuff", "");

        ConfigProvider configProvider = new DefaultConfigProvider(map);
        Assert.assertEquals(null, configProvider.getParameter("int.stuff", BigDecimal.class));
        Assert.assertEquals(null, configProvider.getParameter("url.stuff", URL.class));
        Assert.assertEquals(null, configProvider.getParameter("file.stuff", File.class));
    }

    @Test(expected=ConversionFailedException.class)
    public void testInvalidTypeConversion1() throws Exception {

        Map<String, String> map = new HashMap<>();
        map.put("int.stuff", "crap");

        ConfigProvider configProvider = new DefaultConfigProvider(map);
        configProvider.getParameter("int.stuff", Integer.class);
    }

    @Test(expected=ConversionFailedException.class)
    public void testInvalidTypeConversion2() throws Exception {

        Map<String, String> map = new HashMap<>();
        map.put("url.stuff", "^&*(() sdfs sf");

        ConfigProvider configProvider = new DefaultConfigProvider(map);
        configProvider.getParameter("url.stuff", URI.class);
    }

    @Test
    public void testCurrencyTypeConversion() throws Exception {

        Map<String, String> map = new HashMap<>();
        map.put("some.currency", "USD");

        ConfigProvider configProvider = new DefaultConfigProvider(map);
        Assert.assertEquals(Currency.USD, configProvider.getParameter("some.currency", Currency.class));
    }

    @Test(expected=ConversionFailedException.class)
    public void testInvalidTypeConversion3() throws Exception {

        Map<String, String> map = new HashMap<>();
        map.put("some.currency", "CRAP");

        ConfigProvider configProvider = new DefaultConfigProvider(map);
        configProvider.getParameter("some.currency", Currency.class);
    }

}
