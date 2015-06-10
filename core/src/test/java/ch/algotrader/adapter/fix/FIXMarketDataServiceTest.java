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
package ch.algotrader.adapter.fix;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.marketData.TickDao;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityDao;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.service.ServiceException;
import ch.algotrader.service.fix.fix44.Fix44MarketDataService;
import ch.algotrader.vo.SubscribeTickVO;
import quickfix.fix44.MarketDataRequest;

@RunWith(MockitoJUnitRunner.class)
public class FIXMarketDataServiceTest {

    private static final String STRATEGY_NAME = "MyStrategy";

    @Mock
    private SecurityDao securityDao;
    @Mock
    private TickDao tickDao;
    @Mock
    private FixAdapter fixAdapter;
    @Mock
    private FixSessionStateHolder fixSessionStateHolder;
    @Mock
    private EngineManager engineManager;
    @Mock
    private Engine engine;
    @Mock
    private Engine serverEngine;

    private Fix44MarketDataService impl;

    @Before
    public void setup() {

        when(engine.getStrategyName()).thenReturn(STRATEGY_NAME);
        when(serverEngine.getStrategyName()).thenReturn(StrategyImpl.SERVER);
        when(engineManager.getServerEngine()).thenReturn(serverEngine);
        when(engineManager.getEngines()).thenReturn(Arrays.asList(serverEngine, engine));

        FakeFix44MarketDataService fakeFix44MarketDataService = new FakeFix44MarketDataService(this.fixSessionStateHolder, this.fixAdapter, this.engineManager, this.securityDao );

        this.impl = Mockito.spy(fakeFix44MarketDataService);
    }

    private static Forex createForex(final Currency base, final Currency counter) {
        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(counter);

        Forex forex = new ForexImpl();
        forex.setSymbol(base.name() + "." + counter.name());
        forex.setBaseCurrency(base);
        forex.setSecurityFamily(family);
        return forex;
    }

    @Test
    public void testInitialSubscriptions() throws Exception {


        Mockito.when(this.fixSessionStateHolder.isLoggedOn()).thenReturn(Boolean.TRUE);
        Mockito.when(this.fixSessionStateHolder.isSubscribed()).thenReturn(Boolean.FALSE);
        Mockito.when(this.fixSessionStateHolder.onSubscribe()).thenReturn(Boolean.TRUE);

        Forex forex = createForex(Currency.EUR, Currency.USD);

        Mockito.when(this.securityDao.findSubscribedByFeedTypeForAutoActivateStrategiesInclFamily(eq(FeedType.SIM)))
            .thenReturn(Collections.singletonList((Security) forex));
        Mockito.when(this.securityDao.findSubscribedByFeedTypeAndStrategyInclFamily(eq(FeedType.SIM), eq(STRATEGY_NAME)))
            .thenReturn(Collections.singletonList((Security) forex));

        // do initSubscriptions
        this.impl.initSubscriptions();

        // verify externalMarketDataService.subscribe
        Mockito.verify(this.impl).subscribe(forex);

        // verify engine.sendEvent
        ArgumentCaptor<Object> argumentCaptor1 = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.serverEngine).sendEvent(argumentCaptor1.capture());

        List<Object> allEvents = argumentCaptor1.getAllValues();

        Assert.assertNotNull(allEvents);
        Assert.assertEquals(1, allEvents.size());
        Object event = allEvents.get(0);

        Assert.assertTrue(event instanceof SubscribeTickVO);
        SubscribeTickVO subscribeTick = (SubscribeTickVO) event;
        Tick tick = subscribeTick.getTick();
        Assert.assertNotNull(tick);
        Assert.assertSame(forex, tick.getSecurity());

        // verify fixAdapter.sendMessage
        Mockito.verify(this.fixAdapter, Mockito.times(1)).sendMessage(Mockito.<MarketDataRequest>any(), Mockito.anyString());

