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

import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.collection.CollectionPersister;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ch.algotrader.entity.security.Security;
import ch.algotrader.visitor.InitializationVisitor;

/**
 * A generic Data Access Object providing Hibernate lookup methods.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class GenericDao extends HibernateDaoSupport {

    /**
     * gets any Entity by its {@code class} and {@code id}.
     * Securities will get initialzed. For {@link ch.algotrader.entity.security.Combination Combinations} all
     * {@link ch.algotrader.entity.security.Component Components} will get initialized.
     */
    public Object get(final Class<?> clazz, final Serializable id) {

        return getHibernateTemplate().execute(new HibernateCallback<Object>() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException, SQLException {

                Object result = session.get(clazz, id);

                // initialize Securities
                if (result instanceof Security) {
                    Security security = (Security) result;

                    security.accept(InitializationVisitor.INSTANCE, HibernateInitializer.INSTANCE);
                }

                return result;
            }
        });
    }

    /**
     * gets the initialized Collection specified by its {@code role} and entity {@code id}
     */
    public Object getInitializedCollection(final String role, final Serializable id) {

        return getHibernateTemplate().execute(new HibernateCallback<Object>() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException, SQLException {

                SessionFactoryImpl sessionFactory = (SessionFactoryImpl) getSessionFactory();
                CollectionPersister persister = sessionFactory.getCollectionPersister(role);

                // load the owner entity
                ClassMetadata ownerMetadata = persister.getOwnerEntityPersister().getClassMetadata();
                Object owner = session.get(ownerMetadata.getEntityName(), id);

                // owner does not exist anymore so no point in loading the collection
                if (owner == null) {
                    return null;
                }

                // get the collection by it's property name
                Object col = ownerMetadata.getPropertyValue(owner, persister.getNodeName(), EntityMode.POJO);

                // if it is a PersistentCollection make sure it is initialized
                if (col instanceof PersistentCollection) {

                    PersistentCollection collection = (PersistentCollection) col;
                    if (!collection.wasInitialized()) {
                        collection.forceInitialization();
                    }
                }

                return col;
            }
        });
    }

    /**
     * Performs a HQL query based on the given {@code queryString}
     */
    public List<?> find(final String queryString) {

        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setCacheQueries(true);
        return hibernateTemplate.find(queryString);
    }

    /**
     * Performs a HQL query based on the given {@code queryString} and {@code namedParameters}
     * @return a List of Objects
     */
    public List<?> find(final String queryString, final Map<String, Object> namedParameters) {

        String[] paramNames = new String[namedParameters.size()];
        Object[] values = new Object[namedParameters.size()];

        int index = 0;
        for (Map.Entry<String, Object> entry : namedParameters.entrySet()) {
            paramNames[index] = entry.getKey();
            values[index] = entry.getValue();
            index++;
        }

        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setCacheQueries(true);
        return hibernateTemplate.findByNamedParam(queryString, paramNames, values);
    }


    /**
     * Performs a HQL query based on the given {@code queryString}
     * @return a unique Object
     */
    public Object findUnique(final String queryString) {

        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setCacheQueries(true);

        return hibernateTemplate.execute(new HibernateCallback<Object>() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createQuery(queryString).uniqueResult();
            }
        });
    }


    /**
     * Performs a HQL query based on the given {@code queryString} and {@code namedParameters}
     * @return a unique Object
     */
    public Object findUnique(final String queryString, final Map<String, Object> namedParameters) {

        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setCacheQueries(true);

        return hibernateTemplate.execute(new HibernateCallback<Object>() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery(queryString);
                for (Map.Entry<String, Object> entry : namedParameters.entrySet()) {
                    query.setParameter(entry.getKey(), entry.getValue());
                }
                return query.uniqueResult();
            }
        });
    }

    /**
     * Gets the querySpaces (tables) associated with a query
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Set<String> getQuerySpaces(String queryString) {

        SessionFactoryImpl sessionFactory = (SessionFactoryImpl) getSessionFactory();

        return sessionFactory.getQueryPlanCache().getHQLQueryPlan(queryString, false, new HashMap()).getQuerySpaces();
    }
}
