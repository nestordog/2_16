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
package ch.algotrader.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyDao;

/**
 * Unit test for {@link StrategyPersistenceServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class StrategyPersistenceServiceTest {

    @Mock
    private StrategyDao strategyDao;
    @Mock
    private Strategy strategy;

    //under test
    private StrategyPersistenceService service;

    @Before
    public void beforeEach() {
        service = new StrategyPersistenceServiceImpl(strategyDao);
    }

    //------- persistStrategy
    @Test
    public void shouldPersistStrategy() {
        //when
        service.persistStrategy(strategy);

        //then
        verify(strategyDao).save(same(strategy));
    }

    //------- getOrCreateStrategy
    @Test
    public void shouldGetOrCreateStrategyWhenExistsAlready() {
        //given
        final String name = "MyHolyStrategy";
        final double allocation = 0.123456789;
        when(strategyDao.findByName(eq(name))).thenReturn(strategy);
        when(strategy.getAllocation()).thenReturn(allocation);

        //when
        final Strategy result = service.getOrCreateStrategy(name, allocation);

        //then
        verify(strategyDao).findByName(eq(name));
        verify(strategy, never()).setAllocation(anyDouble());
        verify(strategyDao, never()).save(any(Strategy.class));
        assertSame("existing strategy should be returned", strategy, result);
    }

    @Test
    public void shouldGetOrCreateStrategyWhenExistsAlreadyAfterUpdatingAllocation() {
        //given
        final String name = "MyHolyStrategy";
        final double oldAllocation = 0.01234;
        final double newAllocation = 0.56789;
        when(strategyDao.findByName(eq(name))).thenReturn(strategy);
        when(strategy.getAllocation()).thenReturn(oldAllocation);
        final ArgumentCaptor<Strategy> strategyCaptor = ArgumentCaptor.forClass(Strategy.class);

        //when
        final Strategy result = service.getOrCreateStrategy(name, newAllocation);

        //then
        verify(strategyDao).findByName(eq(name));
        verify(strategy).setAllocation(newAllocation);
        verify(strategyDao).save(strategyCaptor.capture());
        assertSame("new strategy should have been returned", strategyCaptor.getValue(), result);
        assertSame("existing strategy should be returned", strategy, result);
    }

    @Test
    public void shouldGetOrCreateStrategyWhenNotExists() {
        //given
        final String name = "MyHolyStrategy";
        final double allocation = 0.123456789;
        final ArgumentCaptor<Strategy> strategyCaptor = ArgumentCaptor.forClass(Strategy.class);

        //when
        final Strategy result = service.getOrCreateStrategy(name, allocation);

        //then
        verify(strategyDao).findByName(eq(name));
        verify(strategyDao).save(strategyCaptor.capture());
        assertSame("strategy name should have been set", name, strategyCaptor.getValue().getName());
        assertEquals("strategy allocation should have been set", allocation, strategyCaptor.getValue().getAllocation(), 0.0);
        assertEquals("strategy auto-activate flag should be true", true, strategyCaptor.getValue().isAutoActivate());
        assertNotSame("new strategy should have been returned", strategy, strategyCaptor.getValue());
        assertSame("new strategy should have been returned", strategyCaptor.getValue(), result);
    }

    //------- deleteStragegy
    @Test
    public void shouldDeleteStrategy() {
        //given
        final int strategyId = 666;

        //when
        service.deleteStrategy(strategyId);

        //then
        verify(strategyDao).deleteById(eq(strategyId));
    }

    //------- exception cases
    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenConstructedWithNullDao() {
        new StrategyPersistenceServiceImpl(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void persisteStrategyWithNullShouldCauseException() {
        service.persistStrategy(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getOrCreateStrategyWithNullShouldCauseException() {
        service.getOrCreateStrategy(null, 0.5);
    }

}
