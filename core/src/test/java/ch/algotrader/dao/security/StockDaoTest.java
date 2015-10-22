/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.dao.security;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.dao.security.StockDao;
import ch.algotrader.dao.security.StockDaoImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.security.Stock;
import ch.algotrader.entity.security.StockImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.hibernate.InMemoryDBTest;

/**
 * Unit tests for {@link StockDaoImpl}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class StockDaoTest extends InMemoryDBTest {

    private StockDao dao;

    private SecurityFamily family1;

    private SecurityFamily family2;

    public StockDaoTest() throws IOException {

        super();
    }

    @Override
    @Before
    public void setup() throws Exception {

        super.setup();

        this.dao = new StockDaoImpl(this.sessionFactory);

        this.family1 = new SecurityFamilyImpl();
        this.family1.setName("Family1");
        this.family1.setTickSizePattern("0<0.1");
        this.family1.setCurrency(Currency.USD);

        this.family2 = new SecurityFamilyImpl();
        this.family2.setName("Family2");
        this.family2.setTickSizePattern("0<0.1");
        this.family2.setCurrency(Currency.GBP);
    }

    @Test
    public void testFindBySectory() {

        Stock stock1 = new StockImpl();
        stock1.setSecurityFamily(this.family1);
        stock1.setGics("12345678");

        Stock stock2 = new StockImpl();
        stock2.setSecurityFamily(this.family2);
        stock2.setGics("1234");

        this.session.save(this.family1);
        this.session.save(stock1);
        this.session.save(this.family2);
        this.session.save(stock2);
        this.session.flush();

        List<Stock> stocks1 = this.dao.findBySectory("123");

        Assert.assertEquals(0, stocks1.size());

        List<Stock> stocks2 = this.dao.findBySectory("12");

        Assert.assertEquals(1, stocks2.size());

        Assert.assertSame(stock1, stocks2.get(0));

        stock2.setGics("12345678");

        this.session.flush();

        List<Stock> stocks3 = this.dao.findBySectory("12");

        Assert.assertEquals(2, stocks3.size());

        Assert.assertSame(stock1, stocks3.get(0));
        Assert.assertSame(stock2, stocks3.get(1));
    }

    @Test
    public void testFindByIndustryGroup() {

        Stock stock1 = new StockImpl();
        stock1.setSecurityFamily(this.family1);
        stock1.setGics("12345678");

        Stock stock2 = new StockImpl();
        stock2.setSecurityFamily(this.family2);
        stock2.setGics("1234");

        this.session.save(this.family1);
        this.session.save(stock1);
        this.session.save(this.family2);
        this.session.save(stock2);
        this.session.flush();

        List<Stock> stocks1 = this.dao.findByIndustryGroup("12");

        Assert.assertEquals(0, stocks1.size());

        List<Stock> stocks2 = this.dao.findByIndustryGroup("1234");

        Assert.assertEquals(1, stocks2.size());

        Assert.assertSame(stock1, stocks2.get(0));

        stock2.setGics("12345678");

        this.session.flush();

        List<Stock> stocks3 = this.dao.findByIndustryGroup("1234");

        Assert.assertEquals(2, stocks3.size());

        Assert.assertSame(stock1, stocks3.get(0));
        Assert.assertSame(stock2, stocks3.get(1));
    }

    @Test
    public void testFindByIndustry() {

        Stock stock1 = new StockImpl();
        stock1.setSecurityFamily(this.family1);
        stock1.setGics("12345678");

        Stock stock2 = new StockImpl();
        stock2.setSecurityFamily(this.family2);
        stock2.setGics("1234");

        this.session.save(this.family1);
        this.session.save(stock1);
        this.session.save(this.family2);
        this.session.save(stock2);
        this.session.flush();

        List<Stock> stocks1 = this.dao.findByIndustry("11");

        Assert.assertEquals(0, stocks1.size());

        List<Stock> stocks2 = this.dao.findByIndustry("123456");

        Assert.assertEquals(1, stocks2.size());

        Assert.assertSame(stock1, stocks2.get(0));

        stock2.setGics("12345678");

        this.session.flush();

        List<Stock> stocks3 = this.dao.findByIndustry("123456");

        Assert.assertEquals(2, stocks3.size());

        Assert.assertSame(stock1, stocks3.get(0));
        Assert.assertSame(stock2, stocks3.get(1));
    }

    @Test
    public void testFindBySubIndustry() {

        Stock stock1 = new StockImpl();
        stock1.setSecurityFamily(this.family1);
        stock1.setGics("12345678");

        Stock stock2 = new StockImpl();
        stock2.setSecurityFamily(this.family2);
        stock2.setGics("1234");

        this.session.save(this.family1);
        this.session.save(stock1);
        this.session.save(this.family2);
        this.session.save(stock2);
        this.session.flush();

        List<Stock> stocks1 = this.dao.findBySubIndustry("11");

        Assert.assertEquals(0, stocks1.size());

        List<Stock> stocks2 = this.dao.findBySubIndustry("12345678");

        Assert.assertEquals(1, stocks2.size());
        Assert.assertSame(stock1, stocks2.get(0));

        stock2.setGics("12345678");

        this.session.flush();

        List<Stock> stocks3 = this.dao.findBySubIndustry("12345678");

        Assert.assertEquals(2, stocks3.size());

        Assert.assertSame(stock1, stocks3.get(0));
        Assert.assertSame(stock2, stocks3.get(1));
    }

    @Test
    public void testFindStocksBySecurityFamily() {

        Stock stock1 = new StockImpl();
        stock1.setSecurityFamily(this.family1);
        stock1.setGics("12345678");

        Stock stock2 = new StockImpl();
        stock2.setSecurityFamily(this.family2);
        stock2.setGics("1234");

        this.session.save(this.family1);
        this.session.save(stock1);
        this.session.save(this.family2);
        this.session.save(stock2);
        this.session.flush();

        List<Stock> stocks1 = this.dao.findStocksBySecurityFamily(0);

        Assert.assertEquals(0, stocks1.size());

        List<Stock> stocks2 = this.dao.findStocksBySecurityFamily(this.family1.getId());

        Assert.assertEquals(1, stocks2.size());

        Assert.assertSame(stock1, stocks2.get(0));

        stock2.setSecurityFamily(this.family1);

        this.session.flush();

        List<Stock> stocks3 = this.dao.findStocksBySecurityFamily(this.family1.getId());

        Assert.assertEquals(2, stocks3.size());

        Assert.assertSame(stock1, stocks3.get(0));
        Assert.assertSame(stock2, stocks3.get(1));
    }

}
