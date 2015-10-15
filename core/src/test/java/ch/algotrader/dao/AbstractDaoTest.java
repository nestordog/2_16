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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.Charsets;
import org.hibernate.LockOptions;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.PropertyValueException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.LongType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.hibernate4.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import ch.algotrader.entity.GenericItem;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.QueryType;
import ch.algotrader.hibernate.EmbeddedTestDB;

/**
* Unit tests for {@link ch.algotrader.dao.AbstractDao}.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*
* @version $Revision$ $Date$
*/
public class AbstractDaoTest {

    protected static EmbeddedTestDB DATABASE;

    protected Session session;

    private GenericItemDao dao;

    @BeforeClass
    public static void setupDB() throws Exception {

        DATABASE = new EmbeddedTestDB(new ClassPathResource("ch/algotrader/entity/GenericItem.hbm.xml"));
    }

    @AfterClass
    public static void shutdownDB() {

        if (DATABASE != null) {

            DATABASE.shutdown();
        }
    }

    @Before
    public void setup() throws Exception {

        SessionFactory sessionFactory = DATABASE.getSessionFactory();
        this.session = sessionFactory.openSession();

        TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(this.session));

        this.dao = new GenericItemDao(sessionFactory);
    }

    @After
    public void cleanup() throws Exception {

        if (this.session != null) {

            this.session.close();

            ResourceDatabasePopulator dbPopulator = new ResourceDatabasePopulator();
            dbPopulator.addScript(new ByteArrayResource("TRUNCATE TABLE GenericItem".getBytes(Charsets.US_ASCII)));

            DatabasePopulatorUtils.execute(dbPopulator, DATABASE.getDataSource());
            TransactionSynchronizationManager.unbindResource(DATABASE.getSessionFactory());
        }
    }

    @Test
    public void testEntityIdentity() throws Exception {

        GenericItem stuff1 = new GenericItem("this");
        stuff1.setActive(true);
        stuff1.setBroker(Broker.DC.name());

        this.dao.save(stuff1);

        long id1 = stuff1.getId();

        Assert.assertNotEquals(0, id1);

        GenericItem stuff2 = new GenericItem("that");
        stuff2.setActive(true);
        stuff2.setBroker(Broker.IB.name());

        this.dao.save(stuff2);

        long id2 = stuff2.getId();

        Assert.assertNotEquals(0, id2);

        this.dao.flush();

        GenericItem stuff3 = this.dao.load(id1);

        GenericItem stuff4 = this.dao.load(id2);

        Assert.assertTrue(stuff1.equals(stuff1));
        Assert.assertFalse(stuff1.equals(stuff2));
        Assert.assertTrue(stuff1.equals(stuff3));
        Assert.assertSame(stuff1, stuff3);
        Assert.assertSame(stuff2, stuff4);

        List<GenericItem> allGenericItem = this.dao.loadAll();

        Assert.assertNotNull(allGenericItem);

        Assert.assertEquals(2, allGenericItem.size());

        Set<GenericItem> allUniqueGenericItem = new HashSet<>(allGenericItem);

        Assert.assertNotNull(allUniqueGenericItem);

        Assert.assertEquals(2, allUniqueGenericItem.size());
        Assert.assertTrue(allUniqueGenericItem.contains(stuff1));
        Assert.assertTrue(allUniqueGenericItem.contains(stuff2));
    }

    @Test(expected = PropertyValueException.class)
    public void testEntityMandatoryField() throws Exception {

        GenericItem stuff1 = new GenericItem("this");
        stuff1.setActive(true);
        stuff1.setBroker(null);

        this.dao.persist(stuff1);
    }

    @Test
    public void testLockEntity() throws Exception {

        GenericItem stuff1 = new GenericItem("this");
        stuff1.setActive(true);
        stuff1.setBroker(Broker.DC.name());

        this.dao.save(stuff1);

        long id1 = stuff1.getId();

        Assert.assertNotEquals(0, id1);

        this.dao.flush();

        this.dao.lock(stuff1, LockOptions.UPGRADE);
    }

    @Test
    public void testSaveEntity() throws Exception {

        GenericItem stuff1 = new GenericItem("this");
        stuff1.setActive(true);
        stuff1.setBroker(Broker.DC.name());

        this.dao.save(stuff1);

        long id1 = stuff1.getId();

        Assert.assertNotEquals(0, id1);

        this.dao.flush();

        // Update persistent
        stuff1.setBroker(Broker.RT.name());

        this.dao.save(stuff1);
        this.dao.flush();

        // Update detached
        GenericItem detached = new GenericItem(id1, "this");
        detached.setActive(true);
        detached.setBroker(Broker.CNX.name());

        try {

            this.dao.save(detached);
            this.dao.flush();

            Assert.fail("NonUniqueObjectException expected");
        } catch (NonUniqueObjectException ignored) {

        }
    }

    @Test
    public void testPersistEntity() throws Exception {

        GenericItem stuff1 = new GenericItem("this");
        stuff1.setActive(true);
        stuff1.setBroker(Broker.DC.name());

        this.dao.persist(stuff1);

        long id1 = stuff1.getId();

        Assert.assertNotEquals(0, id1);

        this.dao.flush();

        // Update persistent
        stuff1.setBroker(Broker.RT.name());

        this.dao.persist(stuff1);
        this.dao.flush();

        // Update detached
        GenericItem detached = new GenericItem(id1, "this");
        detached.setActive(true);
        detached.setBroker(Broker.CNX.name());
        GenericItem stuff2 = this.dao.persist(detached);

        this.dao.flush();

        Assert.assertEquals(detached.getBroker(), stuff2.getBroker());
        Assert.assertEquals(stuff1.getBroker(), stuff2.getBroker());
    }

    @Test
    public void testLoadAll() throws Exception {

        GenericItem stuff1 = new GenericItem("this");
        stuff1.setActive(true);
        stuff1.setBroker(Broker.DC.name());

        this.dao.save(stuff1);

        long id1 = stuff1.getId();

        Assert.assertNotEquals(0, id1);

        GenericItem stuff2 = new GenericItem("that");
        stuff2.setActive(true);
        stuff2.setBroker(Broker.IB.name());

        this.dao.save(stuff2);

        long id2 = stuff2.getId();

        Assert.assertNotEquals(0, id2);

        GenericItem stuff3 = new GenericItem("this and that");
        stuff3.setActive(true);
        stuff3.setBroker(Broker.RT.name());

        this.dao.save(stuff3);

        long id3 = stuff3.getId();

        Assert.assertNotEquals(0, id3);

        this.dao.flush();

        List<GenericItem> stuffs = this.dao.loadAll();

        Assert.assertNotNull(stuffs);

        Assert.assertEquals(3, stuffs.size());
    }

    @Test
    public void testFindByQuery() throws Exception {

        GenericItem stuff1 = new GenericItem("this");
        stuff1.setActive(true);
        stuff1.setBroker(Broker.DC.name());

        this.dao.save(stuff1);

        long id1 = stuff1.getId();

        Assert.assertNotEquals(0, id1);

        GenericItem stuff2 = new GenericItem("that");
        stuff2.setActive(true);
        stuff2.setBroker(Broker.IB.name());

        this.dao.save(stuff2);

        long id2 = stuff2.getId();

        Assert.assertNotEquals(0, id2);

        GenericItem stuff3 = new GenericItem("this and that");
        stuff3.setActive(true);
        stuff3.setBroker(Broker.RT.name());

        this.dao.save(stuff3);

        long id3 = stuff3.getId();

        Assert.assertNotEquals(0, id3);

        this.dao.flush();

        List<GenericItem> list1 = this.dao.find("select s from GenericItem s where s.active = ?", QueryType.HQL, Boolean.TRUE);

        Assert.assertNotNull(list1);

        Assert.assertEquals(3, list1.size());

        Set<GenericItem> set1 = this.dao.findAsSet("select s from GenericItem s where s.active = ?", QueryType.HQL, Boolean.TRUE);

        Assert.assertNotNull(set1);

        Assert.assertEquals(3, set1.size());

        Set<GenericItem> set2 = this.dao.findAsSet("select s from GenericItem s where s.broker = ?", QueryType.HQL, Broker.IB.name());

        Assert.assertNotNull(set2);

        Assert.assertEquals(1, set2.size());
        Assert.assertEquals("that", set2.iterator().next().getName());
    }

    @Test
    public void testFindUniqueByQuery() throws Exception {

        GenericItem stuff1 = new GenericItem("this");
        stuff1.setActive(true);
        stuff1.setBroker(Broker.DC.name());

        this.dao.save(stuff1);

        long id1 = stuff1.getId();

        Assert.assertNotEquals(0, id1);

        GenericItem stuff2 = new GenericItem("that");
        stuff2.setActive(true);
        stuff2.setBroker(Broker.IB.name());

        this.dao.save(stuff2);

        long id2 = stuff2.getId();

        Assert.assertNotEquals(0, id2);

        GenericItem stuff3 = new GenericItem("this and that");
        stuff3.setActive(true);
        stuff3.setBroker(Broker.RT.name());

        this.dao.save(stuff3);

        long id3 = stuff3.getId();

        Assert.assertNotEquals(0, id3);

        this.dao.flush();

        GenericItem result1 = this.dao.findUnique("select s from GenericItem s where s.name = ?", QueryType.HQL, "this");

        Assert.assertNotNull(result1);

        Assert.assertSame(stuff1, result1);

        try {

            this.dao.findUnique("select s from GenericItem s where s.active = ?", QueryType.HQL, Boolean.TRUE);

            Assert.fail("NonUniqueResultException expected");
        } catch (NonUniqueResultException ignore) {

        }
    }

    @Test
    public void testFindBySQLQuery() throws Exception {

        GenericItem stuff1 = new GenericItem("this");
        stuff1.setActive(true);
        stuff1.setBroker(Broker.DC.name());

        this.dao.save(stuff1);

        long id1 = stuff1.getId();

        Assert.assertNotEquals(0, id1);

        this.dao.flush();

        List<?> list1 = this.dao.findObjects(null, "select s.name from GenericItem s where s.active = ?", QueryType.SQL, Boolean.TRUE);

        Assert.assertNotNull(list1);

        Assert.assertEquals(1, list1.size());
    }

    @Test
    public void testFindBySQLQueryAsEntity() throws Exception {

        GenericItem stuff1 = new GenericItem("this");
        stuff1.setActive(true);
        stuff1.setBroker(Broker.DC.name());

        this.dao.save(stuff1);

        long id1 = stuff1.getId();

        Assert.assertNotEquals(0, id1);

        this.dao.flush();

        SQLQuery query = this.dao.prepareSQLQuery(null, "select s.* from GenericItem s where s.active = ?", Boolean.TRUE);
        query.addEntity(GenericItem.class);

        final List<?> list1 = query.list();

        Assert.assertNotNull(list1);

        Assert.assertEquals(1, list1.size());

        GenericItem stuff = (GenericItem) list1.get(0);

        Assert.assertSame(stuff, stuff1);
    }

    @Test
    public void testFindByQueryCollectionParameter() throws Exception {

        GenericItem stuff1 = new GenericItem("this");
        stuff1.setActive(true);
        stuff1.setBroker(Broker.DC.name());

        this.dao.save(stuff1);

        long id1 = stuff1.getId();

        Assert.assertNotEquals(0, id1);

        GenericItem stuff2 = new GenericItem("that");
        stuff2.setActive(true);
        stuff2.setBroker(Broker.IB.name());

        this.dao.save(stuff2);

        long id2 = stuff2.getId();

        Assert.assertNotEquals(0, id2);

        GenericItem stuff3 = new GenericItem("this and that");
        stuff3.setActive(true);
        stuff3.setBroker(Broker.RT.name());

        this.dao.save(stuff3);

        long id3 = stuff3.getId();

        Assert.assertNotEquals(0, id3);

        this.dao.flush();

        Query query = this.dao.prepareQuery(null, "select s from GenericItem s where s.id in (:ids)", QueryType.HQL);
        query.setParameterList("ids", Arrays.asList(stuff1.getId(), stuff2.getId(), stuff3.getId()), LongType.INSTANCE);

        List<?> list1 = query.list();

        Assert.assertNotNull(list1);

        Assert.assertEquals(3, list1.size());
    }

    @Test
    public void testDeleteById() {

        GenericItem stuff1 = new GenericItem("this");
        stuff1.setActive(true);
        stuff1.setBroker(Broker.DC.name());

        this.dao.save(stuff1);
        this.dao.flush();
        this.session.clear();

        long id = stuff1.getId();

        GenericItem stuff2 = this.dao.load(id);

        Assert.assertNotNull(stuff2);

        Assert.assertTrue(this.dao.deleteById(id));

        this.dao.flush();
        this.session.clear();

        GenericItem stuff3 = this.dao.load(id);

        Assert.assertNull(stuff3);

        Assert.assertFalse(this.dao.deleteById(id));
    }

}
