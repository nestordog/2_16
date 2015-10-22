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
package ch.algotrader.config.spring;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.convert.support.DefaultConversionService;

import ch.algotrader.config.ConfigParams;

/**
 * Spring factory bean for {@link ch.algotrader.config.ConfigParams} that loads config parameters
 * from a list of resources.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class CustomConfigParamsFactoryBean implements FactoryBean<ConfigParams>, ApplicationContextAware {

    private String[] resources;
    private ConfigParams global;

    private ApplicationContext applicationContext;


    public void setResources(final String[] resources) {
        this.resources = resources;
    }

    public void setResource(final String resource) {
        this.resources = new String[] { resource };
    }

    public void setGlobal(final ConfigParams global) {
        this.global = global;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {

        this.applicationContext = applicationContext;
    }

    @Override
    public ConfigParams getObject() throws Exception {

        Map<String, String> paramMap = new LinkedHashMap<>();
        for (String resource : resources) {
            final String pattern;
            if (applicationContext instanceof ConfigurableApplicationContext) {
                //allow for expressions, e.g. "#{systemProperties['config.params.extra']}"
                final ConfigurableApplicationContext cac = (ConfigurableApplicationContext)applicationContext;
                final ConfigurableBeanFactory cbf = cac.getBeanFactory();
                final Object value = cbf.getBeanExpressionResolver().evaluate(resource, new BeanExpressionContext(cbf, null));
                pattern = cbf.getTypeConverter().convertIfNecessary(value, String.class);
            } else {
                pattern = resource;
            }
            ConfigLoader.loadResources(paramMap, this.applicationContext.getResources(pattern));
        }

        return new ConfigParams(new DefaultSystemConfigProvider(paramMap, new DefaultConversionService(),
                this.global != null ? this.global.getConfigProvider() : null));
    }

    @Override
    public Class<?> getObjectType() {
        return ConfigParams.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
