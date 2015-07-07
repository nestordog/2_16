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
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
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
     * Application parameters are resolved in the following sequence:
     * <p><tt>META-INF/conf.properties</tt> classpath resource is loaded if exists<p/>
     * <p><tt>META-INF/conf-core.properties</tt> classpath resource is loaded if exists<p/>
     * <p><tt>META-INF/conf-*.properties</tt> classpath resources matching the pattern are loaded<p/>
     * <p/>
     * Please note that settings of each subsequent resource override those already set from preceding resources.
     * In other words, core settings override common settings, component settings override common and core settings.
     */
    public static Map<String, String> load(final ResourcePatternResolver resourceResolver) throws IOException {

        Assert.notNull(resourceResolver, "ResourcePatternResolver is null");

        Map<String, String> paramMap = new LinkedHashMap<>();
        Set<URL> usedResources = new HashSet<>();
        Resource resource1 = resourceResolver.getResource("classpath:META-INF/conf.properties");
        if (resource1 != null && resource1.exists()) {
            URL url = resource1.getURL();
            if (!usedResources.contains(url)) {
                ConfigLoader.loadResource(paramMap, resource1);
                usedResources.add(url);
            }
        }
        Resource resource2 = resourceResolver.getResource("classpath:META-INF/conf-core.properties");
        if (resource2 != null && resource2.exists() && !usedResources.contains(resource2.getURL())) {
            URL url = resource2.getURL();
            if (!usedResources.contains(url)) {
                ConfigLoader.loadResource(paramMap, resource2);
                usedResources.add(url);
            }
        }
        Resource[] resources = resourceResolver.getResources("classpath*:META-INF/conf-*.properties");
        for (Resource resource: resources) {
            if (resource.exists() && !usedResources.contains(resource.getURL())) {
                URL url = resource.getURL();
                if (!usedResources.contains(url)) {
                    ConfigLoader.loadResource(paramMap, resource);
                    usedResources.add(url);
                }
            }
        }
        return paramMap;
    }

}
