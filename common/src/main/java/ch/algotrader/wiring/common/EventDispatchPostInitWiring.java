/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.wiring.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import ch.algotrader.event.EventListenerRegistry;
import ch.algotrader.event.listener.BarEventListener;
import ch.algotrader.event.listener.ClosePositionEventListener;
import ch.algotrader.event.listener.EntityCacheEventListener;
import ch.algotrader.event.listener.ExpirePositionEventListener;
import ch.algotrader.event.listener.FillEventListener;
import ch.algotrader.event.listener.LifecycleEventListener;
import ch.algotrader.event.listener.OpenPositionEventListener;
import ch.algotrader.event.listener.OrderCompletionEventListener;
import ch.algotrader.event.listener.OrderEventListener;
import ch.algotrader.event.listener.OrderStatusEventListener;
import ch.algotrader.event.listener.QueryCacheEventListener;
import ch.algotrader.event.listener.SessionEventListener;
import ch.algotrader.event.listener.TickEventListener;
import ch.algotrader.event.listener.TransactionEventListener;

/**
 * Event listener wiring.
 */
@Configuration
public class EventDispatchPostInitWiring {

    private static final Class<?>[] LISTENER_CLASSES = new Class[] {
            TickEventListener.class,
            BarEventListener.class,
            OrderEventListener.class,
            OrderStatusEventListener.class,
            OrderCompletionEventListener.class,
            FillEventListener.class,
            TransactionEventListener.class,
            OpenPositionEventListener.class,
            ClosePositionEventListener.class,
            ExpirePositionEventListener.class,
            SessionEventListener.class,
            LifecycleEventListener.class,
            EntityCacheEventListener.class,
            QueryCacheEventListener.class
    };

    @Bean(name = "eventDispatcherPostprocessor")
    public ApplicationListener<ContextRefreshedEvent> createEventDispatcherPostprocessor(
            final EventListenerRegistry eventListenerRegistry,
            final ApplicationContext applicationContext) {

        return new ApplicationListener<ContextRefreshedEvent>() {

            private final AtomicBoolean postProcessed = new AtomicBoolean(false);

            @Override
            public void onApplicationEvent(final ContextRefreshedEvent contextRefreshedEvent) {

                if (this.postProcessed.compareAndSet(false, true)) {

                    for (Class<?> listenerClass : LISTENER_CLASSES) {
                        Method[] methods = listenerClass.getMethods();
                        if (methods.length != 1) {
                            throw new IllegalStateException("Class " + listenerClass + " cannot be used as an event listener");
                        }
                        Method method = methods[0];
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        if (parameterTypes.length != 1) {
                            throw new IllegalStateException("Class " + listenerClass + " cannot be used as an event listener");
                        }
                        Class<?> parameter = parameterTypes[0];
                        Map<String, ?> listenerMap = applicationContext.getBeansOfType(listenerClass);

                        for (Map.Entry<String, ?> entry: listenerMap.entrySet()) {

                            Object listener = entry.getValue();
                            eventListenerRegistry.register(event -> {
                                try {
                                    method.invoke(listener, event);
                                } catch (IllegalAccessException ex) {
                                    throw new IllegalAccessError(ex.getMessage());
                                } catch (InvocationTargetException ex) {
                                    Throwable cause = ex.getCause();
                                    if (cause instanceof RuntimeException) {
                                        throw (RuntimeException) cause;
                                    } else {
                                        throw new RuntimeException(cause);
                                    }
                                }
                            }, parameter);
                        }
                    }
                }
            }
        };
    }

}


