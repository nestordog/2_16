/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.collection.CollectionPersister;
import org.springframework.transaction.support.TransactionTemplate;

import ch.algotrader.entity.BaseEntityI;
import ch.algotrader.enumeration.QueryType;
import ch.algotrader.util.HibernateUtil;
import ch.algotrader.visitor.InitializationVisitor;

/**
 * A generic Data Access Object providing Hibernate lookup methods.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class GenericDaoImpl extends AbstractDao<BaseEntityI> implements GenericDao {

    private final SessionFactory sessionFactory;
    private final TransactionTemplate txTemplate;

    public GenericDaoImpl(final SessionFactory sessionFactory, final TransactionTemplate txTemplate) {

        super(BaseEntityI.class, sessionFactory);

        Validate.notNull(sessionFactory, "SessionFactory is null");
        Validate.notNull(txTemplate, "TransactionTemplate is null");

        this.sessionFactory = sessionFactory;
        this.txTemplate = txTemplate;
    }

    @Override
    public BaseEntityI get(final Class<? extends BaseEntityI> clazz, final long id) {

        return this.txTemplate.execute(txStatus -> {

            Session session = this.sessionFactory.getCurrentSession();
            BaseEntityI result = (BaseEntityI) session.get(clazz, id);

            // initialize Entities
            if (result != null) {
                result.accept(InitializationVisitor.INSTANCE, HibernateInitializer.INSTANCE);
            }

            return result;
        });
    }

    @Override
    public <T> List<T> find(Class<T> clazz, final String queryString, final QueryType type, final NamedParam... namedParams) {

        return find(clazz, queryString, 0, type, namedParams);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> find(Class<T> clazz, final String queryString, final int maxResults, final QueryType type, final NamedParam... namedParams) {

        return this.txTemplate.execute(txStatus -> {
            return (List<T>) super.findObjects(null, queryString, maxResults, type, namedParams);
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T findUnique(Class<T> clazz, final String queryString, final QueryType type, final NamedParam... namedParams) {

        return this.txTemplate.execute(txStatus -> {
            return (T) super.findUniqueObject(null, queryString, type, namedParams);
        });
    }

    @Override
    public Object getInitializedCollection(final String role, final long id) {

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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Set<String> getQuerySpaces(String queryString) {

        SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) this.sessionFactory;
        return sessionFactoryImpl.getQueryPlanCache().getHQLQueryPlan(queryString, false, new HashMap()).getQuerySpaces();
    }

    @Override
    public String getNamedQuery(String queryName) {

        SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) this.sessionFactory;
        return sessionFactoryImpl.getNamedQuery(queryName).getQueryString();
    }

    @Override
    public int getDiscriminatorValue(final Class<?> type) {

        return HibernateUtil.getDisriminatorValue(this.sessionFactory, type);
    }
}
