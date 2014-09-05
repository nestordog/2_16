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
package ch.algotrader.service;

import org.hibernate.collection.AbstractPersistentCollection;
import org.hibernate.proxy.HibernateProxy;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface LazyLoaderService {

    /**
     * Lazy-loads a Hibernate Persistent-Collections.
     * @param target The target object containing the uninitialized PersistentCollection
     * @param context An arbitrary String Context (e.g. "Position.getTransactions") that will be used for Logging purposes.
     * @param col The uninitialized PersistentCollection
     */
    public AbstractPersistentCollection lazyLoadCollection(Object target, String context, AbstractPersistentCollection col);

    /**
     * Lazy-loads a Hibernate-Proxy
     * @param target The target object containing the Hibernate Proxy
     * @param context An arbitrary String Context (e.g. "Position.getStrategy") that will be used for Logging purposes.
     * @param proxy The Hibernate Proxy
     */
    public Object lazyLoadProxy(Object target, String context, HibernateProxy proxy);

}
