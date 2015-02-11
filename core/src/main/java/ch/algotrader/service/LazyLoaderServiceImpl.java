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

import org.apache.commons.lang.Validate;

import ch.algotrader.entity.BaseEntityI;
import ch.algotrader.hibernate.HibernateInitializer;
import ch.algotrader.util.spring.HibernateSession;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@HibernateSession
public class LazyLoaderServiceImpl implements LazyLoaderService {

    public LazyLoaderServiceImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends BaseEntityI> T lazyLoadProxy(BaseEntityI entity, String context, T proxy) {

        Validate.notNull(entity, "Entity is null");
        Validate.notEmpty(context, "Context is empty");
        Validate.notNull(proxy, "Proxy is null");

        return HibernateInitializer.INSTANCE.initializeProxy(entity, context, proxy);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends BaseEntityI> Collection<T> lazyLoadCollection(BaseEntityI entity, String context, Collection<T> col) {

        Validate.notNull(entity, "Entity is null");
        Validate.notEmpty(context, "Context is empty");
        Validate.notNull(col, "Col is null");

        return HibernateInitializer.INSTANCE.initializeCollection(entity, context, col);

    }

}
