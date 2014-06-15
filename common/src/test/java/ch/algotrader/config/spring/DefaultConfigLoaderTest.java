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

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import ch.algotrader.util.Consts;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class DefaultConfigLoaderTest {

    @Mock
    private ResourcePatternResolver resourceResolver;

    private DefaultConfigLoader impl;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);
        impl = new DefaultConfigLoader(this.resourceResolver);
    }

    private static Resource createResource(final String s) {

        if (s == null) {

            return null;
        }
        return new ByteArrayResource(s.getBytes(Consts.ISO_8859_1));
    }

    @Test
    public void testResolveModuleHierarchy() throws Exception {

        Mockito.when(this.resourceResolver.getResource("classpath:META-INF/blah.hierarchy")).thenReturn(createResource("\r\n1:2:3:blah"));

        String[] modules = impl.resolveModuleHierarchy("blah");

        Assert.assertNotNull(modules);
        Assert.assertArrayEquals(new String[]{"1", "2", "3", "blah"}, modules);
    }

    @Test
    public void testResolveModuleHierarchySingle() throws Exception {

        Mockito.when(this.resourceResolver.getResource("classpath:META-INF/blah.hierarchy")).thenReturn(createResource("blah"));

        String[] modules = impl.resolveModuleHierarchy("blah");

        Assert.assertNotNull(modules);
        Assert.assertArrayEquals(new String[] {"blah"}, modules);
    }

    @Test
    public void testResolveModuleHierarchyEmpty() throws Exception {

        Mockito.when(this.resourceResolver.getResource("classpath:META-INF/blah.hierarchy")).thenReturn(createResource(""));

        String[] modules = impl.resolveModuleHierarchy("blah");

        Assert.assertNull(modules);
    }

    @Test
    public void testResolveModuleHierarchyNotFound() throws Exception {

        Mockito.when(this.resourceResolver.getResource("classpath:META-INF/blah.hierarchy")).thenReturn(null);

        String[] modules = impl.resolveModuleHierarchy("blah");

        Assert.assertNull(modules);
    }

    @Test
    public void testLoadResource() throws Exception {

        Map<String, String> map = new HashMap<String, String>();
        impl.loadResource(map, createResource("blah = blah\r\nyada = yada\r\n#stuff = stuff\r\n"));

        Assert.assertEquals("blah", map.get("blah"));
        Assert.assertEquals("yada", map.get("yada"));
        Assert.assertEquals(null, map.get("stuff"));
    }

    @Test
    public void testLoadResourceParameterOverride() throws Exception {

        Map<String, String> map = new HashMap<String, String>();
        map.put("blah", "stuff");
        map.put("yada", "stuff");
        map.put("stuff", "stuff");
        impl.loadResource(map, createResource("blah = blah\r\nyada = yada\r\n#stuff = stuff\r\n"));

        Assert.assertEquals("blah", map.get("blah"));
        Assert.assertEquals("yada", map.get("yada"));
        Assert.assertEquals("stuff", map.get("stuff"));
    }

}
