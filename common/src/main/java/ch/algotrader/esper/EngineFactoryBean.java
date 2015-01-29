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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import com.espertech.esper.client.Configuration;

import ch.algotrader.config.ConfigParams;
import ch.algotrader.util.spring.SpringDependencyLookup;

/**
 * Factory bean for {@link ch.algotrader.esper.Engine}s;
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */

public class EngineFactoryBean implements FactoryBean<Engine>, ApplicationContextAware {

    private String engineName;
    private String strategyName;
    private String[] resources;
    private String[] initModules;
    private String[] runModules;
    private ConfigParams configParams;

    private final EngineConfigurer configurer = new EngineConfigurer();

    private volatile ApplicationContext applicationContext;

    public void setEngineName(final String engineName) {
        this.engineName = engineName;
    }

    public void setStrategyName(final String strategyName) {
        this.strategyName = strategyName;
    }

    public void setResources(final String[] resources) {
        this.resources = resources;
    }

    public void setInitModules(final String[] initModules) {
        this.initModules = initModules;
    }

    public void setRunModules(final String[] runModules) {
        this.runModules = runModules;
    }

    public void setConfigParams(final ConfigParams configParams) {
        this.configParams = configParams;
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

        this.configurer.configure(this.strategyName, configuration, this.configParams);

        return new EngineImpl(this.engineName != null ? this.engineName : this.strategyName, this.strategyName,
                new SpringDependencyLookup(this.applicationContext), configuration, this.initModules, this.runModules, this.configParams);
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
