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
package ch.algotrader.entity;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import ch.algotrader.ServiceLocator;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class EntityTest {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void beforeEntityTestClass() {

        ServiceLocator.instance().init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);
        sessionFactory = ServiceLocator.instance().getService("sessionFactory", SessionFactory.class);
    }

    @Before
    public void beforeEntityTest() {

        this.session = SessionFactoryUtils.getNewSession(sessionFactory);
        TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(this.session));
    }

    @After
    public void afterEntityTest() {

        this.session.close();
        TransactionSynchronizationManager.unbindResource(sessionFactory);
    }
}
