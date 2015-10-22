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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.DefaultResourceLoader;

import ch.algotrader.config.spring.ConfigLoader;
import ch.algotrader.config.spring.DefaultSystemConfigProvider;

/**
 * Configuration parameter and configuration locator.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public final class ConfigLocator {

    private static final Logger LOGGER = LogManager.getLogger(ConfigLocator.class);

    private static volatile ConfigLocator INSTANCE;

    private final ConfigParams configParams;
    private final CommonConfig commonConfig;
    private final Map<Class<?>, Object> beanMap;

    private ConfigLocator(final ConfigParams configParams, final CommonConfig commonConfig) {
        this.configParams = configParams;
        this.commonConfig = commonConfig;
        this.beanMap = new HashMap<>();
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

    public static void initialize(final Map<String, String> paramMap) {

        Validate.notNull(paramMap, "Map is null");

        synchronized(ConfigLocator.class) {
            INSTANCE = standaloneInit(paramMap);
        }
    }

    public static void reset() {

        synchronized(ConfigLocator.class) {
            INSTANCE = null;
        }
    }

    public static ConfigLocator instance() {

        if (INSTANCE == null) {

            synchronized(ConfigLocator.class) {

                if (INSTANCE == null) {
                    try {

                        INSTANCE = standaloneInit();
                    } catch (Exception ex) {

                        INSTANCE = new ConfigLocator(new ConfigParams(new NoopConfigProvider()), CommonConfigBuilder.create().build());
                        LOGGER.error("Unexpected I/O error reading configuration", ex);
                    }
                }
            }
        }
        return INSTANCE;
    }

    private static ConfigLocator standaloneInit(final Map<String, String> paramMap) {

        DefaultSystemConfigProvider configProvider = new DefaultSystemConfigProvider(paramMap);
        ConfigParams configParams = new ConfigParams(configProvider);
        CommonConfig commonConfig = new ConfigBeanFactory().create(configParams, CommonConfig.class);
        return new ConfigLocator(configParams, commonConfig);
    }

    private static ConfigLocator standaloneInit() throws Exception {

        DefaultResourceLoader resourceLoader = new DefaultResourceLoader(ConfigLocator.class.getClassLoader());
        return standaloneInit(ConfigLoader.load(resourceLoader));
    }

}
