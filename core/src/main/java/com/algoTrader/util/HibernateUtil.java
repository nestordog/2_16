package com.algoTrader.util;

import org.apache.commons.lang.StringUtils;
import org.hibernate.LockOptions;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.persister.entity.AbstractEntityPersister;

public class HibernateUtil {

    public static boolean lock(SessionFactory sessionFactory, Object target) {

        Session session = sessionFactory.getCurrentSession();

        try {
            session.buildLockRequest(LockOptions.NONE).lock(target);
            return true;
        } catch (NonUniqueObjectException e) {
            //  different object with the same identifier value was already associated with the session
            return false;
        }
    }

    public static Object merge(SessionFactory sessionFactory, Object target) {

        Session session = sessionFactory.getCurrentSession();

        return session.merge(target);
    }

    public static int getDisriminatorValue(SessionFactory sessionFactory, Class<?> type) {

        String className = StringUtils.removeEnd(type.getName(), "Impl") + "Impl";
        SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) sessionFactory;
        AbstractEntityPersister persisiter = (AbstractEntityPersister) sessionFactoryImpl.getEntityPersister(className);
        String discriminatorStringValue = persisiter.getDiscriminatorSQLValue();
        return Integer.valueOf(discriminatorStringValue);
    }
}
