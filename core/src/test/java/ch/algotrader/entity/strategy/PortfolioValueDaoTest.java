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
package ch.algotrader.entity.strategy;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.dao.strategy.PortfolioValueDao;
import ch.algotrader.dao.strategy.PortfolioValueDaoImpl;
import ch.algotrader.dao.strategy.PortfolioValueVOProducer;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.trade.OrderPreference;
import ch.algotrader.entity.trade.OrderPreferenceImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.OrderType;
import ch.algotrader.hibernate.InMemoryDBTest;
import ch.algotrader.vo.PortfolioValueVO;

/**
* Unit tests for {@link PortfolioValueDaoImpl}.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*
* @version $Revision$ $Date$
*/
public class PortfolioValueDaoTest extends InMemoryDBTest {

    private PortfolioValueDao dao;

    private OrderPreference orderPreference;

    private SecurityFamily family;

    private Strategy strategy;

    public PortfolioValueDaoTest() throws IOException {

        super();
    }

    @Override
    @Before
    public void setup() throws Exception {

        super.setup();

        this.dao = new PortfolioValueDaoImpl(this.sessionFactory);

        this.orderPreference = new OrderPreferenceImpl();
        this.orderPreference.setName("Primary");
        this.orderPreference.setOrderType(OrderType.DISTRIBUTIONAL);

        this.family = new SecurityFamilyImpl();
        this.family.setName("Forex1");
        this.family.setTickSizePattern("0<0.1");
        this.family.setCurrency(Currency.USD);

        this.strategy = new StrategyImpl();
        this.strategy.setName("Strategy1");
    }

    @Test
    public void testFindByStrategyAndMinDate() {

        Calendar cal1 = Calendar.getInstance();

        PortfolioValue portfolioValue1 = new PortfolioValueImpl();
        portfolioValue1.setDateTime(cal1.getTime());
        portfolioValue1.setNetLiqValue(new BigDecimal("11.1"));
        portfolioValue1.setSecuritiesCurrentValue(new BigDecimal("12.2"));
        portfolioValue1.setCashBalance(new BigDecimal("13.3"));
        portfolioValue1.setLeverage(15.5);
        portfolioValue1.setAllocation(16.6);
        portfolioValue1.setCashFlow(new BigDecimal("17.7"));
        portfolioValue1.setStrategy(this.strategy);

        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.HOUR_OF_DAY, 1);

        PortfolioValue portfolioValue2 = new PortfolioValueImpl();
        portfolioValue2.setDateTime(cal2.getTime());
        portfolioValue2.setNetLiqValue(new BigDecimal("21.1"));
        portfolioValue2.setSecuritiesCurrentValue(new BigDecimal("22.2"));
        portfolioValue2.setCashBalance(new BigDecimal("23.3"));
        portfolioValue2.setLeverage(25.5);
        portfolioValue2.setAllocation(26.6);
        portfolioValue2.setCashFlow(new BigDecimal("27.7"));
        portfolioValue2.setStrategy(this.strategy);

        this.session.save(this.strategy);
        this.session.save(portfolioValue1);
        this.session.save(portfolioValue2);
        this.session.flush();

        List<PortfolioValue> portfolioValues1 = this.dao.findByStrategyAndMinDate("Dummy", cal2.getTime());

        Assert.assertEquals(0, portfolioValues1.size());

        List<PortfolioValue> portfolioValues2 = this.dao.findByStrategyAndMinDate("Strategy1", cal2.getTime());

        Assert.assertEquals(1, portfolioValues2.size());

        Assert.assertSame(this.strategy, portfolioValues2.get(0).getStrategy());
        Assert.assertSame(portfolioValue2, portfolioValues2.get(0));

        List<PortfolioValue> portfolioValues3 = this.dao.findByStrategyAndMinDate("Strategy1", cal1.getTime());

        Assert.assertEquals(2, portfolioValues3.size());

