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

import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.Assert;

import ch.algotrader.config.ConfigLocator;

/**
 * Spring factory bean for Config beans such as {@link ch.algotrader.config.CommonConfig}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class ConfigBeanFactoryBean<T> implements FactoryBean<T> {

    private final Class<T> beanClass;

    public ConfigBeanFactoryBean(final Class<T> beanClass) {
        Assert.notNull(beanClass, "BeanClass is null");
        this.beanClass = beanClass;
    }

    @Override
    public T getObject() throws Exception {
        return ConfigLocator.instance().getConfig(this.beanClass);
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
