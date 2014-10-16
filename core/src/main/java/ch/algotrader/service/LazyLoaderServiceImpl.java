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

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.collection.AbstractPersistentCollection;
import org.hibernate.proxy.HibernateProxy;

import ch.algotrader.util.MyLogger;
import ch.algotrader.util.spring.HibernateSession;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@HibernateSession
public class LazyLoaderServiceImpl implements LazyLoaderService {

    private static Logger logger = MyLogger.getLogger(LazyLoaderServiceImpl.class.getName());

    private final SessionFactory sessionFactory;

    public LazyLoaderServiceImpl(final SessionFactory sessionFactory) {

        Validate.notNull(sessionFactory, "SessionFactory is null");

        this.sessionFactory = sessionFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractPersistentCollection lazyLoadCollection(final Object target, final String context, final AbstractPersistentCollection col) {

        Validate.notNull(target, "Target is null");
        Validate.notEmpty(context, "Context is empty");
        Validate.notNull(col, "Col is null");

        Session session = this.sessionFactory.openSession();

        try {
            session.buildLockRequest(LockOptions.NONE).lock(target);
            Hibernate.initialize(col);

            logger.debug("loaded collection: " + context);
        } finally {
            session.close();
        }
        return col;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object lazyLoadProxy(final Object target, final String context, final HibernateProxy proxy) {

        Validate.notNull(target, "Target is null");
        Validate.notEmpty(context, "Context is empty");
        Validate.notNull(proxy, "Proxy is null");

        Session session = this.sessionFactory.openSession();

        Object implementation;
        try {
            session.buildLockRequest(LockOptions.NONE).lock(target);
            implementation = proxy.getHibernateLazyInitializer().getImplementation();

            logger.debug("loaded proxy: " + context);
        } finally {
            session.close();
        }
        return implementation;

    }

}
