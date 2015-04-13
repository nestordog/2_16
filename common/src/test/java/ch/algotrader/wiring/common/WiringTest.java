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
package ch.algotrader.wiring.common;

import java.util.Date;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.ConfigParams;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.enumeration.ConnectionState;
import ch.algotrader.enumeration.LifecyclePhase;
import ch.algotrader.enumeration.OperationMode;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.event.EventListenerRegistry;
import ch.algotrader.event.listener.LifecycleEventListener;
import ch.algotrader.service.LookupService;
import ch.algotrader.vo.LifecycleEventVO;
import ch.algotrader.vo.SessionEventVO;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class WiringTest {

    @Test
    public void testCommonConfigWiring() throws Exception {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(ConfigParamsWiring.class);
        context.refresh();

        Assert.assertNotNull(context.getBean(ConfigParams.class));
        Assert.assertNotNull(context.getBean(CommonConfig.class));
        Assert.assertNotNull(context.getBean("properties", Properties.class));
    }

    @Test
    public void testEngineManagerWiring() throws Exception {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        LookupService lookupService = Mockito.mock(LookupService.class);
        context.getDefaultListableBeanFactory().registerSingleton("lookupService", lookupService);

        Engine engine = Mockito.mock(Engine.class);
        context.getDefaultListableBeanFactory().registerSingleton("someEngine", engine);

        Mockito.when(engine.getEngineName()).thenReturn("someStrategy");
        Mockito.when(engine.getStrategyName()).thenReturn("someStrategy");
        Strategy strategy = new StrategyImpl();
        Mockito.when(lookupService.getStrategyByName("someStrategy")).thenReturn(strategy);

        context.register(ConfigParamsWiring.class, EngineManagerWiring.class);
        context.refresh();
        Assert.assertNotNull(context.getBean(EngineManager.class));

        Mockito.verify(engine).setVariableValue("engineStrategy", strategy);
    }

    @Test
    public void testEventDispatcherWiring() throws Exception {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        LookupService lookupService = Mockito.mock(LookupService.class);
        context.getDefaultListableBeanFactory().registerSingleton("lookupService", lookupService);

        LifecycleEventListener lifecycleEventListener = Mockito.mock(LifecycleEventListener.class);
        context.getDefaultListableBeanFactory().registerSingleton("lifecycleEventListener", lifecycleEventListener);

        context.register(ConfigParamsWiring.class, EngineManagerWiring.class, EventDispatchWiring.class);
        context.refresh();

        EventListenerRegistry registry = context.getBean(EventListenerRegistry.class);
        Assert.assertNotNull(registry);

        registry.broadcast(new LifecycleEventVO(OperationMode.REAL_TIME, LifecyclePhase.INIT, new Date()));
        registry.broadcast(new SessionEventVO(ConnectionState.CONNECTED, "blah"));

        Mockito.verify(lifecycleEventListener, Mockito.times(1)).onChange(Mockito.any());
    }
}
