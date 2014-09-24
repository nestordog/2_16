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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import ch.algotrader.config.spring.DefaultConfigLoader;
import ch.algotrader.config.spring.DefaultSystemConfigProvider;
import ch.algotrader.util.MyLogger;

/**
 * Configuration parameter and base configuration locator.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public final class ConfigLocator {

    private static final Logger LOGGER = MyLogger.getLogger(ConfigLocator.class.getName());

    private static volatile ConfigLocator INSTANCE;

    private final ConfigParams configParams;
    private final CommonConfig commonConfig;
    private final Map<Class<?>, Object> beanMap;

    private ConfigLocator(final ConfigParams configParams, final CommonConfig commonConfig) {
        this.configParams = configParams;
        this.commonConfig = commonConfig;
        this.beanMap = new HashMap<Class<?>, Object>();
        this.beanMap.put(CommonConfig.class, this.commonConfig);
    }

    public ConfigParams getConfigParams() {
        return this.configParams;
    }

    public CommonConfig getCommonConfig() {
        return this.commonConfig;
    }

    public <T> T getConfig(final Class<T> clazz) {
        if (clazz == null) {
            return null;
        }
        synchronized (this.beanMap) {
            T bean = clazz.cast(this.beanMap.get(clazz));
            if (bean == null) {
                bean = new ConfigBeanFactory().create(this.configParams, clazz);
                this.beanMap.put(clazz, bean);
            }
            return bean;
        }
    }

    public static void initialize(final ConfigParams configParams, final CommonConfig commonConfig) {

        Validate.notNull(configParams, "ConfigParams is null");
        Validate.notNull(commonConfig, "ATConfig is null");

        synchronized(ConfigLocator.class) {
            INSTANCE = new ConfigLocator(configParams, commonConfig);
        }
    }

    public static ConfigLocator instance() {

        if (INSTANCE == null) {

            synchronized(ConfigLocator.class) {

                if (INSTANCE == null) {
                    try {

                        INSTANCE = standaloneInit();
                    } catch (Exception ex) {

                        LOGGER.error("Unexpected I/O error reading configuration", ex);
                        INSTANCE = new ConfigLocator(new ConfigParams(new NoOpConfigProvider()), CommonConfigBuilder.create().build());
                    }
                }
            }
        }
        return INSTANCE;
    }

    private static class NoOpConfigProvider implements ConfigProvider {

        @Override
        public <T> T getParameter(String name, Class<T> clazz) {
            return null;
        }

        @Override
        public Set<String> getNames() {
            return Collections.emptySet();
        }
    }

    private static ConfigLocator standaloneInit() throws Exception {
        ResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver(ConfigLocator.class.getClassLoader());
        DefaultConfigLoader loader = new DefaultConfigLoader(patternResolver);
        DefaultSystemConfigProvider configProvider = new DefaultSystemConfigProvider(loader.getParams());
        ConfigParams configParams = new ConfigParams(configProvider);
        CommonConfig commonConfig = new ConfigBeanFactory().create(configParams, CommonConfig.class);
        return new ConfigLocator(configParams, commonConfig);
    }

}