        // verify no event has been sent to the engine
        Mockito.verify(this.serverEngine, Mockito.never()).executeQuery(Mockito.anyString());
    }

    @Test
    public void testInitialSubscriptionsAlreadySubscribed() throws Exception {

        Mockito.when(this.fixSessionStateHolder.isLoggedOn()).thenReturn(Boolean.TRUE);
        Mockito.when(this.fixSessionStateHolder.isSubscribed()).thenReturn(Boolean.TRUE);
        Mockito.when(this.fixSessionStateHolder.onSubscribe()).thenReturn(Boolean.FALSE);

        Forex forex = createForex(Currency.EUR, Currency.USD);
        Mockito.when(this.securityDao.findSubscribedByFeedTypeForAutoActivateStrategiesInclFamily(FeedType.SIM))
                .thenReturn(Collections.singletonList((Security) forex));

        // do initSubscriptions
        this.impl.initSubscriptions();

        // verify externalMarketDataService.subscribe
        Mockito.verify(this.impl, Mockito.never()).subscribe(Mockito.<Security>any());
    }

    @Test
    public void testSubscribe() throws Exception {

        Mockito.when(this.fixSessionStateHolder.isLoggedOn()).thenReturn(Boolean.TRUE);

        Forex forex = createForex(Currency.EUR, Currency.USD);

        // Do subscribe
        this.impl.subscribe(forex);

        Mockito.verify(this.impl).getTickerId(forex);

        // verify engine.sendEvent
        ArgumentCaptor<Object> argumentCaptor1 = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.serverEngine).sendEvent(argumentCaptor1.capture());

        List<Object> allEvents = argumentCaptor1.getAllValues();

        Assert.assertNotNull(allEvents);
        Assert.assertEquals(1, allEvents.size());
        Object event = allEvents.get(0);

        Assert.assertTrue(event instanceof SubscribeTickVO);
        SubscribeTickVO subscribeTick = (SubscribeTickVO) event;
        Tick tick = subscribeTick.getTick();
        Assert.assertNotNull(tick);
        Assert.assertSame(forex, tick.getSecurity());

        // verify fixSessionFactory.sendMessage
        Mockito.verify(this.fixAdapter, Mockito.times(1)).sendMessage(Mockito.<MarketDataRequest>any(), Mockito.anyString());

        // verify engine.executeQuery does not get called
        Mockito.verify(this.serverEngine, Mockito.never()).executeQuery(Mockito.anyString());
    }

    @Test(expected = ServiceException.class)
    public void testSubscribeNotLoggedOn() throws Exception {

        Mockito.when(this.fixSessionStateHolder.isLoggedOn()).thenReturn(Boolean.FALSE);

        Forex forex = createForex(Currency.EUR, Currency.USD);

        // Do subscribe
        this.impl.subscribe(forex);
    }

    @Test
    public void testUnsubscribe() throws Exception {

        Mockito.when(this.fixSessionStateHolder.isLoggedOn()).thenReturn(Boolean.TRUE);
        Mockito.when(this.fixSessionStateHolder.isSubscribed()).thenReturn(Boolean.TRUE);

        Forex forex = createForex(Currency.EUR, Currency.USD);
        forex.setId(123);

        // Do unsubscribe
        this.impl.unsubscribe(forex);

        // verify no event has been sent to the engine
        Mockito.verify(this.serverEngine, Mockito.never()).sendEvent(Mockito.any());

        // verify fixSessionFactory.sendMessage
        // verify fixSessionFactory.sendMessage
        Mockito.verify(this.fixAdapter, Mockito.times(1)).sendMessage(Mockito.<MarketDataRequest>any(), Mockito.anyString());

        // verify the esper delete statement has been executed
        Mockito.verify(this.serverEngine).executeQuery("delete from TickWindow where security.id = 123");
    }

    @Test(expected = ServiceException.class)
    public void testUnsubscribeNotSubscribed() throws Exception {

        Mockito.when(this.fixSessionStateHolder.isLoggedOn()).thenReturn(Boolean.TRUE);
        Mockito.when(this.fixSessionStateHolder.isSubscribed()).thenReturn(Boolean.FALSE);

        Forex forex = createForex(Currency.EUR, Currency.USD);

        // Do unsubscribe
        this.impl.unsubscribe(forex);
    }

}
