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
package ch.algotrader.util;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.LockOptions;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.collection.internal.AbstractPersistentCollection;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.internal.SessionImpl;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.Type;

/**
 * Provides static Hibernate utility methods.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class HibernateUtil {

    private static final Logger LOGGER = LogManager.getLogger(HibernateUtil.class);

    /**
     * Tries to lock the transient object (modifications will be lost).
     * If the object is already associated with the session, a merge is executed to
     * to merge the transient object onto the one already in the session
     * If the object is associated with another session, it will be evicted from the other session
     * Therefore reattach does not work if multiple threads use the same object
     */
    public static Object reattach(SessionFactory sessionFactory, Object target) {

        // get the current session
        Session session = sessionFactory.getCurrentSession();

        try {
            // try to lock
            session.buildLockRequest(LockOptions.NONE).lock(target);
            return target;

        } catch (NonUniqueObjectException e) {

            //  in case "a different object with the same identifier value was already associated with the session" merge the target
            Object obj = session.merge(target);

            try {
                // try to get the entity id (cannot use toString because of lazy initialization)
                Long id = (Long) target.getClass().getMethod("getId", new Class[] {}).invoke(target, new Object[] {});
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("merged {} {}", target.getClass(), id);
                }
            } catch (Exception e1) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("merged {}", target.getClass());
                }
            }

            return obj;

        } catch (HibernateException e) {

            if (e.getMessage().startsWith("Illegal attempt to associate a collection with two open sessions")
                    || e.getMessage().startsWith("illegally attempted to associate a proxy with two open Sessions")
                    || e.getMessage().startsWith("reassociated object has dirty collection")) {

                // evict the target from the other session
                evict(sessionFactory, target);

                // start over
                return reattach(sessionFactory, target);
            } else {
                // some other HibernateException
                throw e;
            }
        }
    }

    /**
     * Gets the descriminator value based on the given class.
     */
    public static int getDisriminatorValue(SessionFactory sessionFactory, Class<?> type) {

        AbstractEntityPersister persister = getEntityPersister(sessionFactory, type);
        return Integer.valueOf(persister.getDiscriminatorSQLValue());
    }

    public static Serializable getNextId(SessionFactory sessionFactory, Class<?> type) {

        AbstractEntityPersister persister = getEntityPersister(sessionFactory, type);
        return persister.getIdentifierGenerator().generate((SessionImpl) sessionFactory.getCurrentSession(), null);
    }

    private static AbstractEntityPersister getEntityPersister(SessionFactory sessionFactory, Class<?> type) {

        String className = StringUtils.removeEnd(type.getName(), "Impl") + "Impl";
        SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) sessionFactory;
        return (AbstractEntityPersister) sessionFactoryImpl.getEntityPersister(className);
    }

    /**
     * Detaches proxies and persistentCollecitions from other another session
     */
    private static void evict(SessionFactory sessionFactory, Object target) {

        SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) sessionFactory;
        Session currentSession = sessionFactory.getCurrentSession();

        // get the className either direct or from the proxy
        String className;
        if (target instanceof HibernateProxy) {
            className = ((HibernateProxy) target).getHibernateLazyInitializer().getEntityName();
        } else
            className = target.getClass().getName();

        AbstractEntityPersister persister = (AbstractEntityPersister) sessionFactoryImpl.getEntityPersister(className);
        Object[] values = persister.getPropertyValues(target);
        Type[] types = persister.getPropertyTypes();
        boolean evicted = false;
        for (int i = 0; i < types.length; i++) {
            Session session = null; // other session

            // check collections (if session is not the same as current session evict)
            if (types[i].isCollectionType() && values[i] instanceof AbstractPersistentCollection) {
                AbstractPersistentCollection col = (AbstractPersistentCollection) values[i];
                session = (Session) col.getSession();
                if (session != null && !session.equals(currentSession)) {
                    session.evict(target);
                    evicted = true;
                }
                // check proxies (if session is not the same as current session evict)
            } else if (types[i].isEntityType() && values[i] instanceof HibernateProxy) {
                HibernateProxy proxy = (HibernateProxy) values[i];
                session = (Session) proxy.getHibernateLazyInitializer().getSession();
                if (session != null && !session.equals(currentSession)) {
                    session.evict(values[i]);
                    evicted = true;
                }
            }
        }

        if (evicted) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("evicted {} {}", target.getClass(), target);
            }
        }
    }
}
