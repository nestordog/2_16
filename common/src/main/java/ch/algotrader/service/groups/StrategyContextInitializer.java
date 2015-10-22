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
package ch.algotrader.service.groups;

import java.util.Objects;

import org.springframework.beans.factory.BeanCreationException;

import ch.algotrader.esper.Engine;

/**
 * Initializes the strategy context of a service implementing {@link StrategyContextAware}.
 * Performs a no-op if the service does not implement this interface.
 *
 * @author <a href="mailto:mterzer@algotrader.ch">Marco Terzer</a>
 */
public class StrategyContextInitializer {

    private final String serviceBeanName;
    private final Object serviceToInitialize;
    private final String strategyName;
    private final double weight;
    private final Engine engine;
    private final Object config;

    public StrategyContextInitializer(String serviceBeanName, Object serviceToInitialize, String strategyName, double weight, Engine engine, Object config) {
        this.serviceBeanName = Objects.requireNonNull(serviceBeanName, "serviceBeanName cannot be null");
        this.serviceToInitialize = Objects.requireNonNull(serviceToInitialize, "serviceToInitialize cannot be null");
        this.strategyName = Objects.requireNonNull(strategyName, "strategyName cannot be null");
        this.weight = weight;
        this.engine = Objects.requireNonNull(engine, "engine cannot be null");
        this.config = Objects.requireNonNull(config, "config cannot be null");
    }

    public void initStrategyContext() {
        if (serviceToInitialize instanceof StrategyContextAware<?>) {
            final StrategyContextAware<?> strategyContextAware = (StrategyContextAware<?>)serviceToInitialize;
            initStrategyContextAwareProps(strategyContextAware);
        }
    }

    @SuppressWarnings("unchecked")
    private <C> void initStrategyContextAwareProps(StrategyContextAware<C> strategyContextAware) {
        strategyContextAware.setStrategyName(strategyName);
        strategyContextAware.setWeight(weight);
        strategyContextAware.setEngine(engine);
        try {
            strategyContextAware.setConfig((C)config);
        } catch (ClassCastException e) {
            throw new BeanCreationException(serviceBeanName, "Initialization of bean <" + serviceBeanName + "> failed, config of type <" +
                    config.getClass().getName() + "> is not compatible with " + serviceToInitialize.getClass().getName() + ".setConfig(..)", e);
        }
    }

}
