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

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.convert.support.DefaultConversionService;

import ch.algotrader.config.ConfigBeanFactory;
import ch.algotrader.config.ConfigParams;

/**
 * Spring factory bean for {@link ch.algotrader.config.ConfigParams} that loads a
 * single config parameters element from a custom list of resources.
 *
 * @author <a href="mailto:mterzer@algotrader.ch">Marco Terzer</a>
 *
 * @version $Revision$ $Date$
 */
public class BasicConfigBeanFactoryBean<T> implements FactoryBean<T>, ApplicationContextAware {

    private Class<T> beanClass;
    private String[] resources;

    private ApplicationContext applicationContext;
    private T cachedBean;

    public void setBeanClass(final Class<T> beanClass) {
        this.beanClass = beanClass;
    }

    public void setResources(final String[] resources) {
        this.resources = resources;
    }

    public void setResource(final String resource) {
        this.resources = new String[] { resource };
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public T getObject() throws Exception {
        if (cachedBean == null) {
            Map<String, String> paramMap = new LinkedHashMap<>();
            for (String pattern: resources) {

                ConfigLoader.loadResources(paramMap, applicationContext.getResources(pattern));
            }
            ConfigParams configParams = new ConfigParams(new DefaultSystemConfigProvider(paramMap, new DefaultConversionService()));
            cachedBean = new ConfigBeanFactory().create(configParams, beanClass);
        }
        return cachedBean;
    }

    @Override
    public Class<T> getObjectType() {
        return beanClass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
