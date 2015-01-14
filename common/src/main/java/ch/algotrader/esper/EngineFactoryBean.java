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

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.ConfigParams;
import ch.algotrader.service.LookupService;

/**
 * {@link ch.algotrader.esper.Engine} factory;
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class EngineFactoryBean implements FactoryBean<Engine>, ApplicationContextAware {

    private final String name;
    private final LookupService lookupService;
    private final ConfigParams configParams;
    private final CommonConfig commonConfig;

    private volatile ApplicationContext applicationContext;

    public EngineFactoryBean(final String name, final LookupService lookupService, final ConfigParams params, final CommonConfig commonConfig) {
        this.name = name;
        this.lookupService = lookupService;
        this.configParams = params;
        this.commonConfig = commonConfig;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Engine getObject() throws Exception {
        EngineFactory factory = new EngineFactory(this.lookupService, new SpringDependencyLookup(this.applicationContext));
        return factory.createEngine(this.name, this.configParams, this.commonConfig);
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
