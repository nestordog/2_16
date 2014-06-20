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

import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.Assert;

/**
 * Spring factory bean for Config beans such as {@link ch.algotrader.config.BaseConfig}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class ConfigPropertiesFactoryBean implements FactoryBean<Properties> {

    private final DefaultConfigProvider defaultConfigProvider;

    public ConfigPropertiesFactoryBean(final DefaultConfigProvider defaultConfigProvider) {
        Assert.notNull(defaultConfigProvider, "DefaultConfigProvider is null");
        this.defaultConfigProvider = defaultConfigProvider;
    }

    @Override
    public Properties getObject() throws Exception {
        Properties props = new Properties();
        Map<String, ?> paramMap = this.defaultConfigProvider.getMap();
        for (Map.Entry<String, ?> entry: paramMap.entrySet()) {
            props.setProperty(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : null);
        }
        return props;
    }

    @Override
    public Class<?> getObjectType() {
        return Properties.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

}
