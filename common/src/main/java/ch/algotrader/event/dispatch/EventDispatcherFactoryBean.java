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
package ch.algotrader.event.dispatch;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.SimpleMessageConverter;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.event.EventBroadcaster;

/**
 * Factory bean for {@link EventDispatcherImpl}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class EventDispatcherFactoryBean implements FactoryBean<EventDispatcher>, ApplicationContextAware {

    private CommonConfig commonConfig;

    private EventBroadcaster eventDispatcher;

    private EngineManager engineManager;

    private MessageConverter messageConverter;

    private ApplicationContext applicationContext;

    public void setCommonConfig(final CommonConfig commonConfig) {
        this.commonConfig = commonConfig;
    }

    public void setLocalEventBroadcaster(final EventBroadcaster eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    public void setEngineManager(final EngineManager engineManager) {
        this.engineManager = engineManager;
    }

    public void setMessageConverter(final MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public EventDispatcher getObject() throws Exception {

        return new EventDispatcherImpl(
                this.commonConfig,
                this.eventDispatcher,
                this.engineManager,
                this.applicationContext.containsBean("genericTemplate") ? this.applicationContext.getBean("genericTemplate", JmsTemplate.class) : null,
                this.applicationContext.containsBean("strategyTemplate") ? this.applicationContext.getBean("strategyTemplate", JmsTemplate.class) : null,
                this.applicationContext.containsBean("marketDataTemplate") ? this.applicationContext.getBean("marketDataTemplate", JmsTemplate.class) : null,
                this.messageConverter != null ? this.messageConverter : new SimpleMessageConverter());
    }

    @Override
    public Class<?> getObjectType() {
        return EventDispatcher.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
