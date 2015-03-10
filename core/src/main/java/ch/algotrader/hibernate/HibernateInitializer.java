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
package ch.algotrader.hibernate;

import java.util.Collection;

import org.apache.commons.lang.ClassUtils;
import org.hibernate.collection.AbstractPersistentCollection;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

import ch.algotrader.entity.BaseEntityI;
import ch.algotrader.entity.Initializer;
import ch.algotrader.util.metric.MetricsUtil;

/**
 * An Initializer implementation that can be used to initialize entities while inside a HibnerateSession
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class HibernateInitializer implements Initializer {

    public static final HibernateInitializer INSTANCE = new HibernateInitializer();

    private HibernateInitializer() {
    }

    @Override
    public <T extends BaseEntityI> T initializeProxy(BaseEntityI entity, String context, T proxy) {


        if (proxy instanceof HibernateProxy) {

            HibernateProxy hibernateProxy = (HibernateProxy) proxy;
            LazyInitializer initializer = hibernateProxy.getHibernateLazyInitializer();

            if (initializer.getSession() != null) {

                long before = System.nanoTime();
                proxy = (T) initializer.getImplementation();
                MetricsUtil.account(ClassUtils.getShortClassName(entity.getClass()) + context, (before));
            } else {
                throw new IllegalStateException("no hibernate session available");
            }
        }

        return proxy;
    }

    @Override
    public <T extends BaseEntityI, C extends Collection<T>> C initializeCollection(BaseEntityI entity, String context, C col) {

        if (col instanceof AbstractPersistentCollection) {

            AbstractPersistentCollection persistentCol = (AbstractPersistentCollection) col;
            if (!persistentCol.wasInitialized()) {
                if (persistentCol.getSession() != null) {

                    long before = System.nanoTime();
                    persistentCol.forceInitialization();
                    MetricsUtil.account(ClassUtils.getShortClassName(entity.getClass()) + context, (before));
                } else {
                    throw new IllegalStateException("no hibernate session available");
                }
            }
        }

        return col;
    }

}
