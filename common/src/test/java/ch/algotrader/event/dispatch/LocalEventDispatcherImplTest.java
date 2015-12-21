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
package ch.algotrader.event.dispatch;

import java.util.Arrays;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.event.EventBroadcaster;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class LocalEventDispatcherImplTest {

    @Mock
    private EventBroadcaster localEventBroadcaster;
    @Mock
    private EngineManager engineManager;
    @Mock
    private Engine serverEngine;
    @Mock
    private Engine engine1;
    @Mock
    private Engine engine2;

    private LocalEventDispatcherImpl impl;

    @Before
    public void setup() {
        Mockito.when(engineManager.hasEngine("SERVER")).thenReturn(true);
        Mockito.when(engineManager.hasEngine("this")).thenReturn(true);
        Mockito.when(engineManager.hasEngine("that")).thenReturn(true);
        Mockito.when(engineManager.getEngine("SERVER")).thenReturn(serverEngine);
        Mockito.when(engineManager.getEngine("this")).thenReturn(engine1);
        Mockito.when(engineManager.getEngine("that")).thenReturn(engine2);
        Mockito.when(engineManager.lookup("SERVER")).thenReturn(serverEngine);
        Mockito.when(engineManager.lookup("this")).thenReturn(engine1);
        Mockito.when(engineManager.lookup("that")).thenReturn(engine2);
        Mockito.when(engineManager.getEngines()).thenReturn(Arrays.asList(serverEngine, engine1, engine2));
        Mockito.when(engineManager.getStrategyEngines()).thenReturn(Arrays.asList(engine1, engine2));

        impl = new LocalEventDispatcherImpl(localEventBroadcaster, engineManager);
    }

    @Test
    public void testSendEvent() throws Exception {

        Object event = new Object();
        impl.sendEvent("this", event);

        Mockito.verify(engine1).sendEvent(event);
        Mockito.verifyZeroInteractions(engine2);
    }

    @Test
    public void testRegisterUnregisterMarketEventSubscriptions() throws Exception {

        Assert.assertFalse(impl.isMarketDataSubscriptionRegistered(1L, "this"));
        impl.registerMarketDataSubscription("this", 1L);
        Assert.assertTrue(impl.isMarketDataSubscriptionRegistered(1L, "this"));

        impl.registerMarketDataSubscription("this", 2L);
        impl.registerMarketDataSubscription("that", 1L);
        impl.registerMarketDataSubscription("that", 3L);

        Assert.assertTrue(impl.isMarketDataSubscriptionRegistered(1L, "that"));
        Assert.assertTrue(impl.isMarketDataSubscriptionRegistered(2L, "this"));
        Assert.assertTrue(impl.isMarketDataSubscriptionRegistered(3L, "that"));

        impl.unregisterMarketDataSubscription("that", 3L);
        Assert.assertFalse(impl.isMarketDataSubscriptionRegistered(3L, "that"));
    }

    @Test
    public void testSendMarketDataEvent() throws Exception {

        impl.registerMarketDataSubscription("this", 1L);
        impl.registerMarketDataSubscription("that", 1L);

        TickVO tick1 = new TickVO(1L, new Date(), FeedType.IB.name(), 1L, 0, 0, 0);
        TickVO tick2 = new TickVO(2L, new Date(), FeedType.IB.name(), 2L, 0, 0, 0);
        impl.sendMarketDataEvent(tick1);
        impl.sendMarketDataEvent(tick2);

        Mockito.verify(engine1, Mockito.times(1)).sendEvent(tick1);
        Mockito.verify(engine1, Mockito.never()).sendEvent(tick2);
        Mockito.verify(engine2, Mockito.times(1)).sendEvent(tick1);
        Mockito.verify(engine2, Mockito.never()).sendEvent(tick2);
        Mockito.verify(localEventBroadcaster, Mockito.times(1)).broadcast(tick1);
        Mockito.verify(localEventBroadcaster, Mockito.times(1)).broadcast(tick2);
    }

    @Test
    public void testBroadcast() throws Exception {

        Object event = new Object();
        impl.broadcast(event);

        Mockito.verify(serverEngine).sendEvent(event);
        Mockito.verify(engine1).sendEvent(event);
        Mockito.verify(engine2).sendEvent(event);
        Mockito.verify(localEventBroadcaster).broadcast(event);
    }

    @Test
    public void testBroadcastLocal() throws Exception {

        Object event = new Object();
        impl.broadcastLocal(event);

        Mockito.verify(serverEngine).sendEvent(event);
        Mockito.verify(engine1).sendEvent(event);
        Mockito.verify(engine2).sendEvent(event);
        Mockito.verify(localEventBroadcaster).broadcast(event);
    }

    @Test
    public void testBroadcastLocalStrategies() throws Exception {

        Object event = new Object();
        impl.broadcastLocalStrategies(event);

        Mockito.verify(serverEngine, Mockito.never()).sendEvent(event);
        Mockito.verify(engine1).sendEvent(event);
        Mockito.verify(engine2).sendEvent(event);
        Mockito.verify(localEventBroadcaster).broadcast(event);
    }

    @Test
    public void testBroadcastAllStrategies() throws Exception {

        Object event = new Object();
        impl.broadcastAllStrategies(event);

        Mockito.verify(serverEngine, Mockito.never()).sendEvent(event);
        Mockito.verify(engine1).sendEvent(event);
        Mockito.verify(engine2).sendEvent(event);
        Mockito.verify(localEventBroadcaster).broadcast(event);
    }

}
