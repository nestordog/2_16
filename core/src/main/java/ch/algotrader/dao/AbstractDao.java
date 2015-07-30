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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.Criteria;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import ch.algotrader.entity.BaseEntityI;
import ch.algotrader.enumeration.QueryType;

/**
 * Abstract generic DAO class that serves as a base for specific entity DAOs.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class AbstractDao<E extends BaseEntityI> {

    private static final int NO_LIMIT = 0;

    private final SessionFactory sessionFactory;
    private final Class<? extends E> entityClass;

    public AbstractDao(final Class<? extends E> entityClass, final SessionFactory sessionFactory) {
        Validate.notNull(entityClass, "Entity class is null");
        Validate.notNull(sessionFactory, "Session factory class is null");
        this.sessionFactory = sessionFactory;
        this.entityClass = entityClass;
    }

    protected Session getCurrentSession() {

        return this.sessionFactory.getCurrentSession();
    }

    protected  Class<? extends E> getEntityClass()  {
        return this.entityClass;
    }

    public E get(final long id) {
        return get(id, null);
    }

    public E get(final long id, final LockOptions lockOptions) {

        Session currentSession = getCurrentSession();
        Object result;
        if (lockOptions == null) {
            result = currentSession.get(this.entityClass, id);
        } else {
            result = currentSession.get(this.entityClass, id, lockOptions);
        }
        if (result != null) {
            return this.entityClass.cast(result);
        } else {
            return null;
        }
    }

    public E getLocked(long id) {
        return get(id, LockOptions.UPGRADE);
    }

    public E load(final long id) {
        return get(id, null);
    }

    public E load(final long id, final LockOptions lockOptions) {

        Session currentSession = getCurrentSession();
        Object result;
        if (lockOptions == null) {
            result = currentSession.load(this.entityClass, id);
        } else {
            result = currentSession.load(this.entityClass, id, lockOptions);
        }
        return this.entityClass.cast(result);
    }

    public E loadLocked(long id) {
        return load(id, LockOptions.UPGRADE);
    }

    public void lock(final E entity, final LockOptions lockOptions) {

        Session currentSession = getCurrentSession();
        Session.LockRequest lockRequest = currentSession.buildLockRequest(lockOptions);
        lockRequest.lock(entity);
    }

    public void lock(E entity) {

        lock(entity, LockOptions.UPGRADE);
    }

    public E persist(final E entity) {

        Session currentSession = getCurrentSession();
        if (entity.getId() == 0) {
            currentSession.save(entity);
        } else if (!currentSession.contains(entity)) {
            return this.entityClass.cast(currentSession.merge(entity));
        }
        return entity;
    }

    public void save(final E entity) {

        Session currentSession = getCurrentSession();
        currentSession.saveOrUpdate(entity);
    }

    public void saveAll(final Collection<E> entities) {

        Session currentSession = getCurrentSession();
        for (E entity: entities) {

            currentSession.saveOrUpdate(entity);
        }
    }

    protected static Long convertId(final Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Long) {
            return (Long) obj;
        } else if (obj instanceof BigInteger) {
            return ((BigInteger) obj).longValue();
        } else if (obj instanceof Integer) {
            return ((Integer) obj).longValue();
        } else {
            throw new IllegalStateException("Unexpected id class: " + obj.getClass());
        }
    }

    protected static List<Long> convertIds(final List<?> objects) {
        if (objects == null || objects.isEmpty()) {
            return Collections.emptyList();
        }
        final List<Long> ids = new ArrayList<>(objects.size());
        for (int i = 0; i < objects.size(); i++) {
            ids.add(convertId(objects.get(i)));
        }
        return ids;
    }

    protected static <T> List<T> convertToList(final List<?> list, final Class<? extends T> clazz) {

        List<T> results = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            T entity = clazz.cast(list.get(i));
            results.add(entity);
        }
        return results;
    }

    protected static <T> Set<T> convertToSet(final List<?> list, final Class<? extends T> clazz) {

        Set<T> results = new LinkedHashSet<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            T entity = clazz.cast(list.get(i));
            results.add(entity);
        }
        return results;
    }

    private void applyParameters(final Query query, final Object... params) {
        for (int i = 0; i < params.length; i++) {
            query.setParameter(i, params[i]);
        }
    }

    private void applyParameters(final Query query, final NamedParam... params) {
        for (NamedParam param : params) {
            query.setParameter(param.getName(), param.getValue());
        }
    }

    private Query createQuery(final String queryString, final QueryType type) {

        Session currentSession = getCurrentSession();
        switch (type) {
            case HQL:
                return currentSession.createQuery(queryString);
            case SQL:
                return currentSession.createSQLQuery(queryString);
            case BY_NAME:
                return currentSession.getNamedQuery(queryString);
            default:
                throw new IllegalStateException("Unexpected query type: " + type);
        }
    }

    protected Query prepareQuery(final LockOptions lockOptions, final String queryString, final int maxResults, final QueryType type) {

        Query query = createQuery(queryString, type);
        if (lockOptions != null) {
            query.setLockOptions(lockOptions);
        }
        if (maxResults > 0) {
            query.setMaxResults(maxResults);
        }
        return query;
    }

    protected Query prepareQuery(final LockOptions lockOptions, final String queryString, final int maxResults, final QueryType type, final Object... params) {

        Query query = createQuery(queryString, type);
        if (lockOptions != null) {
            query.setLockOptions(lockOptions);
        }
        if (maxResults > 0) {
            query.setMaxResults(maxResults);
        }
        applyParameters(query, params);
        return query;
    }

    protected Query prepareQuery(final LockOptions lockOptions, final String queryString, final int maxResults, final QueryType type, final NamedParam... params) {

        Query query = createQuery(queryString, type);
        if (lockOptions != null) {
            query.setLockOptions(lockOptions);
        }
        if (maxResults > 0) {
            query.setMaxResults(maxResults);
        }
        applyParameters(query, params);
        return query;
    }

    protected Query prepareQuery(final LockOptions lockOptions, final String queryString, final boolean cacheable, final int maxResults, final QueryType type) {

        Query query = createQuery(queryString, type);
        if (lockOptions != null) {
            query.setLockOptions(lockOptions);
        }
        query.setCacheable(cacheable);
        if (maxResults > 0) {
            query.setMaxResults(maxResults);
        }
        return query;
    }

    protected Query prepareQuery(final LockOptions lockOptions, final String queryString, final boolean cacheable, final int maxResults, final QueryType type, final Object... params) {

        Query query = createQuery(queryString, type);
        if (lockOptions != null) {
            query.setLockOptions(lockOptions);
        }
        query.setCacheable(cacheable);
        if (maxResults > 0) {
            query.setMaxResults(maxResults);
        }
        applyParameters(query, params);
        return query;
    }

    protected Query prepareQuery(final LockOptions lockOptions, final String queryString, final boolean cacheable, final int maxResults, final QueryType type, final NamedParam... params) {

        Query query = createQuery(queryString, type);
        if (lockOptions != null) {
            query.setLockOptions(lockOptions);
        }
        query.setCacheable(cacheable);
        if (maxResults > 0) {
            query.setMaxResults(maxResults);
        }
        applyParameters(query, params);
        return query;
    }

    protected SQLQuery prepareSQLQuery(final LockOptions lockOptions, final String queryString, final int maxResults) {

        Session currentSession = getCurrentSession();
        SQLQuery query = currentSession.createSQLQuery(queryString);
        if (lockOptions != null) {
            query.setLockOptions(lockOptions);
        }
        if (maxResults > 0) {
            query.setMaxResults(maxResults);
        }
        return query;
    }

    protected SQLQuery prepareSQLQuery(final LockOptions lockOptions, final String queryString, final int maxResults, final Object... params) {

        Session currentSession = getCurrentSession();
        SQLQuery query = currentSession.createSQLQuery(queryString);
        if (lockOptions != null) {
            query.setLockOptions(lockOptions);
        }
        if (maxResults > 0) {
            query.setMaxResults(maxResults);
        }
        applyParameters(query, params);
        return query;
    }

    protected SQLQuery prepareSQLQuery(final LockOptions lockOptions, final String queryString, final int maxResults, final NamedParam... params) {

        Session currentSession = getCurrentSession();
        SQLQuery query = currentSession.createSQLQuery(queryString);
        if (lockOptions != null) {
            query.setLockOptions(lockOptions);
        }
        if (maxResults > 0) {
            query.setMaxResults(maxResults);
        }
        applyParameters(query, params);
        return query;
    }

    protected Query prepareQuery(final LockOptions lockOptions, final String queryString, final QueryType type) {

        Session currentSession = getCurrentSession();
        Query query = createQuery(queryString, type);
        if (lockOptions != null) {
            query.setLockOptions(lockOptions);
        }
        return query;
    }

    protected Query prepareQuery(final LockOptions lockOptions, final String queryString, final QueryType type, final Object... params) {

        Session currentSession = getCurrentSession();
        Query query = createQuery(queryString, type);
        if (lockOptions != null) {
            query.setLockOptions(lockOptions);
        }
        applyParameters(query, params);
        return query;
    }

    protected Query prepareQuery(final LockOptions lockOptions, final String queryString, final QueryType type, final NamedParam... params) {

        Session currentSession = getCurrentSession();
        Query query = createQuery(queryString, type);
        if (lockOptions != null) {
            query.setLockOptions(lockOptions);
        }
        applyParameters(query, params);
        return query;
    }

    protected SQLQuery prepareSQLQuery(final LockOptions lockOptions, final String queryString) {

        return prepareSQLQuery(lockOptions, queryString, NO_LIMIT);
    }

    protected SQLQuery prepareSQLQuery(final LockOptions lockOptions, final String queryString, final Object... params) {

        return prepareSQLQuery(lockOptions, queryString, NO_LIMIT, params);
    }

    protected SQLQuery prepareSQLQuery(final LockOptions lockOptions, final String queryString, final NamedParam... params) {

        return prepareSQLQuery(lockOptions, queryString, NO_LIMIT, params);
    }

    protected List<?> findObjects(final LockOptions lockOptions, final String queryString, final QueryType type) {

        return prepareQuery(lockOptions, queryString, type).list();
    }

    protected List<?> findObjects(final LockOptions lockOptions, final String queryString, final QueryType type, final Object... params) {

        return prepareQuery(lockOptions, queryString, type, params).list();
    }

    protected List<?> findObjects(final LockOptions lockOptions, final String queryString, final QueryType type, final NamedParam... params) {

        return prepareQuery(lockOptions, queryString, type, params).list();
    }

    protected List<?> findObjects(final LockOptions lockOptions, final String queryString, final int maxResults, final QueryType type) {

        return prepareQuery(lockOptions, queryString, maxResults, type).list();
    }

    protected List<?> findObjects(final LockOptions lockOptions, final String queryString, final int maxResults, final QueryType type, final Object... params) {

        return prepareQuery(lockOptions, queryString, maxResults, type, params).list();
    }

    protected List<?> findObjects(final LockOptions lockOptions, final String queryString, final int maxResults, final QueryType type, final NamedParam... params) {

        return prepareQuery(lockOptions, queryString, maxResults, type, params).list();
    }

    protected List<?> findObjectsCaching(final LockOptions lockOptions, final String queryString, final QueryType type) {

        return prepareQuery(lockOptions, queryString, true, NO_LIMIT, type).list();
    }

    protected List<?> findObjectsCaching(final LockOptions lockOptions, final String queryString, final QueryType type, final Object... params) {

        return prepareQuery(lockOptions, queryString, true, NO_LIMIT, type, params).list();
    }

    protected List<?> findObjectsCaching(final LockOptions lockOptions, final String queryString, final QueryType type, final NamedParam... params) {

        return prepareQuery(lockOptions, queryString, true, NO_LIMIT, type, params).list();
    }

    protected List<?> findObjectsCaching(final LockOptions lockOptions, final String queryString, final int maxResults, final QueryType type) {

        return prepareQuery(lockOptions, queryString, true, maxResults, type).list();
    }

    protected List<?> findObjectsCaching(final LockOptions lockOptions, final String queryString, final int maxResults, final QueryType type, final Object... params) {

        return prepareQuery(lockOptions, queryString, true, maxResults, type, params).list();
    }

    protected List<?> findObjectsCaching(final LockOptions lockOptions, final String queryString, final int maxResults, final QueryType type, final NamedParam... params) {

        return prepareQuery(lockOptions, queryString, true, maxResults, type, params).list();
    }

    protected Object findUniqueObject(final LockOptions lockOptions, final String queryString, final QueryType type) {

        return prepareQuery(lockOptions, queryString, type).uniqueResult();
    }

    protected Object findUniqueObject(final LockOptions lockOptions, final String queryString, final QueryType type, final Object... params) {

        return prepareQuery(lockOptions, queryString, type, params).uniqueResult();
    }

    protected Object findUniqueObject(final LockOptions lockOptions, final String queryString, final QueryType type, final NamedParam... params) {

        return prepareQuery(lockOptions, queryString, type, params).uniqueResult();
    }

    protected E findUnique(final LockOptions lockOptions, final String queryString, final QueryType type) {

        return this.entityClass.cast(prepareQuery(lockOptions, queryString, type).uniqueResult());
    }

    protected E findUnique(final LockOptions lockOptions, final String queryString, final QueryType type, final Object... params) {

        return this.entityClass.cast(prepareQuery(lockOptions, queryString, type, params).uniqueResult());
    }

    protected E findUnique(final LockOptions lockOptions, final String queryString, final QueryType type, final NamedParam... params) {

        return this.entityClass.cast(prepareQuery(lockOptions, queryString, type, params).uniqueResult());
    }

    protected E findUniqueCaching(final LockOptions lockOptions, final String queryString, final QueryType type) {

        return this.entityClass.cast(prepareQuery(lockOptions, queryString, true, NO_LIMIT, type).uniqueResult());
    }

    protected E findUniqueCaching(final LockOptions lockOptions, final String queryString, final QueryType type, final Object... params) {

        return this.entityClass.cast(prepareQuery(lockOptions, queryString, true, NO_LIMIT, type, params).uniqueResult());
    }

    protected E findUniqueCaching(final LockOptions lockOptions, final String queryString, final QueryType type, final NamedParam... params) {

        return this.entityClass.cast(prepareQuery(lockOptions, queryString, true, NO_LIMIT, type, params).uniqueResult());
    }

    protected E findUniqueCaching(final LockOptions lockOptions, final String queryString, final int maxResults, final QueryType type) {

        return this.entityClass.cast(prepareQuery(lockOptions, queryString, true, maxResults, type).uniqueResult());
    }

    protected E findUniqueCaching(final LockOptions lockOptions, final String queryString, final int maxResults, final QueryType type, final Object... params) {

        return this.entityClass.cast(prepareQuery(lockOptions, queryString, true, maxResults, type, params).uniqueResult());
    }

    protected E findUniqueCaching(final LockOptions lockOptions, final String queryString, final int maxResults, final QueryType type, final NamedParam... params) {

        return this.entityClass.cast(prepareQuery(lockOptions, queryString, true, maxResults, type, params).uniqueResult());
    }

    protected E findUnique(final String queryString, final QueryType type) {

        return findUnique(null, queryString, type);
    }

    protected E findUnique(final String queryString, final QueryType type, final Object... params) {

        return findUnique(null, queryString, type, params);
    }

    protected E findUnique(final String queryString, final QueryType type, final NamedParam... params) {

        return findUnique(null, queryString, type, params);
    }

    protected E findUniqueCaching(final String queryString, final QueryType type) {

        return findUniqueCaching(null, queryString, type);
    }

    protected E findUniqueCaching(final String queryString, final QueryType type, final Object... params) {

        return findUniqueCaching(null, queryString, type, params);
    }

    protected E findUniqueCaching(final String queryString, final QueryType type, final NamedParam... params) {

        return findUniqueCaching(null, queryString, type, params);
    }

    protected Object findUniqueSQL( final LockOptions lockOptions, final String queryString, final QueryType type) {

        return prepareSQLQuery(lockOptions, queryString, type).uniqueResult();
    }

    protected Object findUniqueSQL( final LockOptions lockOptions, final String queryString, final QueryType type, final Object... params) {

        return prepareSQLQuery(lockOptions, queryString, type, params).uniqueResult();
    }

    protected Object findUniqueSQL( final LockOptions lockOptions, final String queryString, final QueryType type, final NamedParam... params) {

        return prepareSQLQuery(lockOptions, queryString, type, params).uniqueResult();
    }

    protected Object findUniqueSQL( final String queryString, final QueryType type) {

        return findUniqueSQL(null, queryString, type);
    }

    protected Object findUniqueSQL( final String queryString, final QueryType type, final Object... params) {

        return findUniqueSQL(null, queryString, type, params);
    }

    protected Object findUniqueSQL( final String queryString, final QueryType type, final NamedParam... params) {

        return findUniqueSQL(null, queryString, type, params);
    }

    @SuppressWarnings("unchecked")
    protected List<E> find(final LockOptions lockOptions, final String queryString, final int maxResults, final QueryType type) {

        return (List<E>) findObjects(lockOptions, queryString, maxResults, type);
    }

    @SuppressWarnings("unchecked")
    protected List<E> find(final LockOptions lockOptions, final String queryString, final int maxResults, final QueryType type, final Object... params) {

        return (List<E>) findObjects(lockOptions, queryString, maxResults, type, params);
    }

    @SuppressWarnings("unchecked")
    protected List<E> find(final LockOptions lockOptions, final String queryString, final int maxResults, final QueryType type, final NamedParam... params) {

        return (List<E>) findObjects(lockOptions, queryString, maxResults, type, params);
    }

    @SuppressWarnings("unchecked")
    protected List<E> findCaching(final LockOptions lockOptions, final String queryString, final QueryType type) {

        return (List<E>) findObjectsCaching(lockOptions, queryString, type);
    }

    @SuppressWarnings("unchecked")
    protected List<E> findCaching(final LockOptions lockOptions, final String queryString, final QueryType type, final Object... params) {

        return (List<E>) findObjectsCaching(lockOptions, queryString, type, params);
    }

    @SuppressWarnings("unchecked")
    protected List<E> findCaching(final LockOptions lockOptions, final String queryString, final QueryType type, final NamedParam... params) {

        return (List<E>) findObjectsCaching(lockOptions, queryString, type, params);
    }

    @SuppressWarnings("unchecked")
    protected List<E> findCaching(final LockOptions lockOptions, final String queryString, final int maxResults, final QueryType type) {

        return (List<E>) findObjectsCaching(lockOptions, queryString, maxResults, type);
    }

    @SuppressWarnings("unchecked")
    protected List<E> findCaching(final LockOptions lockOptions, final String queryString, final int maxResults, final QueryType type, final Object... params) {

        return (List<E>) findObjectsCaching(lockOptions, queryString, maxResults, type, params);
    }

    @SuppressWarnings("unchecked")
    protected List<E> findCaching(final LockOptions lockOptions, final String queryString, final int maxResults, final QueryType type, final NamedParam... params) {

        return (List<E>) findObjectsCaching(lockOptions, queryString, maxResults, type, params);
    }

    @SuppressWarnings("unchecked")
    protected List<E> find(final LockOptions lockOptions, final String queryString, final QueryType type) {

        return (List<E>) findObjects(lockOptions, queryString, type);
    }

    @SuppressWarnings("unchecked")
    protected List<E> find(final LockOptions lockOptions, final String queryString, final QueryType type, final Object... params) {

        return (List<E>) findObjects(lockOptions, queryString, type, params);
    }

    @SuppressWarnings("unchecked")
    protected List<E> find(final LockOptions lockOptions, final String queryString, final QueryType type, final NamedParam... params) {

        return (List<E>) findObjects(lockOptions, queryString, type, params);
    }

    @SuppressWarnings("unchecked")
    protected List<E> find(final String queryString, final int maxResults, final QueryType type) {

        return (List<E>) findObjects(null, queryString, maxResults, type);
    }

    @SuppressWarnings("unchecked")
    protected List<E> find(final String queryString, final int maxResults, final QueryType type, final Object... params) {

        return (List<E>) findObjects(null, queryString, maxResults, type, params);
    }

    @SuppressWarnings("unchecked")
    protected List<E> find(final String queryString, final int maxResults, final QueryType type, final NamedParam... params) {

        return (List<E>) findObjects(null, queryString, maxResults, type, params);
    }

    @SuppressWarnings("unchecked")
    protected List<E> findCaching(final String queryString, final QueryType type) {

        return (List<E>) findObjectsCaching(null, queryString, type);
    }

    @SuppressWarnings("unchecked")
    protected List<E> findCaching(final String queryString, final QueryType type, final Object... params) {

        return (List<E>) findObjectsCaching(null, queryString, type, params);
    }

    @SuppressWarnings("unchecked")
    protected List<E> findCaching(final String queryString, final QueryType type, final NamedParam... params) {

        return (List<E>) findObjectsCaching(null, queryString, type, params);
    }

    @SuppressWarnings("unchecked")
    protected List<E> findCaching(final String queryString,final int maxResults,  final QueryType type) {

        return (List<E>) findObjectsCaching(null, queryString, maxResults, type);
    }

    @SuppressWarnings("unchecked")
    protected List<E> findCaching(final String queryString, final int maxResults, final QueryType type, final Object... params) {

        return (List<E>) findObjectsCaching(null, queryString, maxResults, type, params);
    }

    @SuppressWarnings("unchecked")
    protected List<E> findCaching(final String queryString, final int maxResults, final QueryType type, final NamedParam... params) {

        return (List<E>) findObjectsCaching(null, queryString, maxResults, type, params);
    }

    @SuppressWarnings("unchecked")
    protected List<E> find(final String queryString, final QueryType type) {

        return (List<E>) findObjects(null, queryString, type);
    }

    @SuppressWarnings("unchecked")
    protected List<E> find(final String queryString, final QueryType type, final Object... params) {

        return (List<E>) findObjects(null, queryString, type, params);
    }

    @SuppressWarnings("unchecked")
    protected List<E> find(final String queryString, final QueryType type, final NamedParam... params) {

        return (List<E>) findObjects(null, queryString, type, params);
    }

    private <V> List<V> convert(Iterator it, EntityConverter<E, V> converter) {

        List<V> result = new ArrayList<>();
        while (it.hasNext()) {

            E entity = this.entityClass.cast(it.next());
            result.add(converter.convert(entity));
        }
        return result;
    }

    protected <V> List<V> find(final EntityConverter<E, V> converter, final String queryString, final int maxResults, final QueryType type) {

        Query query = prepareQuery(null, queryString, maxResults, type);
        return convert(query.iterate(), converter);
    }

    protected <V> List<V> find(final EntityConverter<E, V> converter, final String queryString, final int maxResults, final QueryType type, final Object... params) {

        Query query = prepareQuery(null, queryString, maxResults, type, params);
        return convert(query.iterate(), converter);
    }

    protected <V> List<V> find(final EntityConverter<E, V> converter, final String queryString, final int maxResults, final QueryType type, final NamedParam... params) {

        Query query = prepareQuery(null, queryString, maxResults, type, params);
        return convert(query.iterate(), converter);
    }

    protected <V> List<V> find(final EntityConverter<E, V> converter, final String queryString, final QueryType type) {

        Query query = prepareQuery(null, queryString, type);
        return convert(query.iterate(), converter);
    }

    protected <V> List<V> find(final EntityConverter<E, V> converter, final String queryString, final QueryType type, final Object... params) {

        Query query = prepareQuery(null, queryString, type, params);
        return convert(query.iterate(), converter);
    }

    protected <V> List<V> find(final EntityConverter<E, V> converter, final String queryString, final QueryType type, final NamedParam... params) {

        Query query = prepareQuery(null, queryString, type, params);
        return convert(query.iterate(), converter);
    }

    protected Set<E> findAsSet(final LockOptions lockOptions, final String queryString, final QueryType type) {

        return convertToSet(findObjects(lockOptions, queryString, type), this.entityClass);
    }

    protected Set<E> findAsSet(final LockOptions lockOptions, final String queryString, final QueryType type, final Object... params) {

        return convertToSet(findObjects(lockOptions, queryString, type, params), this.entityClass);
    }

    protected Set<E> findAsSet(final LockOptions lockOptions, final String queryString, final QueryType type, final NamedParam... params) {

        return convertToSet(findObjects(lockOptions, queryString, type, params), this.entityClass);
    }

    protected Set<E> findAsSet(final String queryString, final QueryType type) {

        return convertToSet(findObjects(null, queryString, type), this.entityClass);
    }

    protected Set<E> findAsSet(final String queryString, final QueryType type, final Object... params) {

        return convertToSet(findObjects(null, queryString, type, params), this.entityClass);
    }

    protected Set<E> findAsSet(final String queryString, final QueryType type, final NamedParam... params) {

        return convertToSet(findObjects(null, queryString, type, params), this.entityClass);
    }

    protected Set<E> findAsSetCaching(final LockOptions lockOptions, final String queryString, final QueryType type) {

        return convertToSet(findObjectsCaching(lockOptions, queryString, type), this.entityClass);
    }

    protected Set<E> findAsSetCaching(final LockOptions lockOptions, final String queryString, final QueryType type, final Object... params) {

        return convertToSet(findObjectsCaching(lockOptions, queryString, type, params), this.entityClass);
    }

    protected Set<E> findAsSetCaching(final LockOptions lockOptions, final String queryString, final QueryType type, final NamedParam... params) {

        return convertToSet(findObjectsCaching(lockOptions, queryString, type, params), this.entityClass);
    }

    protected Set<E> findAsSetCaching(final String queryString, final QueryType type) {

        return convertToSet(findObjectsCaching(null, queryString, type), this.entityClass);
    }

    protected Set<E> findAsSetCaching(final String queryString, final QueryType type, final Object... params) {

        return convertToSet(findObjectsCaching(null, queryString, type, params), this.entityClass);
    }

    protected Set<E> findAsSetCaching(final String queryString, final QueryType type, final NamedParam... params) {

        return convertToSet(findObjectsCaching(null, queryString, type, params), this.entityClass);
    }

    private Query createLoadAllQuery() {

        Session currentSession = getCurrentSession();
        Query query = currentSession.createQuery("from " + this.entityClass.getSimpleName());
        query.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return query;
    }

    @SuppressWarnings("unchecked")
    public List<E> loadAll() {

        return createLoadAllQuery().list();
    }

    public <V> List<V> loadAll(EntityConverter<E, V> converter) {

        Validate.notNull(converter, "Entity converter is null");

        Query query = createLoadAllQuery();
        return convert(query.iterate(), converter);
    }

    public void delete(final E entity) {

        Session currentSession = getCurrentSession();
        currentSession.delete(entity);
    }

    public void deleteAll(final Collection<E> entities) {

        for (E entity: entities) {

            delete(entity);
        }
    }

    public boolean deleteById (final long id) {

        Session currentSession = getCurrentSession();
        Query query = currentSession.createQuery("delete from " + this.entityClass.getSimpleName() +  " where id = :id");
        query.setParameter("id", id);
        int result = query.executeUpdate();
        if (result > 0) {

            // Need to clear the session to ensure deleted entities are not stuck in the session cache
            currentSession.clear();
            return true;
        } else {

            return false;
        }
    }

    public void flush() {

        Session currentSession = getCurrentSession();
        currentSession.flush();
    }

}
