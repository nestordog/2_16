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
package ch.algotrader.wiring.common;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jms.core.JmsTemplate;
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
import ch.algotrader.event.EventListenerRegistryImpl;
import ch.algotrader.event.dispatch.DistributedEventDispatcherImpl;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.event.dispatch.LocalEventDispatcherImpl;
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
 * Event dispatch configuration.
 */
@Configuration
public class EventDispatchWiring {

    @Bean(name = "eventListenerRegistry")
    public EventListenerRegistry createEventListenerRegistry() {

        return new EventListenerRegistryImpl();
    }

    @Bean(name = "eventDispatcher")
    public EventDispatcher createEventDispatcher(
            final CommonConfig commonConfig,
            final EventListenerRegistry eventListenerRegistry,
            final EngineManager engineManager,
            final ApplicationContext applicationContext) {

        if (commonConfig.isSimulation() || commonConfig.isEmbedded()) {
            return new LocalEventDispatcherImpl(eventListenerRegistry, engineManager);
        } else {
            return new DistributedEventDispatcherImpl(
                    eventListenerRegistry,
                    engineManager,
                    applicationContext.containsBean("genericTemplate") ? applicationContext.getBean("genericTemplate", JmsTemplate.class) : null,
                    applicationContext.containsBean("strategyTemplate") ? applicationContext.getBean("strategyTemplate", JmsTemplate.class) : null,
                    applicationContext.containsBean("marketDataTemplate") ? applicationContext.getBean("marketDataTemplate", JmsTemplate.class) : null,
                    new SimpleMessageConverter());
        }
    }

    @Bean(name = "eventDispatcherPostprocessor")
    public ApplicationListener<ContextRefreshedEvent> createEventDispatcherPostprocessor(
            final EventListenerRegistry eventListenerRegistry,
            final ApplicationContext applicationContext) {

        return new ApplicationListener<ContextRefreshedEvent>() {

            private final AtomicBoolean postProcessed = new AtomicBoolean(false);

            @Override
            public void onApplicationEvent(final ContextRefreshedEvent contextRefreshedEvent) {

                if (this.postProcessed.compareAndSet(false, true)) {

                    Map<String, TickEventListener> tickListenerMap = applicationContext.getBeansOfType(TickEventListener.class);
                    for (Map.Entry<String, TickEventListener> entry: tickListenerMap.entrySet()) {

                        final TickEventListener listener = entry.getValue();
                        eventListenerRegistry.register(listener::onTick, Tick.class);
                    }
                    Map<String, BarEventListener> barListenerMap = applicationContext.getBeansOfType(BarEventListener.class);
                    for (Map.Entry<String, BarEventListener> entry: barListenerMap.entrySet()) {

                        final BarEventListener listener = entry.getValue();
                        eventListenerRegistry.register(listener::onBar, Bar.class);
                    }
                    Map<String, SessionEventListener> sessionEventSinkMap = applicationContext.getBeansOfType(SessionEventListener.class);
                    for (Map.Entry<String, SessionEventListener> entry: sessionEventSinkMap.entrySet()) {

                        final SessionEventListener listener = entry.getValue();

                        eventListenerRegistry.register(listener::onChange, SessionEventVO.class);
                    }
                    Map<String, LifecycleEventListener> lifecycleEventMap = applicationContext.getBeansOfType(LifecycleEventListener.class);
                    for (Map.Entry<String, LifecycleEventListener> entry: lifecycleEventMap.entrySet()) {

                        final LifecycleEventListener listener = entry.getValue();

                        eventListenerRegistry.register(listener::onChange, LifecycleEventVO.class);
                    }
                    Map<String, OrderEventListener> orderEventListenerMap = applicationContext.getBeansOfType(OrderEventListener.class);
                    for (Map.Entry<String, OrderEventListener> entry: orderEventListenerMap.entrySet()) {

                        final OrderEventListener listener = entry.getValue();

                        eventListenerRegistry.register(listener::onOrder, Order.class);
                    }
                    Map<String, OrderStatusEventListener> orderStatusEventListenerMap = applicationContext.getBeansOfType(OrderStatusEventListener.class);
                    for (Map.Entry<String, OrderStatusEventListener> entry: orderStatusEventListenerMap.entrySet()) {

                        final OrderStatusEventListener orderEventListener = entry.getValue();

                        eventListenerRegistry.register(orderEventListener::onOrderStatus, OrderStatus.class);
                    }
                    Map<String, OrderCompletionEventListener> orderCompletionEventListenerMap = applicationContext.getBeansOfType(OrderCompletionEventListener.class);
                    for (Map.Entry<String, OrderCompletionEventListener> entry: orderCompletionEventListenerMap.entrySet()) {

                        final OrderCompletionEventListener listener = entry.getValue();

                        eventListenerRegistry.register(listener::onOrderCompletion, OrderCompletion.class);
                    }
                    Map<String, FillEventListener> fillEventListenerMap = applicationContext.getBeansOfType(FillEventListener.class);
                    for (Map.Entry<String, FillEventListener> entry: fillEventListenerMap.entrySet()) {

                        final FillEventListener listener = entry.getValue();

                        eventListenerRegistry.register(listener::onFill, Fill.class);
                    }
                    Map<String, TransactionEventListener> transactionEventListenerMap = applicationContext.getBeansOfType(TransactionEventListener.class);
                    for (Map.Entry<String, TransactionEventListener> entry: transactionEventListenerMap.entrySet()) {

                        final TransactionEventListener listener = entry.getValue();

                        eventListenerRegistry.register(listener::onTransaction, Transaction.class);
                    }
                    Map<String, OpenPositionEventListener> openPositionEventListenerMap = applicationContext.getBeansOfType(OpenPositionEventListener.class);
                    for (Map.Entry<String, OpenPositionEventListener> entry: openPositionEventListenerMap.entrySet()) {

                        final OpenPositionEventListener listener = entry.getValue();

                        eventListenerRegistry.register(listener::onOpenPosition, OpenPositionVO.class);
                    }
                    Map<String, ClosePositionEventListener> closePositionEventListenerMap = applicationContext.getBeansOfType(ClosePositionEventListener.class);
                    for (Map.Entry<String, ClosePositionEventListener> entry: closePositionEventListenerMap.entrySet()) {

                        final ClosePositionEventListener listener = entry.getValue();

                        eventListenerRegistry.register(listener::onClosePosition, ClosePositionVO.class);
                    }
                    Map<String, ExpirePositionEventListener> expirePositionEventListenerMap = applicationContext.getBeansOfType(ExpirePositionEventListener.class);
                    for (Map.Entry<String, ExpirePositionEventListener> entry: expirePositionEventListenerMap.entrySet()) {

                        final ExpirePositionEventListener listener = entry.getValue();

                        eventListenerRegistry.register(listener::onExpirePosition, ExpirePositionVO.class);
                    }
                }
            }
        };
    }

}


