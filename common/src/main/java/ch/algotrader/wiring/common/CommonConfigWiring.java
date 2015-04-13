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
package ch.algotrader.wiring.common;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.support.ResourcePatternResolver;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.ConfigBeanFactory;
import ch.algotrader.config.ConfigLocator;
import ch.algotrader.config.ConfigParams;
import ch.algotrader.config.ConfigProvider;
import ch.algotrader.config.spring.ConfigLoader;
import ch.algotrader.config.spring.DefaultSystemConfigProvider;

/**
 * Common framework configuration.
 */
@Configuration
public class CommonConfigWiring {

    @Bean(name = "configParams")
    public ConfigParams createConfigParams(final ResourcePatternResolver resourceResolver) throws Exception {

        Map<String, String> paramMap = ConfigLoader.load(resourceResolver);
        DefaultSystemConfigProvider configProvider = new DefaultSystemConfigProvider(paramMap, new DefaultConversionService());
        ConfigParams configParams = new ConfigParams(configProvider);
        ConfigLocator.initialize(configParams, new ConfigBeanFactory().create(configParams, CommonConfig.class));
        return configParams;
    }

    @Bean(name = "properties")
    public Properties createProperties(final ConfigParams configParams) throws Exception {

        Properties props = new Properties();
        ConfigProvider configProvider = configParams.getConfigProvider();
        Set<String> names = configProvider.getNames();
        if (names != null) {
            for (String name: names) {

                String value = configProvider.getParameter(name, String.class);
                if (value != null) {

                    props.setProperty(name, value);
                }
            }
        }
        return props;
    }

    @Bean(name = "commonConfig")
    public CommonConfig createCommonConfig() {

        return ConfigLocator.instance().getConfig(CommonConfig.class);
    }

}

