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

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.util.RoundUtil;
import ch.algotrader.vo.PositionVO;

/**
* Unit tests for {@link ch.algotrader.entity.PositionVOProducer}.
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

        Position entity = new PositionImpl();
        entity.setId(111);
        entity.setQuantity(0);
        entity.setCost(333.33);
        entity.setRealizedPL(444.44);
        entity.setExitValue(new BigDecimal(103.55));
        entity.setSecurity(forex);
        entity.setStrategy(strategy);
        entity.setRealizedPL(102);
        entity.setMaintenanceMargin(new BigDecimal(104.666));

        int scale = entity.getSecurity().getSecurityFamily().getScale();

        PositionVO vo = this.instance.convert(entity);

        Assert.assertNotNull(vo);

        Assert.assertEquals(111, vo.getId());
        Assert.assertEquals(0, vo.getQuantity());

        Assert.assertEquals(entity.getSecurity().getId(), vo.getSecurityId());
        Assert.assertEquals(entity.getSecurity().toString(), vo.getName());
        Assert.assertEquals(entity.getStrategy().toString(), vo.getStrategy());
        Assert.assertEquals(entity.getSecurity().getSecurityFamily().getCurrency(), vo.getCurrency());
        Assert.assertEquals(RoundUtil.getBigDecimal(entity.getMarketPrice(this.tick), scale), vo.getMarketPrice());
        Assert.assertEquals(RoundUtil.getBigDecimal(entity.getMarketValue(this.tick)), vo.getMarketValue());
        Assert.assertEquals(RoundUtil.getBigDecimal(entity.getAveragePrice(), scale), vo.getAveragePrice());
        Assert.assertEquals(RoundUtil.getBigDecimal(entity.getCost()), vo.getCost());
        Assert.assertEquals(RoundUtil.getBigDecimal(entity.getUnrealizedPL(this.tick)), vo.getUnrealizedPL());
        Assert.assertEquals(RoundUtil.getBigDecimal(entity.getRealizedPL()), vo.getRealizedPL());
        Assert.assertEquals(RoundUtil.getBigDecimal(entity.getMaxLoss(this.tick)), vo.getMaxLoss());
        Assert.assertEquals(entity.getExitValue().setScale(scale, BigDecimal.ROUND_HALF_UP), vo.getExitValue());
        Assert.assertEquals(entity.getMaintenanceMargin().setScale(scale, BigDecimal.ROUND_HALF_UP), vo.getMargin());
    }

}
