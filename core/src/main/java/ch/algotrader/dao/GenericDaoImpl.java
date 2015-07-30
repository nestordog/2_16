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
package ch.algotrader.dao;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.collection.CollectionPersister;
import org.springframework.transaction.support.TransactionTemplate;

import ch.algotrader.entity.security.Security;
import ch.algotrader.hibernate.HibernateInitializer;
import ch.algotrader.visitor.InitializationVisitor;

/**
 * A generic Data Access Object providing Hibernate lookup methods.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class GenericDaoImpl implements GenericDao {

    private final SessionFactory sessionFactory;
    private final TransactionTemplate txTemplate;

    public GenericDaoImpl(final SessionFactory sessionFactory, final TransactionTemplate txTemplate) {

        Validate.notNull(sessionFactory, "SessionFactory is null");
        Validate.notNull(txTemplate, "TransactionTemplate is null");

        this.sessionFactory = sessionFactory;
        this.txTemplate = txTemplate;
    }

    @Override
    public Object get(final Class<?> clazz, final Serializable id) {

        return this.txTemplate.execute(txStatus -> {

            Session session = this.sessionFactory.getCurrentSession();
            Object result = session.get(clazz, id);

            // initialize Securities
            if (result instanceof Security) {
                Security security = (Security) result;

                security.accept(InitializationVisitor.INSTANCE, HibernateInitializer.INSTANCE);
            }

            return result;
        });
    }

    @Override
    public Object getInitializedCollection(final String role, final Serializable id) {

        return this.txTemplate.execute(txStatus -> {

            SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) this.sessionFactory;
            CollectionPersister persister = sessionFactoryImpl.getCollectionPersister(role);

            // load the owner entity
            ClassMetadata ownerMetadata = persister.getOwnerEntityPersister().getClassMetadata();
            Session session = this.sessionFactory.getCurrentSession();
            Object owner = session.get(ownerMetadata.getEntityName(), id);

            // owner does not exist anymore so no point in loading the collection
            if (owner == null) {
                return null;
            }

            // get the collection by it's property name
            Object col = ownerMetadata.getPropertyValue(owner, persister.getNodeName());

            // if it is a PersistentCollection make sure it is initialized
            if (col instanceof PersistentCollection) {

                PersistentCollection collection = (PersistentCollection) col;
                if (!collection.wasInitialized()) {
                    collection.forceInitialization();
                }
            }

            return col;
        });
    }

    @Override
    public List<?> find(final String queryString, final NamedParam... namedParams) {

        return find(queryString, 0, namedParams);
    }

    @Override
    public List<?> find(final String queryString, final int maxResults, final NamedParam... namedParams) {

        return this.txTemplate.execute(txStatus -> {

            Session session = this.sessionFactory.getCurrentSession();
            Query query = session.createQuery(queryString);
            query.setCacheable(true);
            query.setMaxResults(maxResults);
            for (NamedParam namedParam : namedParams) {
                query.setParameter(namedParam.getName(), namedParam.getValue());
            }
            return query.list();
        });
    }

    @Override
    public Object findUnique(final String queryString, final NamedParam... namedParams) {

        return this.txTemplate.execute(txStatus -> {

            Session session = this.sessionFactory.getCurrentSession();
            Query query = session.createQuery(queryString);
            query.setCacheable(true);
            for (NamedParam namedParam : namedParams) {
                query.setParameter(namedParam.getName(), namedParam.getValue());
            }
            return query.uniqueResult();
        });
    }

    /**
     * Gets the querySpaces (tables) associated with a query
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Set<String> getQuerySpaces(String queryString) {

        SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) this.sessionFactory;
        return sessionFactoryImpl.getQueryPlanCache().getHQLQueryPlan(queryString, false, new HashMap()).getQuerySpaces();
    }
}
