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

import java.util.Collection;

import ch.algotrader.entity.BaseEntityI;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface LazyLoaderService {

    /**
     * Lazy-loads a Hibernate-Proxy
     * @param entity The target object containing the Hibernate Proxy
     * @param context An arbitrary String Context (e.g. "Position.getStrategy") that will be used for Logging purposes.
     * @param proxy The Hibernate Proxy
     */
    public <T extends BaseEntityI> T lazyLoadProxy(BaseEntityI entity, String context, T proxy);

    /**
     * Lazy-loads a Hibernate Persistent-Collections.
     * @param entity The target entity containing the uninitialized PersistentCollection
     * @param context An arbitrary String Context (e.g. "Position.getTransactions") that will be used for Logging purposes.
     * @param col The uninitialized PersistentCollection
     */
    public <T extends BaseEntityI> Collection<T> lazyLoadCollection(BaseEntityI entity, String context, Collection<T> col);

}
