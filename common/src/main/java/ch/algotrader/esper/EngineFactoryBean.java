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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

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
    private String[] configResourceList;
    private String configResource;
    private String[] initModuleList;
    private String initModules;
    private String[] runModuleList;
    private String runModules;
    private ConfigParams configParams;

    private final EngineConfigurer configurer = new EngineConfigurer();

    private volatile ApplicationContext applicationContext;

    public void setEngineName(final String engineName) {
        this.engineName = engineName;
    }

    public void setStrategyName(final String strategyName) {
        this.strategyName = strategyName;
    }

    public void setConfigResourceList(final String[] configResourceList) {
        this.configResourceList = configResourceList;
    }

    public void setConfigResource(final String configResource) {
        this.configResource = configResource;
    }

    public void setInitModuleList(final String[] initModuleList) {
        this.initModuleList = initModuleList;
    }

    public void setInitModules(final String initModules) {
        this.initModules = initModules;
    }

    public void setRunModuleList(final String[] runModuleList) {
        this.runModuleList = runModuleList;
    }

    public void setRunModules(final String runModules) {
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

        if (this.configResourceList != null) {
            for (String resource: this.configResourceList) {
                Resource[] cfgResources = this.applicationContext.getResources(resource);
                for (Resource cfgResource: cfgResources) {
                    configuration.configure(cfgResource.getURL());
                }
            }
        }
        if (this.configResource != null) {
            Resource resource1 = this.applicationContext.getResource("classpath:/META-INF/esper-common.cfg.xml");
            configuration.configure(resource1.getURL());
            Resource resource2 = this.applicationContext.getResource(this.configResource.contains(":/") ? this.configResource : "classpath:/META-INF/" + this.configResource);
            configuration.configure(resource2.getURL());
        }

        this.configurer.configure(this.strategyName, configuration, this.configParams);

        return new EngineImpl(this.engineName != null ? this.engineName : this.strategyName, this.strategyName,
                new SpringDependencyLookup(this.applicationContext), configuration,
                parseModules(this.initModuleList, this.initModules),
                parseModules(this.runModuleList, this.runModules),
                this.configParams);
    }

    private static String[] parseModules(final String[] array, final String text) {
        if (array == null && text == null) {
            return null;
        }
        List<String> list = new ArrayList<>();
        if (array != null) {
            for (String s: array) {
                list.add(s);
            }
        }
        if (text != null) {
            for (String s: StringUtils.tokenizeToStringArray(text, ",", true, true)) {
                list.add(s);
            }
        }
        return !list.isEmpty() ? list.toArray(new String[list.size()]) : null;
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
