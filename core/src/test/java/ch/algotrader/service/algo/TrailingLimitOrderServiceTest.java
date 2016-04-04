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
package ch.algotrader.service.algo;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.entity.marketData.TickVOBuilder;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.Stock;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.OrderValidationException;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.entity.trade.algo.TrailingLimitOrder;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Side;
import ch.algotrader.service.MarketDataCacheService;
import ch.algotrader.service.OrderExecutionService;
import ch.algotrader.service.SimpleOrderService;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class TrailingLimitOrderServiceTest {

    @Mock private static OrderExecutionService orderExecutionService;

    @Mock
    private static MarketDataCacheService marketDataCacheService;

    @Mock
    private static SimpleOrderService simpleOrderService;

    @Mock private static TrailingLimitOrderService trailingLimitOrderService;

    private static Security security;
    private static Strategy strategy;
    private static String intId = "XYZ";

    @Before
    public void setup() {

        trailingLimitOrderService = new TrailingLimitOrderService(orderExecutionService, simpleOrderService, marketDataCacheService);

        SecurityFamily securityFamily = SecurityFamily.Factory.newInstance("", Currency.USD, 1, 2, "0<0.01", true, false);
        security = Stock.Factory.newInstance(securityFamily);
        security.setSymbol("TEST_SECURITY");
        security.setId(1);

        strategy = Strategy.Factory.newInstance("TEST_STRATEGY", false);
    }

    @Test
    public void testSendBuyOrder() throws ReflectiveOperationException, OrderValidationException {

        TrailingLimitOrder algoOrder = sendOrder(Side.BUY, new BigDecimal("12.80"));

        ArgumentCaptor<SimpleOrder> captor = ArgumentCaptor.forClass(SimpleOrder.class);
        Mockito.verify(simpleOrderService, Mockito.atLeast(1)).sendOrder(captor.capture());
        LimitOrder limitOrder = (LimitOrder) captor.getValue();
        Assert.assertNotNull(limitOrder);

        Optional<TrailingLimitOrderStateVO> optional = trailingLimitOrderService.getAlgoOrderState(algoOrder);
        Assert.assertTrue(optional.isPresent());

        TrailingLimitOrderStateVO algoOrderState = optional.get();
        Assert.assertNotNull(algoOrderState.getLimitOrder());
        Assert.assertEquals(algoOrderState.getLimitOrder(), limitOrder);

        Assert.assertEquals(new BigDecimal("12.30"), limitOrder.getLimit());

        trailingLimitOrderService.adjustLimit(algoOrder, new BigDecimal("12.85"));
        Assert.assertSame(limitOrder, algoOrderState.getLimitOrder());

        trailingLimitOrderService.adjustLimit(algoOrder, new BigDecimal("12.90"));
        Assert.assertNotSame(limitOrder, algoOrderState.getLimitOrder());
        Assert.assertEquals(new BigDecimal("12.40"), algoOrderState.getLimitOrder().getLimit());

        trailingLimitOrderService.adjustLimit(algoOrder, new BigDecimal("12.20"));
        Assert.assertNotSame(limitOrder, algoOrderState.getLimitOrder());
        Assert.assertEquals(new BigDecimal("12.40"), algoOrderState.getLimitOrder().getLimit());
    }

    @Test
    public void testSendSellOrder() throws ReflectiveOperationException, OrderValidationException {

        TrailingLimitOrder algoOrder = sendOrder(Side.SELL, new BigDecimal("12.80"));

        ArgumentCaptor<SimpleOrder> captor = ArgumentCaptor.forClass(SimpleOrder.class);
        Mockito.verify(simpleOrderService, Mockito.atLeast(1)).sendOrder(captor.capture());
        LimitOrder limitOrder = (LimitOrder) captor.getValue();
        Assert.assertNotNull(limitOrder);

        Optional<TrailingLimitOrderStateVO> optional = trailingLimitOrderService.getAlgoOrderState(algoOrder);
        Assert.assertTrue(optional.isPresent());

        TrailingLimitOrderStateVO algoOrderState = optional.get();
        Assert.assertNotNull(algoOrderState.getLimitOrder());
        Assert.assertEquals(algoOrderState.getLimitOrder(), limitOrder);

        Assert.assertEquals(new BigDecimal("13.30"), limitOrder.getLimit());

        trailingLimitOrderService.adjustLimit(algoOrder, new BigDecimal("12.75"));
        Assert.assertSame(limitOrder, algoOrderState.getLimitOrder());

        trailingLimitOrderService.adjustLimit(algoOrder, new BigDecimal("12.70"));
        Assert.assertNotSame(limitOrder, algoOrderState.getLimitOrder());
        Assert.assertEquals(new BigDecimal("13.20"), algoOrderState.getLimitOrder().getLimit());

        trailingLimitOrderService.adjustLimit(algoOrder, new BigDecimal("13.40"));
        Assert.assertNotSame(limitOrder, algoOrderState.getLimitOrder());
        Assert.assertEquals(new BigDecimal("13.20"), algoOrderState.getLimitOrder().getLimit());
    }

    private TrailingLimitOrder sendOrder(Side side, BigDecimal last) throws OrderValidationException {

        TickVO tick = TickVOBuilder.create().setSecurityId(security.getId()).setLast(last).build();
        Mockito.when(marketDataCacheService.getCurrentMarketDataEvent(security.getId())).thenReturn(tick);

        TrailingLimitOrder algoOrder = new TrailingLimitOrder();
        algoOrder.setIntId(intId);
        algoOrder.setSecurity(security);
        algoOrder.setStrategy(strategy);
        algoOrder.setSide(side);
        algoOrder.setQuantity(200);
        algoOrder.setTrailingAmount(new BigDecimal("0.5"));
        algoOrder.setIncrement(new BigDecimal("0.1"));

        trailingLimitOrderService.validateOrder(algoOrder);
        trailingLimitOrderService.sendOrder(algoOrder);

        return algoOrder;
    }

}
