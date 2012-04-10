package com.algoTrader.service;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.collection.AbstractPersistentCollection;
import org.hibernate.proxy.HibernateProxy;

import com.algoTrader.util.MyLogger;

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
