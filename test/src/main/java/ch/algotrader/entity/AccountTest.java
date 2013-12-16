package ch.algotrader.entity;

import org.junit.Before;
import org.junit.Test;

import ch.algotrader.ServiceLocator;
import ch.algotrader.dbunit.AbstractDaoDbUnitTemplateTestCase;
import ch.algotrader.enumeration.OrderServiceType;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class AccountTest extends AbstractDaoDbUnitTemplateTestCase {

    private AccountDao accountDao;

    @Before
    public void before() {

        this.accountDao = ServiceLocator.instance().getService("accountDao", AccountDao.class);
    }

    @Test
    public void testFindByName() {

        this.accountDao.findByName("");
    }

    @Test
    public void testFindActiveSessionsByOrderServiceType() {

        this.accountDao.findActiveSessionsByOrderServiceType(OrderServiceType.IB_NATIVE);
    }
}
