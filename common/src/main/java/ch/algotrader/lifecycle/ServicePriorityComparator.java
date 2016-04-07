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

package ch.algotrader.lifecycle;

import java.util.Comparator;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.core.annotation.AnnotationUtils;

import ch.algotrader.enumeration.InitializingServiceType;
import ch.algotrader.service.InitializationPriority;
import ch.algotrader.service.InitializingServiceI;

class ServicePriorityComparator implements Comparator<InitializingServiceI> {

    static final ServicePriorityComparator INSTANCE = new ServicePriorityComparator();

    @Override
    public int compare(final InitializingServiceI s1, final InitializingServiceI s2) {
        InitializationPriority a1 = getServiceType(s1);
        InitializationPriority a2 = getServiceType(s2);
        InitializingServiceType t1 = a1 != null ? a1.value() : InitializingServiceType.CORE;
        InitializingServiceType t2 = a2 != null ? a2.value() : InitializingServiceType.CORE;
        int result = t1.compareTo(t2);
        if (result != 0) {
            return result;
        } else {
            int p1 = a1 != null ? a1.priority() : 0;
            int p2 = a2 != null ? a2.priority() : 0;
            result = Integer.compare(p2, p1);
            if (result != 0) {
                return result;
            } else {
                // Ensure predictable order in case the type of services is the same
                return s1.getClass().getName().compareTo(s2.getClass().getName());
            }
        }
    }

    private InitializationPriority getServiceType(final InitializingServiceI service) {
        Class<?> implClass = AopProxyUtils.ultimateTargetClass(service);
        return AnnotationUtils.findAnnotation(implClass, InitializationPriority.class);
    }

}
