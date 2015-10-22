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

import org.springframework.beans.factory.FactoryBean;

import ch.algotrader.config.ConfigBeanFactory;
import ch.algotrader.config.ConfigParams;

/**
 * Spring factory bean for custom config beans that loads config parameters
 * from an arbitrary {@link ch.algotrader.config.ConfigParams} instance.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class CustomConfigBeanFactoryBean<T> implements FactoryBean<T> {

    private Class<T> beanClass;
    private ConfigParams configParams;

    public void setBeanClass(final Class<T> beanClass) {
        this.beanClass = beanClass;
    }

    public void setConfigParams(final ConfigParams configParams) {
        this.configParams = configParams;
    }

    @Override
    public T getObject() throws Exception {
        return new ConfigBeanFactory().create(this.configParams, this.beanClass);
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
