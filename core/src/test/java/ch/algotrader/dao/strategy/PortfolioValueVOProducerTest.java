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
package ch.algotrader.dao.strategy;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.entity.strategy.PortfolioValue;
import ch.algotrader.entity.strategy.PortfolioValueImpl;
import ch.algotrader.entity.strategy.StrategyImpl;
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
        portfolioValue.setMarketValue(new BigDecimal("2.2"));
        portfolioValue.setRealizedPL(new BigDecimal("3.3"));
        portfolioValue.setUnrealizedPL(new BigDecimal("4.4"));
        portfolioValue.setCashBalance(new BigDecimal("5.5"));
        portfolioValue.setOpenPositions(6);
        portfolioValue.setLeverage(7.7);
        portfolioValue.setCashFlow(new BigDecimal("8.8"));
        portfolioValue.setStrategy(new StrategyImpl());

        PortfolioValueVO portfolioValueVO = this.instance.convert(portfolioValue);

        Assert.assertNotNull(portfolioValueVO);

        Assert.assertEquals(portfolioValue.getDateTime(), portfolioValueVO.getDateTime());
        Assert.assertEquals(portfolioValue.getNetLiqValue(), portfolioValueVO.getNetLiqValue());
        Assert.assertEquals(portfolioValue.getMarketValue(), portfolioValueVO.getMarketValue());
        Assert.assertEquals(portfolioValue.getRealizedPL(), portfolioValueVO.getRealizedPL());
        Assert.assertEquals(portfolioValue.getUnrealizedPL(), portfolioValueVO.getUnrealizedPL());
        Assert.assertEquals(portfolioValue.getCashBalance(), portfolioValueVO.getCashBalance());
        Assert.assertEquals(portfolioValue.getOpenPositions(), portfolioValueVO.getOpenPositions());
        Assert.assertEquals(new Double(portfolioValue.getLeverage()), new Double(portfolioValueVO.getLeverage()));
        Assert.assertEquals(portfolioValue.getCashFlow(), portfolioValueVO.getCashFlow());
    }

}
