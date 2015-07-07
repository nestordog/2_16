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
package ch.algotrader.entity.security;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.dao.security.EasyToBorrowDao;
import ch.algotrader.dao.security.EasyToBorrowDaoImpl;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.hibernate.InMemoryDBTest;

/**
 * Unit tests for {@link EasyToBorrowDaoImpl}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class EasyToBorrowDaoTest extends InMemoryDBTest {

    private EasyToBorrowDao dao;

    public EasyToBorrowDaoTest() throws IOException {

        super();
    }

    @Override
    @Before
    public void setup() throws Exception {

        super.setup();

        this.dao = new EasyToBorrowDaoImpl(this.sessionFactory);
    }

    @Test
    public void testFindByDateAndBroker() {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setName("Forex1");
        family.setTickSizePattern("0<0.1");
        family.setCurrency(Currency.USD);

        Stock stock1 = new StockImpl();
        stock1.setSecurityFamily(family);

        Stock stock2 = new StockImpl();
        stock2.setSecurityFamily(family);

        EasyToBorrow easyToBorrow1 = new EasyToBorrowImpl();
        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        easyToBorrow1.setDate(cal1.getTime());
        easyToBorrow1.setBroker(Broker.CNX);
        easyToBorrow1.setStock(stock1);

        EasyToBorrow easyToBorrow2 = new EasyToBorrowImpl();
        easyToBorrow2.setDate(cal1.getTime());
        easyToBorrow2.setBroker(Broker.CNX);
        easyToBorrow2.setStock(stock2);

        this.session.save(family);
        this.session.save(stock1);
        this.session.save(stock2);
        this.session.save(easyToBorrow1);
        this.session.save(easyToBorrow2);

        this.session.flush();

        List<EasyToBorrow> easyToBorrows1 = this.dao.findByDateAndBroker(cal1.getTime(), Broker.DC);

        Assert.assertEquals(0, easyToBorrows1.size());

        Calendar cal2 = Calendar.getInstance();
        cal2.set(Calendar.HOUR, 2);

        List<EasyToBorrow> easyToBorrows2 = this.dao.findByDateAndBroker(cal2.getTime(), Broker.CNX);

        Assert.assertEquals(0, easyToBorrows2.size());

        List<EasyToBorrow> easyToBorrows3 = this.dao.findByDateAndBroker(cal1.getTime(), Broker.CNX);

        Assert.assertEquals(2, easyToBorrows3.size());

        Assert.assertSame(easyToBorrow1, easyToBorrows3.get(0));
        Assert.assertSame(Broker.CNX, easyToBorrows3.get(0).getBroker());
        Assert.assertSame(stock1, easyToBorrows3.get(0).getStock());
        Assert.assertSame(easyToBorrow2, easyToBorrows3.get(1));
        Assert.assertSame(Broker.CNX, easyToBorrows3.get(1).getBroker());
        Assert.assertSame(stock2, easyToBorrows3.get(1).getStock());
    }

}
