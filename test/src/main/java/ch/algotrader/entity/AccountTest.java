package ch.algotrader.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import ch.algotrader.ServiceLocator;
import ch.algotrader.dbunit.AbstractDaoDbUnitTemplateTestCase;
import ch.algotrader.dbunit.DataSets;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.util.collection.CollectionUtil;

public class AccountTest extends AbstractDaoDbUnitTemplateTestCase {

    private static final int ID = 1;
    private static final String ACCOUNT_NAME = "TEST_ACCOUNT";
    private static final boolean ACCOUNT_ACTIVE = true;
    private static final Broker ACCOUNT_BROKER = Broker.IB;
    private static final OrderServiceType ACCOUNT_ORDER_SERVICE_TYPE = OrderServiceType.IB_NATIVE;
    private static final String ACCOUNT_SESSION_QUALIFIER = "IB_TEST";

    private AccountDao accountDao;

    @Before
    public void before() {
        this.accountDao = ServiceLocator.instance().getService("accountDao", AccountDao.class);
    }

    @Test
    @DataSets(setUpDataSet = "/account.xml")
    public void testFindById() throws Exception {

        Account account = this.accountDao.get(ID);
        assertAccount(account);
    }

    @Test
    @DataSets(setUpDataSet = "/account.xml")
    public void testFindByUnknowId() throws Exception {

        Account account = this.accountDao.get(ID + 1);
        assertNull(account);
    }

    @Test
    @DataSets(setUpDataSet = "/account.xml")
    public void testFindByName() {

        Account account = this.accountDao.findByName(ACCOUNT_NAME);
        assertAccount(account);
    }

    @Test
    @DataSets(setUpDataSet = "/account.xml")
    public void testFindByUnknownName() {

        Account account = this.accountDao.findByName("XXX");
        assertNull(account);
    }

    @Test
    @DataSets(setUpDataSet = "/account.xml")
    public void testFindActiveSessionsByOrderServiceType() {

        Collection<String> activeSessions = this.accountDao.findActiveSessionsByOrderServiceType(OrderServiceType.IB_NATIVE);
        String session = CollectionUtil.getSingleElement(activeSessions);
        assertEquals(session, ACCOUNT_SESSION_QUALIFIER);
    }

    @Test
    @DataSets(setUpDataSet = "/account.xml")
    public void testFindActiveSessionsByOrderServiceType2() {

        Collection<String> activeSessions = this.accountDao.findActiveSessionsByOrderServiceType(OrderServiceType.IB_FIX);
        assertEquals(activeSessions.size(), 0);
    }

    @Test
    @DataSets(assertDataSet = "/account.xml", assertTable = "account")
    public void testAddAccount() throws Exception {

        beginTransaction();
        Account account = newAccount();
        this.accountDao.create(account);
        commitTransaction();

        long id = account.getId();
        assertTrue(id > 0);
    }

    @Test
    @DataSets(setUpDataSet = "/account.xml", assertDataSet = "/empty.xml")
    public void testDeleteAccount() throws Exception {

        beginTransaction();
        this.accountDao.remove(ID);
        commitTransaction();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullAccount() throws Exception {

        Account account = null;
        this.accountDao.create(account);
    }

    private Account newAccount() {

        Account account = new AccountImpl();
        account.setName(ACCOUNT_NAME);
        account.setActive(ACCOUNT_ACTIVE);
        account.setBroker(ACCOUNT_BROKER);
        account.setOrderServiceType(ACCOUNT_ORDER_SERVICE_TYPE);
        account.setSessionQualifier(ACCOUNT_SESSION_QUALIFIER);

        return account;
    }

    private void assertAccount(Account account) {

        assertNotNull(account);
        assertEquals(ACCOUNT_NAME, account.getName());
        assertEquals(ACCOUNT_ACTIVE, account.isActive());
        assertEquals(ACCOUNT_BROKER, account.getBroker());
        assertEquals(ACCOUNT_ORDER_SERVICE_TYPE, account.getOrderServiceType());
        assertEquals(ACCOUNT_SESSION_QUALIFIER, account.getSessionQualifier());
    }
}
