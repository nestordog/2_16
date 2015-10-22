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

import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class ConfigParamsTest {

    @Mock
    private ConfigProvider configProvider;

    private ConfigParams impl;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);
        impl = new ConfigParams(configProvider);
    }

    @Test
    public void testGetStringParam() {

        Mockito.when(configProvider.getParameter("stuff", String.class)).thenReturn("blah");

        Assert.assertEquals("blah", impl.getString("stuff"));
        Assert.assertEquals(null, impl.getString("other stuff"));
        Assert.assertEquals("yada", impl.getString("other stuff", "yada"));
    }

    @Test
    public void testGetIntegerParam() {

        Mockito.when(configProvider.getParameter("stuff", Integer.class)).thenReturn(3);

        Assert.assertEquals(Integer.valueOf(3), impl.getInteger("stuff"));
        Assert.assertEquals(null, impl.getInteger("other stuff"));
        Assert.assertEquals(5, impl.getInteger("other stuff", 5));
    }

    @Test
    public void testGetLongParam() {

        Mockito.when(configProvider.getParameter("stuff", Long.class)).thenReturn(3L);

        Assert.assertEquals(Long.valueOf(3), impl.getLong("stuff"));
        Assert.assertEquals(null, impl.getLong("other stuff"));
        Assert.assertEquals(5L, impl.getLong("other stuff", 5L));
    }

    @Test
    public void testGetBooleanParam() {

        Mockito.when(configProvider.getParameter("stuff", Boolean.class)).thenReturn(true);

        Assert.assertEquals(Boolean.TRUE, impl.getBoolean("stuff"));
        Assert.assertEquals(null, impl.getBoolean("other stuff"));
        Assert.assertEquals(false, impl.getBoolean("other stuff", false));
    }

    @Test
    public void testGetDoubleParam() {

        Mockito.when(configProvider.getParameter("stuff", Double.class)).thenReturn(3.0d);

        Assert.assertEquals(Double.valueOf(3.0d), impl.getDouble("stuff"));
        Assert.assertEquals(null, impl.getDouble("other stuff"));
        Assert.assertEquals(5.0d, impl.getDouble("other stuff", 5.0d), 0.1d);
    }

    @Test
    public void testGetBigDecimalParam() {

        Mockito.when(configProvider.getParameter("stuff", BigDecimal.class)).thenReturn(new BigDecimal("3.0"));

        Assert.assertEquals(BigDecimal.valueOf(3.0d), impl.getBigDecimal("stuff"));
        Assert.assertEquals(null, impl.getBigDecimal("other stuff"));
        Assert.assertEquals(new BigDecimal("5.0"), impl.getBigDecimal("other stuff", new BigDecimal("5.0")));
    }

    @Test
    public void testGetURLParam() throws Exception {

        Mockito.when(configProvider.getParameter("stuff", URL.class)).thenReturn(new URL("http://stuff/"));

        Assert.assertEquals(new URL("http://stuff/"), impl.getURL("stuff"));
        Assert.assertEquals(null, impl.getURL("other stuff"));
        Assert.assertEquals(new URL("http://localhost/"), impl.getURL("other stuff", new URL("http://localhost/")));
    }

    @Test
    public void testGetURIParam() throws Exception {

        Mockito.when(configProvider.getParameter("stuff", URI.class)).thenReturn(new URI("http://stuff/"));

        Assert.assertEquals(new URI("http://stuff/"), impl.getURI("stuff"));
        Assert.assertEquals(null, impl.getURI("other stuff"));
        Assert.assertEquals(new URI("http://localhost/"), impl.getURI("other stuff", new URI("http://localhost/")));
    }

}
