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
package ch.algotrader.dbunit;

import java.sql.Connection;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Settings;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.impl.SessionFactoryImpl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import ch.algotrader.ServiceLocator;

public abstract class AbstractDaoTestCase {

    private static SessionFactory sessionFactory;
    protected static Connection connection;
    protected Session session;

    @BeforeClass
    public static void setupDatabase() throws Exception {

        ServiceLocator.instance().init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);

        sessionFactory = ServiceLocator.instance().getService("sessionFactory", SessionFactory.class);
        connection = getConnection(sessionFactory);
    }

    @AfterClass
    public static void closeDatabase() throws Exception {

        if (connection != null) {

            connection.close();
            connection = null;
        }
    }

    @Before
    public void openSession() {

        this.session = SessionFactoryUtils.getNewSession(sessionFactory);
        TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(this.session));
    }

    @After
    public void closeSession() {

        assert this.session != null;
        this.session.close();
        TransactionSynchronizationManager.unbindResource(sessionFactory);
    }

    public static Connection getConnection(SessionFactory sessionFactory) throws Exception {

        Connection connection = null;
        if (sessionFactory instanceof SessionFactoryImpl) {
            SessionFactoryImpl sfi = (SessionFactoryImpl) sessionFactory;
            Settings settings = sfi.getSettings();
            ConnectionProvider provider = settings.getConnectionProvider();
            connection = provider.getConnection();
        }
        return connection;
    }

    protected void beginTransaction() {

        assert this.session != null;
        this.session.getTransaction().begin();
    }

    protected void commitTransaction() {

        assert this.session != null;
        this.session.getTransaction().commit();
    }

    protected void commitTransaction(boolean clearContext) {

        commitTransaction();
        if (clearContext) {
            this.session.clear();
        }
    }
}
