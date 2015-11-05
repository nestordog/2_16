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
import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Event class cache..
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public final class EventTypeCache {

    private final ConcurrentHashMap<Class<?>, Class<?>[]> eventTypeMap;

    public EventTypeCache() {
        this.eventTypeMap = new ConcurrentHashMap<>();
    }

    static Class<?>[] calculateTypeHierarchy(final Class<?> eventClass) {

        LinkedHashSet<Class<?>> allTypeSet = new LinkedHashSet<>();
        Class<?> currentClass = eventClass;
        while (currentClass != null) {
            allTypeSet.add(currentClass);

            Class<?>[] interfaces = currentClass.getInterfaces();
            if (interfaces != null) {

                Collections.addAll(allTypeSet, interfaces);
            }
            currentClass = currentClass.getSuperclass();
        }
        return allTypeSet.toArray(new Class[allTypeSet.size()]);
    }

    public Class<?>[] getTypeHierarchy(final Class<?> eventClass) {

        Class<?>[] allTypes = this.eventTypeMap.get(eventClass);
        if (allTypes != null) {

            return allTypes;
        }
        Class<?>[] newAllTypes = calculateTypeHierarchy(eventClass);
        allTypes = this.eventTypeMap.putIfAbsent(eventClass, newAllTypes);
        if (allTypes == null) {

            allTypes = newAllTypes;
        }

        return allTypes;
    }

}
