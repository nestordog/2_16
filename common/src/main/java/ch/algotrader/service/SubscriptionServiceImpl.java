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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import ch.algotrader.cache.EntityCacheEvictionEventVO;
import ch.algotrader.cache.QueryCacheEvictionEventVO;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.Subscription;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class SubscriptionServiceImpl implements SubscriptionService, ApplicationContextAware {

    private final CommonConfig commonConfig;
    private final MarketDataService marketDataService;
    private final LookupService lookupService;
    private final EngineManager engineManager;

    private ApplicationContext applicationContext;

    public SubscriptionServiceImpl(
            final CommonConfig commonConfig,
            final MarketDataService marketDataService,
            final LookupService lookupService,
            final EngineManager engineManager) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(marketDataService, "MarketDataService is null");
        Validate.notNull(lookupService, "LookupService is null");
        Validate.notNull(engineManager, "EngineManager is null");

        this.commonConfig = commonConfig;
        this.marketDataService = marketDataService;
        this.lookupService = lookupService;
        this.engineManager = engineManager;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribeMarketDataEvent(final String strategyName, final long securityId) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        this.marketDataService.subscribe(strategyName, securityId);

        initMarketDataEventSubscriptions();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribeMarketDataEvent(final String strategyName, final long securityId, final String feedType) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(feedType, "Feed type is null");

        this.marketDataService.subscribe(strategyName, securityId, feedType);

        initMarketDataEventSubscriptions();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribeMarketDataEvent(final String strategyName, final long securityId) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        this.marketDataService.unsubscribe(strategyName, securityId);

        initMarketDataEventSubscriptions();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribeMarketDataEvent(final String strategyName, final long securityId, final String feedType) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(feedType, "Feed type is null");

        this.marketDataService.unsubscribe(strategyName, securityId, feedType);

        initMarketDataEventSubscriptions();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initMarketDataEventSubscriptions() {

        final Engine engine = getStrategyEngine();
        if (engine == null) {
            return;
        }

        // assemble the message selector
        List<String> selections = new ArrayList<>();
        for (Subscription subscription : this.lookupService.getSubscriptionsByStrategyInclComponentsAndProps(engine.getStrategyName())) {
            selections.add("securityId=" + subscription.getSecurity().getId());
        }

        String messageSelector = StringUtils.join(selections, " OR ");
        if ("".equals(messageSelector)) {
            messageSelector = "false";
        }

        final DefaultMessageListenerContainer marketDataMessageListenerContainer = this.applicationContext.getBean("marketDataMessageListenerContainer", DefaultMessageListenerContainer.class);

        updateMessageSelector(marketDataMessageListenerContainer, messageSelector);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribeGenericEvents(final Set<Class<?>> classes) {

        Validate.notNull(classes, "Classes is null");

        if (getStrategyEngine() == null) {
            return;
        }

        classes.add(EntityCacheEvictionEventVO.class);
        classes.add(QueryCacheEvictionEventVO.class);

        // assemble the message selector
        List<String> selections = new ArrayList<>();
        for (Class<?> clazz : classes) {
            selections.add("clazz='" + clazz.getName() + "'");
        }

        String messageSelector = StringUtils.join(selections, " OR ");
        if ("".equals(messageSelector)) {
            messageSelector = "false";
        }

        final DefaultMessageListenerContainer genericMessageListenerContainer = this.applicationContext.getBean("genericMessageListenerContainer", DefaultMessageListenerContainer.class);

        updateMessageSelector(genericMessageListenerContainer, messageSelector);

    }

    @Override
    public void initGenericEventSubscriptions() {
        subscribeGenericEvents(new HashSet<Class<?>>());
    }

    /**
     * If not simulation and not embedded, this method returns the single
     * strategy engine. If simulation or embedded, or if the
     * {@link EngineManager} contains multiple engines, or if the single engine
     * is the server engine, null is returned.
     *
     * @return  the single strategy engine if available, or null if simulation
     *          or embedded mode or if the single engine is the server engine
     */
    private Engine getStrategyEngine() {
        CommonConfig commonConfig = this.commonConfig;
        if (commonConfig.isSimulation() || commonConfig.isEmbedded()) {
            return null;
        }

        final List<Engine> strategyEngines = new ArrayList<>(this.engineManager.getStrategyEngines());
        return strategyEngines.size() == 1 ? strategyEngines.get(0) : null;
    }

    private void updateMessageSelector(final DefaultMessageListenerContainer genericMessageListenerContainer, String messageSelector) {

        genericMessageListenerContainer.setMessageSelector(messageSelector);

        // restart the container (must do this in a separate thread to prevent dead-locks)
        (new Thread() {
            @Override
            public void run() {
                genericMessageListenerContainer.stop();
                genericMessageListenerContainer.shutdown();
                genericMessageListenerContainer.start();
                genericMessageListenerContainer.initialize();
            }
        }).start();
    }
}
