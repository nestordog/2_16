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

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.ConfigBeanFactory;
import ch.algotrader.config.ConfigLocator;
import ch.algotrader.config.ConfigParams;

/**
 * Spring factory bean for {@link ch.algotrader.config.ConfigLocator}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class ConfigLocatorFactoryBean implements FactoryBean<ConfigLocator> {

    public ConfigLocatorFactoryBean(final ConfigParams configParams) {
        Assert.notNull(configParams, "ConfigParams is null");
        ConfigLocator.initialize(configParams, new ConfigBeanFactory().create(configParams, CommonConfig.class));
    }

    @Override
    public ConfigLocator getObject() throws Exception {

        return ConfigLocator.instance();
    }

    @Override
    public Class<?> getObjectType() {
        return ConfigLocator.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
