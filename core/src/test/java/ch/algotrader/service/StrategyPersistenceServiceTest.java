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
package ch.algotrader.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
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

import ch.algotrader.dao.strategy.StrategyDao;
import ch.algotrader.entity.strategy.Strategy;

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

        //when
        final Strategy result = service.getOrCreateStrategy(name);

        //then
        verify(strategyDao).findByName(eq(name));
        verify(strategyDao, never()).save(any(Strategy.class));
        assertSame("existing strategy should be returned", strategy, result);
    }

    @Test
    public void shouldGetOrCreateStrategyWhenNotExists() {
        //given
        final String name = "MyHolyStrategy";
        final ArgumentCaptor<Strategy> strategyCaptor = ArgumentCaptor.forClass(Strategy.class);

        //when
        final Strategy result = service.getOrCreateStrategy(name);

        //then
        verify(strategyDao).findByName(eq(name));
        verify(strategyDao).save(strategyCaptor.capture());
        assertSame("strategy name should have been set", name, strategyCaptor.getValue().getName());
        assertEquals("strategy auto-activate flag should be true", true, strategyCaptor.getValue().isAutoActivate());
        assertNotSame("new strategy should have been returned", strategy, strategyCaptor.getValue());
        assertSame("new strategy should have been returned", strategyCaptor.getValue(), result);
    }

    //------- deleteStragegy
    @Test
    public void shouldDeleteStrategy() {
        //given
        final long strategyId = 666;

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

}
