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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.entity.Position;
import ch.algotrader.entity.PositionImpl;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.util.RoundUtil;
import ch.algotrader.vo.client.PositionVO;

/**
* Unit tests for {@link PositionVOProducer}.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*
* @version $Revision$ $Date$
*/
public class PositionVOProducerTest {

    private PositionVOProducer instance;

    private Tick tick;

    @Before
    public void setup() throws Exception {

        this.instance = PositionVOProducer.INSTANCE;

        this.tick = Tick.Factory.newInstance();
    }

    @Test
    public void testConvert() {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setName("Forex1");
        family.setTickSizePattern("0<0.1");
        family.setCurrency(Currency.USD);
        family.setScale(1);

        Forex forex = new ForexImpl();
        forex.setSymbol("EUR.USD");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);
        forex.setId(101);

        Strategy strategy = new StrategyImpl();
        strategy.setName("Strategy");

        Position position = new PositionImpl();
        position.setId(111);
        position.setQuantity(0);
        position.setCost(333.33);
        position.setRealizedPL(444.44);
        position.setSecurity(forex);
        position.setStrategy(strategy);
        position.setRealizedPL(102);

        int scale = position.getSecurity().getSecurityFamily().getScale();

        PositionVO vo = this.instance.convert(position);

        Assert.assertNotNull(vo);

        Assert.assertEquals(111, vo.getId());
        Assert.assertEquals(0, vo.getQuantity());

        Assert.assertEquals(position.getSecurity().getId(), vo.getSecurityId());
        Assert.assertEquals(position.getSecurity().toString(), vo.getName());
        Assert.assertEquals(position.getStrategy().toString(), vo.getStrategy());
        Assert.assertEquals(position.getSecurity().getSecurityFamily().getCurrency(), vo.getCurrency());
        Assert.assertEquals(RoundUtil.getBigDecimal(position.getMarketPrice(this.tick), scale), vo.getMarketPrice());
        Assert.assertEquals(RoundUtil.getBigDecimal(position.getMarketValue(this.tick)), vo.getMarketValue());
        Assert.assertEquals(RoundUtil.getBigDecimal(position.getAveragePrice(), scale), vo.getAveragePrice());
        Assert.assertEquals(RoundUtil.getBigDecimal(position.getCost()), vo.getCost());
        Assert.assertEquals(RoundUtil.getBigDecimal(position.getUnrealizedPL(this.tick)), vo.getUnrealizedPL());
        Assert.assertEquals(RoundUtil.getBigDecimal(position.getRealizedPL()), vo.getRealizedPL());
    }

}
