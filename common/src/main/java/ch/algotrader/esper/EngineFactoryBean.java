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
package ch.algotrader.esper;

import org.apache.commons.lang.Validate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import com.espertech.esper.client.Configuration;

import ch.algotrader.config.ConfigParams;

/**
 * Factory bean for {@link ch.algotrader.esper.Engine}s;
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */

public class EngineFactoryBean implements FactoryBean<Engine>, ApplicationContextAware {

    private final String name;
    private final String[] resources;
    private final String[] initModules;
    private final String[] runModules;
    private final ConfigParams configParams;
    private final EngineConfigurer configurer;

    private volatile ApplicationContext applicationContext;

    public EngineFactoryBean(final String name, final String[] resources, final String[] initModules, final String[] runModules, final ConfigParams configParams) {

        Validate.notEmpty(name, "Name is null");
        Validate.notNull(configParams, "ConfigParams is null");

        this.name = name;
        this.resources = resources;
        this.initModules = initModules;
        this.runModules = runModules;
        this.configParams = configParams;
        this.configurer = new EngineConfigurer(configParams);
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Engine getObject() throws Exception {

        Configuration configuration = new Configuration();

        for (String resource: this.resources) {
            Resource[] cfgResources = this.applicationContext.getResources(resource);
            for (Resource cfgResource: cfgResources) {
                configuration.configure(cfgResource.getURL());
            }
        }

        this.configurer.configure(this.name, configuration);

        return new EngineImpl(this.name, new SpringDependencyLookup(this.applicationContext), configuration, this.initModules, this.runModules, this.configParams);
    }

    @Override
    public Class<?> getObjectType() {
        return Engine.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
