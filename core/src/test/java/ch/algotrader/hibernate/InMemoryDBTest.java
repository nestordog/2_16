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

import org.apache.commons.io.Charsets;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Base class for unit tests involving an embedded H2 database initialized
 * with the Algotrader schema.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class InMemoryDBTest {

    protected SessionFactory sessionFactory;

    protected Session session;

    @Before
    public void setup() throws Exception {

        ResourceDatabasePopulator dbPopulator = new ResourceDatabasePopulator();
        dbPopulator.addScript(new ClassPathResource("/db/h2/h2.sql"));
        DatabasePopulatorUtils.execute(dbPopulator, EmbeddedTestDB.DATABASE.getDataSource());

        this.sessionFactory = EmbeddedTestDB.DATABASE.getSessionFactory();

        this.session = SessionFactoryUtils.getNewSession(this.sessionFactory);

        TransactionSynchronizationManager.bindResource(this.sessionFactory, new SessionHolder(this.session));
    }

    @After
    public void cleanup() throws Exception {

        ResourceDatabasePopulator dbPopulator = new ResourceDatabasePopulator();
        dbPopulator.addScript(new ByteArrayResource("DROP ALL OBJECTS".getBytes(Charsets.US_ASCII)));

        DatabasePopulatorUtils.execute(dbPopulator, EmbeddedTestDB.DATABASE.getDataSource());

        if (this.sessionFactory != null) {

            TransactionSynchronizationManager.unbindResource(EmbeddedTestDB.DATABASE.getSessionFactory());
        }
        if (this.session != null) {

            this.session.close();
        }
    }

}
