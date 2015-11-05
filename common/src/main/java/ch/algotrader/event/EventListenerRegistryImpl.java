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
package ch.algotrader.event;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.lang.Validate;

/**
 * Default {@link ch.algotrader.event.EventListenerRegistry} implementations.
 * Instances of this class are expected to be thread-safe.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public final class EventListenerRegistryImpl implements EventListenerRegistry {

    private final EventTypeCache eventTypeCache;
    private final ConcurrentHashMap<Class<?>, Set<EventListener<?>>> listenerMap;

    public EventListenerRegistryImpl() {
        this.eventTypeCache = new EventTypeCache();
        this.listenerMap = new ConcurrentHashMap<>();
    }

    @Override
    public <T> void register(final EventListener<T> listener, final Class<T> eventType) {

        Validate.notNull(listener, "Listener is null");
        Validate.notNull(eventType, "Event type is null");

        Set<EventListener<?>> listeners = this.listenerMap.get(eventType);
        if (listeners == null) {

            Set<EventListener<?>> newQueue = new CopyOnWriteArraySet<>();
            listeners = this.listenerMap.putIfAbsent(eventType, newQueue);
            if (listeners == null) {
                listeners = newQueue;
            }
        }
        listeners.add(listener);
    }

    @Override
    public <T> void unregister(final EventListener<T> listener, final Class<T> eventType) {

        Validate.notNull(eventType, "Event type is null");

        Set<EventListener<?>> listeners = this.listenerMap.get(eventType);
        if (listeners != null) {

            listeners.remove(listener);
        }
    }

    @Override
    public Set<EventListener<?>> getListeners(final Class<?> eventType) {

        final Set<EventListener<?>> eventListeners = this.listenerMap.get(eventType);
        return eventListeners != null ? new HashSet<>(eventListeners) : Collections.emptySet();
    }

    @Override
    public void broadcast(final Object event) {

        if (event == null) {

            return;
        }

        Class<?>[] allTypes = this.eventTypeCache.getTypeHierarchy(event.getClass());
        for (Class<?> type: allTypes) {

            Set<EventListener<?>> listeners = this.listenerMap.get(type);
            if (listeners != null) {

                broadcastToAll(listeners, event);
            }
        }
    }

    private void broadcastToAll(final Set<EventListener<?>> listeners, final Object event) {

        for (EventListener<?> entry: listeners) {

            @SuppressWarnings("unchecked")
            EventListener<Object> listener = (EventListener<Object>) entry;
            listener.onEvent(event);
        }
    }

}
