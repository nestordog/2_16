/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package com.algoTrader.service;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.collection.AbstractPersistentCollection;
import org.hibernate.proxy.HibernateProxy;

import com.algoTrader.util.MyLogger;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class LazyLoaderServiceImpl extends LazyLoaderServiceBase {

    private static Logger logger = MyLogger.getLogger(LazyLoaderServiceImpl.class.getName());

    @Override
    public AbstractPersistentCollection handleLazyLoadCollection(Object target, String context, AbstractPersistentCollection col) {

        Session session = this.getSessionFactory().openSession();

        try {
            session.buildLockRequest(LockOptions.NONE).lock(target);
            Hibernate.initialize(col);

            logger.debug("loaded collection: " + context);
        } finally {
            session.close();
        }
        return col;
    }

    @Override
    public Object handleLazyLoadProxy(Object target, String context, HibernateProxy proxy) {

        Session session = this.getSessionFactory().openSession();

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
