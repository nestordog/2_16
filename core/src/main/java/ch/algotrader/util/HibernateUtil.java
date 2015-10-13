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
import org.hibernate.SessionFactory;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.internal.SessionImpl;
import org.hibernate.persister.entity.AbstractEntityPersister;

/**
 * Provides static Hibernate utility methods.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class HibernateUtil {

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
}
