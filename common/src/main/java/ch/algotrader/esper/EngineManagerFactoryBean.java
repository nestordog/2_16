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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jms.core.JmsTemplate;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.service.LookupService;

/**
 * Factory bean for {@link ch.algotrader.esper.EngineManagerFactoryBean}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class EngineManagerFactoryBean implements FactoryBean<EngineManager>, ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {

    private final AtomicBoolean postProcessed;

    private CommonConfig commonConfig;

    private ApplicationContext applicationContext;

    public EngineManagerFactoryBean() {
        this.postProcessed = new AtomicBoolean(false);
    }

    public void setCommonConfig(final CommonConfig commonConfig) {
        this.commonConfig = commonConfig;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public EngineManager getObject() throws Exception {

        Map<String, Engine> engineBeanMap = this.applicationContext.getBeansOfType(Engine.class);
        Map<String, Engine> engineMap = new HashMap<>(engineBeanMap.size());
        for (Map.Entry<String, Engine> entry: engineBeanMap.entrySet()) {
            Engine engine = entry.getValue();
            engineMap.put(engine.getEngineName(), engine);
        }
        Map<String, JmsTemplate> jmsTemplateBeanMap = this.applicationContext.getBeansOfType(JmsTemplate.class);
        Map<String, JmsTemplate> jmsTemplateMap = new HashMap<>(jmsTemplateBeanMap.size());
        for (Map.Entry<String, JmsTemplate> entry: jmsTemplateBeanMap.entrySet()) {
            String name = entry.getKey();
            JmsTemplate jmsTemplate = entry.getValue();
            jmsTemplateMap.put(name, jmsTemplate);
        }
        return new EngineManagerImpl(this.commonConfig, engineMap, jmsTemplateMap);
    }

    @Override
    public Class<?> getObjectType() {
        return EngineManager.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {

        if (this.postProcessed.compareAndSet(false, true)) {

            LookupService lookupService = this.applicationContext.getBean("lookupService", LookupService.class);
            Map<String, Engine> engineBeanMap = this.applicationContext.getBeansOfType(Engine.class);
            for (Map.Entry<String, Engine> entry: engineBeanMap.entrySet()) {

                Engine engine = entry.getValue();
                Strategy strategy = lookupService.getStrategyByName(engine.getStrategyName());
                if (strategy != null) {

                    engine.setVariableValue("engineStrategy", strategy);
                }
            }
        }
    }
}
