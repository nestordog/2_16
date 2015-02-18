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
package ch.algotrader.entity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.vo.OpenPositionVO;

/**
* Unit tests for {@link ch.algotrader.entity.ClosePositionVOProducer}.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*
* @version $Revision$ $Date$
*/
public class OpenPositionVOProducerTest {

    private OpenPositionVOProducer instance;

    @Before
    public void setup() throws Exception {

        this.instance = OpenPositionVOProducer.INSTANCE;
    }

    @Test
    public void testConvert() {

        Forex forex = new ForexImpl();
        forex.setId(666);

        Strategy strategy = new StrategyImpl();
        strategy.setName("Strategy");

        Position position = new PositionImpl();

        position.setId(111);
        position.setQuantity(222);
        position.setSecurity(forex);
        position.setStrategy(strategy);

        OpenPositionVO openPositionVO = this.instance.convert(position);

        Assert.assertNotNull(openPositionVO);

        Assert.assertEquals(111, openPositionVO.getId());
        Assert.assertEquals(222, openPositionVO.getQuantity());
        Assert.assertEquals(666, openPositionVO.getSecurityId());
        Assert.assertEquals(position.getStrategy().toString(), openPositionVO.getStrategy());
    }

}
