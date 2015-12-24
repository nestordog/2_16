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
package ch.algotrader.vo.client;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ch.algotrader.entity.Position;
import ch.algotrader.entity.PositionImpl;
import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.service.MarketDataCache;

/**
* Unit tests for {@link PositionVOProducer}.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*
* @version $Revision$ $Date$
*/
public class PositionVOProducerTest {

    private MarketDataCache marketDataCache;

    private PositionVOProducer instance;

    @Before
    public void setup() throws Exception {

        this.marketDataCache = Mockito.mock(MarketDataCache.class);
        this.instance = new PositionVOProducer(this.marketDataCache);
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
        position.setCost(new BigDecimal(333.33));
        position.setRealizedPL(new BigDecimal(102));
        position.setSecurity(forex);
        position.setStrategy(strategy);

        int scale = position.getSecurity().getSecurityFamily().getScale();

        TickVO tick = new TickVO(0L, new Date(), FeedType.SIM.name(), 101, new BigDecimal("1.1"), new Date(), new BigDecimal("1.12"), new BigDecimal("1.09"), 1, 2, 3);
        Mockito.when(this.marketDataCache.getCurrentMarketDataEvent(101L)).thenReturn(tick);

        PositionVO vo = this.instance.convert(position);

        Assert.assertNotNull(vo);

        Assert.assertEquals(111, vo.getId());
        Assert.assertEquals(0, vo.getQuantity());

        Assert.assertEquals(position.getSecurity().getId(), vo.getSecurityId());
        Assert.assertEquals(position.getSecurity().toString(), vo.getName());
        Assert.assertEquals(position.getStrategy().toString(), vo.getStrategy());
        Assert.assertEquals(position.getSecurity().getSecurityFamily().getCurrency(), vo.getCurrency());
        Assert.assertEquals(position.getMarketPrice(tick), vo.getMarketPrice());
        Assert.assertEquals(position.getMarketValue(tick), vo.getMarketValue());
        Assert.assertEquals(position.getAveragePrice(), vo.getAveragePrice());
        Assert.assertEquals(position.getCost(), vo.getCost());
        Assert.assertEquals(position.getUnrealizedPL(tick), vo.getUnrealizedPL());
        Assert.assertEquals(position.getRealizedPL(), vo.getRealizedPL());
    }

}
