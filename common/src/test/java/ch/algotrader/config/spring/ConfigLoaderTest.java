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

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class ConfigLoaderTest {

    private static Resource createResource(final String s) {

        if (s == null) {

            return null;
        }
        return new ByteArrayResource(s.getBytes(StandardCharsets.ISO_8859_1));
    }

    @Test
    public void testLoadResource() throws Exception {

        Map<String, String> map = new HashMap<>();
        ConfigLoader.loadResource(map, createResource("blah = blah\r\nyada = yada\r\n#stuff = stuff\r\n"));

        Assert.assertEquals("blah", map.get("blah"));
        Assert.assertEquals("yada", map.get("yada"));
        Assert.assertEquals(null, map.get("stuff"));
    }

    @Test
    public void testLoadResourceParameterOverride() throws Exception {

        Map<String, String> map = new HashMap<>();
        map.put("blah", "stuff");
        map.put("yada", "stuff");
        map.put("stuff", "stuff");
        ConfigLoader.loadResource(map, createResource("blah = blah\r\nyada = yada\r\nstuff = stuff\r\n"));

        Assert.assertEquals("blah", map.get("blah"));
        Assert.assertEquals("yada", map.get("yada"));
        Assert.assertEquals("stuff", map.get("stuff"));
    }

    @Test
    public void testLoadMultipleResourcesParameterOverride() throws Exception {

        Map<String, String> map = new HashMap<>();
        ConfigLoader.loadResources(map,
                createResource("blah = stuff\r\nyada = stuff\r\nstuff = stuff\r\n"),
                createResource("blah = blah\r\nyada = yada\r\n"));

        Assert.assertEquals("blah", map.get("blah"));
        Assert.assertEquals("yada", map.get("yada"));
        Assert.assertEquals("stuff", map.get("stuff"));
    }

}
