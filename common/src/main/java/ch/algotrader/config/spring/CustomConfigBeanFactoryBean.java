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

import java.util.Objects;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import ch.algotrader.config.ConfigBeanFactory;



/**
 * Spring factory bean for {@link ch.algotrader.config.ConfigParams} that loads a
 * single config parameters element from a custom list of resources.
 *
 * @author <a href="mailto:mterzer@algotrader.ch">Marco Terzer</a>
 *
 * @version $Revision$ $Date$
 */
public class CustomConfigBeanFactoryBean<T> implements FactoryBean<T>, ApplicationContextAware {

    private final Class<T> beanClass;
    private final String[] resources;

    private ApplicationContext applicationContext;
    private T cachedBean;

    public CustomConfigBeanFactoryBean(Class<T> beanClass, String resource) {
        this(beanClass, new String[] {resource});
    }
    public CustomConfigBeanFactoryBean(Class<T> beanClass, String[] resources) {
        this.beanClass = Objects.requireNonNull(beanClass, "beanClass cannot be null");
        this.resources = Objects.requireNonNull(resources, "resources cannot be null");
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public T getObject() throws Exception {
        if (cachedBean == null) {
            final CustomConfigParamsFactoryBean custonConfigParamsFactoryBean = new CustomConfigParamsFactoryBean();
            custonConfigParamsFactoryBean.setApplicationContext(Objects.requireNonNull(applicationContext, "application context cannot be null"));
            custonConfigParamsFactoryBean.setResources(resources);
            cachedBean = new ConfigBeanFactory().create(custonConfigParamsFactoryBean.getObject(), beanClass);
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
