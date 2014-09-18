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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.Assert;

import ch.algotrader.util.Consts;

/**
 * Spring based configuration loader.
 * <p/>
 * Application parameters are resolved in the following sequence:
 * <p><tt>META-INF/conf.properties</tt> classpath resource is loaded if exists<p/>
 * <p><tt>META-INF/conf-core.properties</tt> classpath resource is loaded if exists<p/>
 * <p><tt>META-INF/conf-*.properties</tt> classpath resources matching the pattern are loaded<p/>
 * <p>Strategy specific parameters from modules defined in <tt>META-INF/[StrategyName].hierarchy</tt>
 * classpath resource. The content of the resource is expected to be a list of colon separated modules
 * such as <tt>parent-strategy:parent-strategy2:my-strategy</tt>. The <tt>strategyName</tt> value is specified either
 * as a system property or as a property in one of the property resources above<p/>
 * <p><tt>META-INF/parent-strategy.properties</tt> classpath resource is loaded if exists<p/>
 * <p><tt>META-INF/parent-strategy2.properties</tt> classpath resource is loaded if exists<p/>
 * <p><tt>META-INF/my-strategy.properties</tt> classpath resource is loaded if exists<p/>
 * <p/>
 * Please note that settings of each subsequent resource override those already set from preceding resources.
 * In other words, core settings override common settings, component settings override common and core settings,
 * strategy settings override common, core and component settings.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class DefaultConfigLoader {

    private final ResourcePatternResolver resourceResolver;

    public DefaultConfigLoader(final ResourcePatternResolver resourceResolver) {
        Assert.notNull(resourceResolver, "ResourcePatternResolver is null");
        this.resourceResolver = resourceResolver;
    }

    void loadResource(final Map<String, String> paramMap, final Resource resource) throws IOException {

        if (resource == null || !resource.exists()) {
            return;
        }

        InputStream inputStream = resource.getInputStream();
        try {
            Properties props = new Properties();
            props.load(new InputStreamReader(inputStream, Consts.UTF_8));

            for (Map.Entry<Object, Object> entry: props.entrySet()) {
                String paramName = (String) entry.getKey();
                String paramValue = (String) entry.getValue();
                if (StringUtils.isNotBlank(paramName)) {
                    paramMap.put(paramName, paramValue);
                }
            }

        } finally {
            inputStream.close();
        }
    }

    String[] resolveModuleHierarchy(final String strategyName) throws IOException {

        Resource resource = this.resourceResolver.getResource("classpath:META-INF/" + strategyName + ".hierarchy");
        if (resource != null && resource.exists()) {
            InputStream instream = resource.getInputStream();
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (StringUtils.isNotBlank(line)) {
                        return line.split(":");
                    }
                }
            } finally {
                instream.close();
            }
        }
        return null;
    }

    public Map<String, String> getParams() throws IOException {

        // Load common and core parameters
        Map<String, String> paramMap = new HashMap<String, String>();
        loadResource(paramMap, this.resourceResolver.getResource("classpath:META-INF/conf.properties"));
        loadResource(paramMap, this.resourceResolver.getResource("classpath:META-INF/conf-core.properties"));

        // Load component parameters
        Resource[] resources = this.resourceResolver.getResources("classpath*:META-INF/conf-*.properties");
        for (Resource resource: resources) {

            loadResource(paramMap, resource);
        }

        // Load strategy specific parameters
        String strategyName = System.getProperty("strategyName");
        if (strategyName == null) {

            strategyName = paramMap.get("strategyName");
        }
        if (strategyName != null) {

            String[] modules = resolveModuleHierarchy(strategyName);
            if (modules == null || modules.length == 0) {

                modules = resolveModuleHierarchy(strategyName.toLowerCase(Locale.ROOT));
                if (modules == null || modules.length == 0) {

                    modules = new String[] { strategyName };
                }
            }

            for (String module: modules) {

                loadResource(paramMap, this.resourceResolver.getResource("classpath:META-INF/" + module.trim() + ".properties"));
            }
        }

        return paramMap;
    }

}
