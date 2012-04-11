package com.algoTrader.util;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.LockOptions;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.collection.AbstractPersistentCollection;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.Type;

public class HibernateUtil {

    private static Logger logger = MyLogger.getLogger(HibernateUtil.class.getName());

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
                Integer id = (Integer) target.getClass().getMethod("getId", new Class[] {}).invoke(target, new Object[] {});
                logger.debug("merged " + target.getClass() + " " + id);
            } catch (Exception e1) {
                logger.debug("merged " + target.getClass());
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
     * make sure no proxies and persistentCollecitions are still attached to another session
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
        Object[] values = persister.getPropertyValues(target, EntityMode.POJO);
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
            logger.debug("evicted " + target.getClass() + " " + target);
        }
    }

    public static int getDisriminatorValue(SessionFactory sessionFactory, Class<?> type) {

        String className = StringUtils.removeEnd(type.getName(), "Impl") + "Impl";
        SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) sessionFactory;
        AbstractEntityPersister persisiter = (AbstractEntityPersister) sessionFactoryImpl.getEntityPersister(className);
        String discriminatorStringValue = persisiter.getDiscriminatorSQLValue();
        return Integer.valueOf(discriminatorStringValue);
    }

    public static Object getProxyImplementation(Object object) {

        if (object instanceof HibernateProxy) {
            HibernateProxy proxy = (HibernateProxy) object;
            return proxy.getHibernateLazyInitializer().getImplementation();
        } else {
            return object;
        }
    }
}
