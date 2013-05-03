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
package com.algoTrader.cache;

import java.io.Serializable;

import org.hibernate.EntityMode;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.impl.SessionImpl;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.collection.CollectionPersister;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class GenericDao extends HibernateDaoSupport {

    public Object get(Class<?> clazz, Serializable id) {

        return getHibernateTemplate().get(clazz, id);
    }

    public Object getInitializedCollection(String role, Serializable id) {

        SessionImpl session = (SessionImpl) getSession();
        SessionFactoryImpl sessionFactory = (SessionFactoryImpl) session.getSessionFactory();
        CollectionPersister persister = sessionFactory.getCollectionPersister(role);

        // load the owner entity
        ClassMetadata ownerMetadata = persister.getOwnerEntityPersister().getClassMetadata();
        Object owner = session.get(ownerMetadata.getEntityName(), id);

        // owner does not exist anymore so no point in updating the collection
        if (owner == null) {
            return null;
        }

        // get the collection by it's property name
        Object obj = ownerMetadata.getPropertyValue(owner, persister.getNodeName(), EntityMode.POJO);

        // if it is a PersistentCollection make sure it is initialized
        if (obj instanceof PersistentCollection) {

            PersistentCollection collection = (PersistentCollection) obj;
            if (!collection.wasInitialized()) {
                collection.forceInitialization();
            }
        }

        return obj;
    }
}
