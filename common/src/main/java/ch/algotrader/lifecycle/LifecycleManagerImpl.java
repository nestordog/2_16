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
package ch.algotrader.lifecycle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import ch.algotrader.enumeration.LifecyclePhase;
import ch.algotrader.enumeration.OperationMode;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.service.InitializingServiceI;
import ch.algotrader.service.SubscriptionService;
import ch.algotrader.vo.LifecycleEventVO;

/**
 * Default {@link ch.algotrader.lifecycle.LifecycleManager} implementation.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class LifecycleManagerImpl implements LifecycleManager, ApplicationContextAware {

    private final EngineManager engineManager;
    private final EventDispatcher eventDispatcher;
    private final SubscriptionService subscriptionService;

    private volatile ApplicationContext applicationContext;

    public LifecycleManagerImpl(final EngineManager engineManager, final EventDispatcher eventDispatcher, final SubscriptionService subscriptionService) {

        Validate.notNull(engineManager, "EngineManager is null");
        Validate.notNull(eventDispatcher, "EventDispatcher is null");

        this.engineManager = engineManager;
        this.eventDispatcher = eventDispatcher;
        this.subscriptionService = subscriptionService;
    }

    public LifecycleManagerImpl(final EngineManager engineManager, final EventDispatcher eventDispatcher) {

        this(engineManager, eventDispatcher, null);
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void runServices() {

        // initialize services
        Map<String, InitializingServiceI> allServices = this.applicationContext.getBeansOfType(InitializingServiceI.class);
        if (!allServices.isEmpty()) {

            List<InitializingServiceI> allServicesByPriority = new ArrayList<>(allServices.values());
            Collections.sort(allServicesByPriority, ServicePriorityComparator.INSTANCE);
            for (InitializingServiceI service: allServicesByPriority) {

                service.init();
            }
        }
    }

    @Override
    public void runServer() {

        Engine engine = this.engineManager.getServerEngine();
        engine.setInternalClock(true);
        engine.deployAllModules();

        runServices();

        broadcastLocal(LifecyclePhase.START);

        broadcastLocalOnShutdown(LifecyclePhase.EXIT);
    }

    @Override
    public void runEmbedded() {

        Engine serverEngine = this.engineManager.getServerEngine();
        serverEngine.setInternalClock(true);
        serverEngine.deployAllModules();

        runServices();

        Collection<Engine> engines = this.engineManager.getStrategyEngines();

        broadcastLocal(LifecyclePhase.INIT);

        for (Engine engine: engines) {
            engine.deployInitModules();
        }

        broadcastLocal(LifecyclePhase.PREFEED);

        for (Engine engine: engines) {
            engine.setInternalClock(true);
            engine.deployRunModules();
        }

        broadcastLocal(LifecyclePhase.START);

        broadcastLocalOnShutdown(LifecyclePhase.EXIT);
    }

    @Override
    public void runStrategy() {

        Collection<Engine> engines = this.engineManager.getEngines();

        broadcastLocal(LifecyclePhase.INIT);

        for (Engine engine: engines) {
            engine.deployInitModules();
        }

        broadcastLocal(LifecyclePhase.PREFEED);

        for (Engine engine: engines) {
            engine.setInternalClock(true);
            engine.deployRunModules();
        }

        broadcastLocal(LifecyclePhase.START);

        if (this.subscriptionService != null) {
            this.subscriptionService.initMarketDataEventSubscriptions();
            this.subscriptionService.initGenericEventSubscriptions();
        }

        broadcastLocalOnShutdown(LifecyclePhase.EXIT);
    }

    private void broadcastLocal(final LifecyclePhase phase) {
        this.eventDispatcher.broadcastLocal(new LifecycleEventVO(OperationMode.REAL_TIME, phase, new Date()));
    }

    private void broadcastLocalOnShutdown(final LifecyclePhase phase) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            this.eventDispatcher.broadcastLocal(new LifecycleEventVO(OperationMode.REAL_TIME, phase, new Date()));
        }));
    }

}
