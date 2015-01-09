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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.convert.support.DefaultConversionService;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.ConfigBeanFactory;
import ch.algotrader.config.ConfigLocator;
import ch.algotrader.config.ConfigParams;

/**
 * Spring factory bean for {@link ch.algotrader.config.ConfigParams}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class DefaultConfigParamsFactoryBean implements FactoryBean<ConfigParams>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    public DefaultConfigParamsFactoryBean() {
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {

        this.applicationContext = applicationContext;
    }

    @Override
    public ConfigParams getObject() throws Exception {
        Map<String, String> paramMap = ConfigLoader.load(this.applicationContext);
        DefaultSystemConfigProvider configProvider = new DefaultSystemConfigProvider(paramMap, new DefaultConversionService());
        ConfigParams configParams = new ConfigParams(configProvider);
        ConfigLocator.initialize(configParams, new ConfigBeanFactory().create(configParams, CommonConfig.class));
        return configParams;
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
