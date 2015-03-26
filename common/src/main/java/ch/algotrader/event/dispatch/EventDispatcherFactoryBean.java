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

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.SimpleMessageConverter;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderCompletion;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.event.EventListenerRegistry;
import ch.algotrader.event.listener.BarEventListener;
import ch.algotrader.event.listener.ClosePositionEventListener;
import ch.algotrader.event.listener.ExpirePositionEventListener;
import ch.algotrader.event.listener.FillEventListener;
import ch.algotrader.event.listener.LifecycleEventListener;
import ch.algotrader.event.listener.OpenPositionEventListener;
import ch.algotrader.event.listener.OrderCompletionEventListener;
import ch.algotrader.event.listener.OrderEventListener;
import ch.algotrader.event.listener.OrderStatusEventListener;
import ch.algotrader.event.listener.SessionEventListener;
import ch.algotrader.event.listener.TickEventListener;
import ch.algotrader.event.listener.TransactionEventListener;
import ch.algotrader.vo.ClosePositionVO;
import ch.algotrader.vo.ExpirePositionVO;
import ch.algotrader.vo.LifecycleEventVO;
import ch.algotrader.vo.OpenPositionVO;
import ch.algotrader.vo.SessionEventVO;

/**
 * Factory bean for {@link EventDispatcherImpl}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class EventDispatcherFactoryBean implements FactoryBean<EventDispatcher>, ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {

    private final AtomicBoolean postProcessed;

    private CommonConfig commonConfig;

    private EventListenerRegistry eventListenerRegistry;

    private EngineManager engineManager;

    private MessageConverter messageConverter;

    private ApplicationContext applicationContext;

    public EventDispatcherFactoryBean() {
        this.postProcessed = new AtomicBoolean(false);
    }

    public void setCommonConfig(final CommonConfig commonConfig) {
        this.commonConfig = commonConfig;
    }

    public void setEventListenerRegistry(final EventListenerRegistry eventListenerRegistry) {
        this.eventListenerRegistry = eventListenerRegistry;
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
                this.eventListenerRegistry,
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

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {

        if (this.postProcessed.compareAndSet(false, true)) {

            Map<String, TickEventListener> tickListenerMap = this.applicationContext.getBeansOfType(TickEventListener.class);
            for (Map.Entry<String, TickEventListener> entry: tickListenerMap.entrySet()) {

                final TickEventListener listener = entry.getValue();
                this.eventListenerRegistry.register(listener::onTick, Tick.class);
            }
            Map<String, BarEventListener> barListenerMap = this.applicationContext.getBeansOfType(BarEventListener.class);
            for (Map.Entry<String, BarEventListener> entry: barListenerMap.entrySet()) {

                final BarEventListener listener = entry.getValue();
                this.eventListenerRegistry.register(listener::onBar, Bar.class);
            }
            Map<String, SessionEventListener> sessionEventSinkMap = this.applicationContext.getBeansOfType(SessionEventListener.class);
            for (Map.Entry<String, SessionEventListener> entry: sessionEventSinkMap.entrySet()) {

                final SessionEventListener listener = entry.getValue();

                this.eventListenerRegistry.register(listener::onChange, SessionEventVO.class);
            }
            Map<String, LifecycleEventListener> lifecycleEventMap = this.applicationContext.getBeansOfType(LifecycleEventListener.class);
            for (Map.Entry<String, LifecycleEventListener> entry: lifecycleEventMap.entrySet()) {

                final LifecycleEventListener listener = entry.getValue();

                this.eventListenerRegistry.register(listener::onChange, LifecycleEventVO.class);
            }
            Map<String, OrderEventListener> orderEventListenerMap = this.applicationContext.getBeansOfType(OrderEventListener.class);
            for (Map.Entry<String, OrderEventListener> entry: orderEventListenerMap.entrySet()) {

                final OrderEventListener listener = entry.getValue();

                this.eventListenerRegistry.register(listener::onOrder, Order.class);
            }
            Map<String, OrderStatusEventListener> orderStatusEventListenerMap = this.applicationContext.getBeansOfType(OrderStatusEventListener.class);
            for (Map.Entry<String, OrderStatusEventListener> entry: orderStatusEventListenerMap.entrySet()) {

                final OrderStatusEventListener orderEventListener = entry.getValue();

                this.eventListenerRegistry.register(orderEventListener::onOrderStatus, OrderStatus.class);
            }
            Map<String, OrderCompletionEventListener> orderCompletionEventListenerMap = this.applicationContext.getBeansOfType(OrderCompletionEventListener.class);
            for (Map.Entry<String, OrderCompletionEventListener> entry: orderCompletionEventListenerMap.entrySet()) {

                final OrderCompletionEventListener listener = entry.getValue();

                this.eventListenerRegistry.register(listener::onOrderCompletion, OrderCompletion.class);
            }
            Map<String, FillEventListener> fillEventListenerMap = this.applicationContext.getBeansOfType(FillEventListener.class);
            for (Map.Entry<String, FillEventListener> entry: fillEventListenerMap.entrySet()) {

                final FillEventListener listener = entry.getValue();

                this.eventListenerRegistry.register(listener::onFill, Fill.class);
            }
            Map<String, TransactionEventListener> transactionEventListenerMap = this.applicationContext.getBeansOfType(TransactionEventListener.class);
            for (Map.Entry<String, TransactionEventListener> entry: transactionEventListenerMap.entrySet()) {

                final TransactionEventListener listener = entry.getValue();

                this.eventListenerRegistry.register(listener::onTransaction, Transaction.class);
            }
            Map<String, OpenPositionEventListener> openPositionEventListenerMap = this.applicationContext.getBeansOfType(OpenPositionEventListener.class);
            for (Map.Entry<String, OpenPositionEventListener> entry: openPositionEventListenerMap.entrySet()) {

                final OpenPositionEventListener listener = entry.getValue();

                this.eventListenerRegistry.register(listener::onOpenPosition, OpenPositionVO.class);
            }
            Map<String, ClosePositionEventListener> closePositionEventListenerMap = this.applicationContext.getBeansOfType(ClosePositionEventListener.class);
            for (Map.Entry<String, ClosePositionEventListener> entry: closePositionEventListenerMap.entrySet()) {

                final ClosePositionEventListener listener = entry.getValue();

                this.eventListenerRegistry.register(listener::onClosePosition, ClosePositionVO.class);
            }
            Map<String, ExpirePositionEventListener> expirePositionEventListenerMap = this.applicationContext.getBeansOfType(ExpirePositionEventListener.class);
            for (Map.Entry<String, ExpirePositionEventListener> entry: expirePositionEventListenerMap.entrySet()) {

                final ExpirePositionEventListener listener = entry.getValue();

                this.eventListenerRegistry.register(listener::onExpirePosition, ExpirePositionVO.class);
            }
        }
    }

}
