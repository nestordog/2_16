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

import ch.algotrader.vo.PortfolioValueVO;

/**
* Unit tests for {@link ch.algotrader.entity.strategy.PortfolioValueVOProducer}.
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

        PortfolioValue entity = new PortfolioValueImpl();

        entity.setDateTime(new Date());
        entity.setNetLiqValue(new BigDecimal("1.1"));
        entity.setSecuritiesCurrentValue(new BigDecimal("2.2"));
        entity.setCashBalance(new BigDecimal("3.3"));
        entity.setMaintenanceMargin(new BigDecimal("4.4"));
        entity.setLeverage(5.5);
        entity.setAllocation(6.6);
        entity.setCashFlow(new BigDecimal("7.7"));

        PortfolioValueVO vo = this.instance.convert(entity);

        Assert.assertNotNull(vo);

        Assert.assertEquals(entity.getDateTime(), vo.getDateTime());
        Assert.assertEquals(entity.getNetLiqValue(), vo.getNetLiqValue());
        Assert.assertEquals(entity.getSecuritiesCurrentValue(), vo.getSecuritiesCurrentValue());
        Assert.assertEquals(entity.getCashBalance(), vo.getCashBalance());
        Assert.assertEquals(entity.getMaintenanceMargin(), vo.getMaintenanceMargin());
        Assert.assertEquals(new Double(entity.getLeverage()), new Double(vo.getLeverage()));
        Assert.assertEquals(new Double(entity.getAllocation()), new Double(vo.getAllocation()));
        Assert.assertEquals(entity.getCashFlow(), vo.getCashFlow());
    }

}
