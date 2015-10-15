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
package ch.algotrader.event;

import java.util.Set;

/**
 * Event broadcaster that maintains a registry of {@link ch.algotrader.event.EventListener}s
 * it can broadcast events to.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public interface EventListenerRegistry extends EventBroadcaster {

    /**
     * Registers the given event listener as a sink for the given type of events.
     * @param listener event listener
     * @param eventType event class
     * @param <T> event type
     */
    <T> void register(EventListener<T> listener, Class<T> eventType);

    /**
     * Unregisters the given event listener as a sink for the given type of events.
     * @param listener event listener
     * @param eventType
     * @param <T> event type
     */
    <T> void unregister(EventListener<T> listener, Class<T> eventType);

    /**
     * Returns listeners registered for the given event type.
     * @param eventType
     */
    Set<EventListener<?>> getListeners(Class<?> eventType);

}
