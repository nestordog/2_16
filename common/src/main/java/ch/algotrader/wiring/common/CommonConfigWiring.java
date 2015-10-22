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
package ch.algotrader.wiring.common;

import java.util.Date;
import java.util.Properties;
import java.util.Set;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.ConfigLocator;
import ch.algotrader.config.ConfigParams;
import ch.algotrader.config.spring.ObjectToStringConverter;
import ch.algotrader.config.spring.StringToDateConverter;

/**
 * Common framework configuration.
 */
@Configuration
public class CommonConfigWiring {

    @Bean(name = "conversionService")
    public ConversionService createConversionService() {
        DefaultConversionService conversionService = new DefaultConversionService();
        conversionService.addConverter(new StringToDateConverter());
        conversionService.addConverter(Date.class, String.class, new ObjectToStringConverter());
        return conversionService;
    }

    @Bean(name = "configParams")
    public ConfigParams createConfigParams() throws Exception {

        return ConfigLocator.instance().getConfigParams();
    }

    @Bean(name = "commonConfig")
    public CommonConfig createCommonConfig() {

        return ConfigLocator.instance().getConfig(CommonConfig.class);
    }

    @Bean(name = "commonConfigBeanFactoryPostProcessor")
    public static BeanFactoryPostProcessor createCommonConfigBeanFactoryPostProcessor() {

        return beanFactory -> {
            StandardBeanExpressionResolver beanExpressionResolver = new StandardBeanExpressionResolver(beanFactory.getBeanClassLoader()) {
                @Override
                protected void customizeEvaluationContext(final StandardEvaluationContext evalContext) {

                    evalContext.addPropertyAccessor(new ConfigParamAccessor());
                }
            };
            beanFactory.setBeanExpressionResolver(beanExpressionResolver);
        };

    }

    @Bean
    public static PropertyPlaceholderConfigurer createPropertyPlaceholderConfigurer() {

        ConfigParams configParams = ConfigLocator.instance().getConfigParams();
        Properties properties = new Properties();
        Set<String> names = configParams.getConfigProvider().getNames();
        for (String name: names) {
            String value = configParams.getString(name);
            if (value != null) {
                properties.put(name, value);
            }
        }
        PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
        configurer.setProperties(properties);
        configurer.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
        return configurer;
    }

}

