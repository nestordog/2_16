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

import java.util.Properties;
import java.util.Set;

import org.springframework.beans.factory.FactoryBean;

import ch.algotrader.config.ConfigParams;
import ch.algotrader.config.ConfigProvider;

/**
 * Spring factory bean that can export config values as {@link java.util.Properties}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class ConfigPropertiesFactoryBean implements FactoryBean<Properties> {

    private ConfigParams configParams;

    public void setConfigParams(final ConfigParams configParams) {
        this.configParams = configParams;
    }

    @Override
    public Properties getObject() throws Exception {
        Properties props = new Properties();
        ConfigProvider configProvider = this.configParams.getConfigProvider();
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

    @Override
    public Class<?> getObjectType() {
        return Properties.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

}
