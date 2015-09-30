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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;

/**
 * Configuration resource loader.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public final class ConfigLoader {

    private ConfigLoader() {
    }

    static void loadResource(final Map<String, String> paramMap, final Resource resource) throws IOException {

        try (InputStream inputStream = resource.getInputStream()) {
            Properties props = new Properties();
            props.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            for (Map.Entry<Object, Object> entry : props.entrySet()) {
                String paramName = (String) entry.getKey();
                String paramValue = (String) entry.getValue();
                if (StringUtils.isNotBlank(paramName)) {
                    paramMap.put(paramName, paramValue);
                }
            }

        }
    }

    static void loadResources(final Map<String, String> paramMap, final Resource... resources) throws IOException {
        for (Resource resource: resources) {
            loadResource(paramMap, resource);
        }
    }

    public static Map<String, String> loadResources(final Resource... resources) throws IOException {

        // Load common and core parameters
        Map<String, String> paramMap = new LinkedHashMap<>();
        for (Resource resource: resources) {
            loadResource(paramMap, resource);
        }
        return paramMap;
    }

    /**
     * Loads system parameters.
     * <p/>
     * <p><tt>/conf.properties</tt><p/>
     * <p><tt>/conf-core.properties</tt><p/>
     * <p><tt>/conf-fix.properties</tt><p/>
     * <p><tt>/conf-ib.properties</tt><p/>
     * <p><tt>/conf-bb.properties</tt><p/>
     * <p/>
     */
    public static Map<String, String> load(final ResourceLoader resourceResolver) throws IOException {

        Assert.notNull(resourceResolver, "ResourcePatternResolver is null");

        Map<String, String> paramMap = new LinkedHashMap<>();
        String[] resourceNames = new String[] { "conf.properties", "conf-core.properties", "conf-fix.properties", "conf-ib.properties", "conf-bb.properties" };
        for (String resourceName : resourceNames) {
            Resource resource = resourceResolver.getResource("classpath:/" + resourceName);
            if (resource != null && resource.exists()) {
                ConfigLoader.loadResource(paramMap, resource);
            }
        }
        return paramMap;
    }

}
