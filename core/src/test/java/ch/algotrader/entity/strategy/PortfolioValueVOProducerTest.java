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

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.dao.strategy.PortfolioValueVOProducer;
import ch.algotrader.vo.PortfolioValueVO;

/**
* Unit tests for {@link PortfolioValueVOProducer}.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*
* @version $Revision$ $Date$
*/
public class PortfolioValueVOProducerTest {

    private PortfolioValueVOProducer instance;

    @Before
    public void setup() throws Exception {

        this.instance = PortfolioValueVOProducer.INSTANCE;
    }

    @Test
    public void testConvert() {

        PortfolioValue portfolioValue = new PortfolioValueImpl();

        portfolioValue.setDateTime(new Date());
        portfolioValue.setNetLiqValue(new BigDecimal("1.1"));
        portfolioValue.setSecuritiesCurrentValue(new BigDecimal("2.2"));
        portfolioValue.setCashBalance(new BigDecimal("3.3"));
        portfolioValue.setLeverage(5.5);
        portfolioValue.setAllocation(6.6);
        portfolioValue.setCashFlow(new BigDecimal("7.7"));

        PortfolioValueVO portfolioValueVO = this.instance.convert(portfolioValue);

        Assert.assertNotNull(portfolioValueVO);

        Assert.assertEquals(portfolioValue.getDateTime(), portfolioValueVO.getDateTime());
        Assert.assertEquals(portfolioValue.getNetLiqValue(), portfolioValueVO.getNetLiqValue());
        Assert.assertEquals(portfolioValue.getSecuritiesCurrentValue(), portfolioValueVO.getSecuritiesCurrentValue());
        Assert.assertEquals(portfolioValue.getCashBalance(), portfolioValueVO.getCashBalance());
        Assert.assertEquals(new Double(portfolioValue.getLeverage()), new Double(portfolioValueVO.getLeverage()));
        Assert.assertEquals(new Double(portfolioValue.getAllocation()), new Double(portfolioValueVO.getAllocation()));
        Assert.assertEquals(portfolioValue.getCashFlow(), portfolioValueVO.getCashFlow());
    }

}