        Assert.assertSame(this.strategy, portfolioValues3.get(0).getStrategy());
        Assert.assertSame(portfolioValue1, portfolioValues3.get(0));
        Assert.assertSame(this.strategy, portfolioValues3.get(1).getStrategy());
        Assert.assertSame(portfolioValue2, portfolioValues3.get(1));
    }

    @Test
    public void testFindByStrategyAndMinDateTRANSFORM_PORTFOLIOVALUEVO() {

        Calendar cal1 = Calendar.getInstance();

        PortfolioValue portfolioValue1 = new PortfolioValueImpl();
        portfolioValue1.setDateTime(cal1.getTime());
        portfolioValue1.setNetLiqValue(new BigDecimal("11.1"));
        portfolioValue1.setSecuritiesCurrentValue(new BigDecimal("12.2"));
        portfolioValue1.setCashBalance(new BigDecimal("13.3"));
        portfolioValue1.setLeverage(15.5);
        portfolioValue1.setAllocation(16.6);
        portfolioValue1.setCashFlow(new BigDecimal("17.7"));
        portfolioValue1.setStrategy(this.strategy);

        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.HOUR_OF_DAY, 1);

        PortfolioValue portfolioValue2 = new PortfolioValueImpl();
        portfolioValue2.setDateTime(cal2.getTime());
        portfolioValue2.setNetLiqValue(new BigDecimal("21.1"));
        portfolioValue2.setSecuritiesCurrentValue(new BigDecimal("22.2"));
        portfolioValue2.setCashBalance(new BigDecimal("23.3"));
        portfolioValue2.setLeverage(25.5);
        portfolioValue2.setAllocation(26.6);
        portfolioValue2.setCashFlow(new BigDecimal("27.7"));
        portfolioValue2.setStrategy(this.strategy);

        this.session.save(this.strategy);
        this.session.save(portfolioValue1);
        this.session.save(portfolioValue2);
        this.session.flush();

        List<PortfolioValueVO> portfolioValueVOs1 = this.dao.findByStrategyAndMinDate("Dummy", cal2.getTime(), PortfolioValueVOProducer.INSTANCE);

        Assert.assertEquals(0, portfolioValueVOs1.size());

        List<PortfolioValueVO> portfolioValueVOs2 = this.dao.findByStrategyAndMinDate("Strategy1", cal2.getTime(), PortfolioValueVOProducer.INSTANCE);

        Assert.assertEquals(1, portfolioValueVOs2.size());

        PortfolioValueVO portfolioValueVO1 = portfolioValueVOs2.get(0);

        Assert.assertNotNull(portfolioValueVO1);

        Assert.assertEquals(portfolioValue2.getDateTime(), portfolioValueVO1.getDateTime());
        Assert.assertEquals(portfolioValue2.getNetLiqValue(), portfolioValueVO1.getNetLiqValue());
        Assert.assertEquals(portfolioValue2.getSecuritiesCurrentValue(), portfolioValueVO1.getSecuritiesCurrentValue());
        Assert.assertEquals(portfolioValue2.getCashBalance(), portfolioValueVO1.getCashBalance());
        Assert.assertEquals(new Double(portfolioValue2.getLeverage()), new Double(portfolioValueVO1.getLeverage()));
        Assert.assertEquals(new Double(portfolioValue2.getAllocation()), new Double(portfolioValueVO1.getAllocation()));
        Assert.assertEquals(portfolioValue2.getCashFlow(), portfolioValueVO1.getCashFlow());

        List<PortfolioValueVO> portfolioValueVOs3 = this.dao.findByStrategyAndMinDate("Strategy1", cal1.getTime(), PortfolioValueVOProducer.INSTANCE);

        Assert.assertEquals(2, portfolioValueVOs3.size());

        PortfolioValueVO portfolioValueVO2 = portfolioValueVOs3.get(0);

        Assert.assertNotNull(portfolioValueVO2);

        PortfolioValueVO portfolioValueVO3 = portfolioValueVOs3.get(1);

        Assert.assertNotNull(portfolioValueVO3);

        Assert.assertEquals(portfolioValue1.getDateTime(), portfolioValueVO2.getDateTime());
        Assert.assertEquals(portfolioValue1.getNetLiqValue(), portfolioValueVO2.getNetLiqValue());
        Assert.assertEquals(portfolioValue1.getSecuritiesCurrentValue(), portfolioValueVO2.getSecuritiesCurrentValue());
        Assert.assertEquals(portfolioValue1.getCashBalance(), portfolioValueVO2.getCashBalance());
        Assert.assertEquals(new Double(portfolioValue1.getLeverage()), new Double(portfolioValueVO2.getLeverage()));
        Assert.assertEquals(new Double(portfolioValue1.getAllocation()), new Double(portfolioValueVO2.getAllocation()));
        Assert.assertEquals(portfolioValue1.getCashFlow(), portfolioValueVO2.getCashFlow());

        Assert.assertEquals(portfolioValue2.getDateTime(), portfolioValueVO3.getDateTime());
        Assert.assertEquals(portfolioValue2.getNetLiqValue(), portfolioValueVO3.getNetLiqValue());
        Assert.assertEquals(portfolioValue2.getSecuritiesCurrentValue(), portfolioValueVO3.getSecuritiesCurrentValue());
        Assert.assertEquals(portfolioValue2.getCashBalance(), portfolioValueVO3.getCashBalance());
        Assert.assertEquals(new Double(portfolioValue2.getLeverage()), new Double(portfolioValueVO3.getLeverage()));
        Assert.assertEquals(new Double(portfolioValue2.getAllocation()), new Double(portfolioValueVO3.getAllocation()));
        Assert.assertEquals(portfolioValue2.getCashFlow(), portfolioValueVO3.getCashFlow());
    }

}
