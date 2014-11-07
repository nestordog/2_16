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

package ch.algotrader;

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
        InitializingServiceType p1 = getServiceType(s1);
        InitializingServiceType p2 = getServiceType(s2);
        int result = p1.compareTo(p2);
        if (result != 0) {
            return result;
        } else {
            // Ensure predictable order in case the type of services is the same
            return s1.getClass().getName().compareTo(s2.getClass().getName());
        }
    }

    private InitializingServiceType getServiceType(final InitializingServiceI service) {
        Class<?> implClass = AopProxyUtils.ultimateTargetClass(service);
        InitializationPriority initializationPriority = AnnotationUtils.findAnnotation(implClass, InitializationPriority.class);
        return initializationPriority != null ? initializationPriority.value() : InitializingServiceType.CORE;
    }

}
