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
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.Subscription;
import ch.algotrader.enumeration.FeedType;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class SubscriptionServiceImpl implements SubscriptionService, ApplicationContextAware {

    private final CommonConfig commonConfig;
    private final MarketDataService marketDataService;
    private final LookupService lookupService;

    private ApplicationContext applicationContext;

    public SubscriptionServiceImpl(
            final CommonConfig commonConfig,
            final MarketDataService marketDataService,
            final LookupService lookupService) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(marketDataService, "MarketDataService is null");
        Validate.notNull(lookupService, "LookupService is null");

        this.commonConfig = commonConfig;
        this.marketDataService = marketDataService;
        this.lookupService = lookupService;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribeMarketDataEvent(final String strategyName, final int securityId) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        this.marketDataService.subscribe(strategyName, securityId);

        initMarketDataEventSubscriptions();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribeMarketDataEvent(final String strategyName, final int securityId, final FeedType feedType) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(feedType, "Feed type is null");

        this.marketDataService.subscribe(strategyName, securityId, feedType);

        initMarketDataEventSubscriptions();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribeMarketDataEvent(final String strategyName, final int securityId) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        this.marketDataService.unsubscribe(strategyName, securityId);

        initMarketDataEventSubscriptions();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribeMarketDataEvent(final String strategyName, final int securityId, final FeedType feedType) {

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

        CommonConfig commonConfig = this.commonConfig;
        if (commonConfig.isSimulation() || commonConfig.isStartedStrategyBASE() || commonConfig.isSingleVM())
            return;

        // assemble the message selector
        List<String> selections = new ArrayList<String>();
        for (Subscription subscription : this.lookupService.getSubscriptionsByStrategyInclComponents(commonConfig.getStrategyName())) {
            selections.add("securityId=" + subscription.getSecurity().getId());
        }

        String messageSelector = StringUtils.join(selections, " OR ");
        if ("".equals(messageSelector)) {
            messageSelector = "false";
        }

        final DefaultMessageListenerContainer marketDataMessageListenerContainer = this.applicationContext.getBean("genericMessageListenerContainer", DefaultMessageListenerContainer.class);

        // update the message selector
        marketDataMessageListenerContainer.setMessageSelector(messageSelector);

        // restart the container (must do this in a separate thread to prevent dead-locks)
        (new Thread() {
            @Override
            public void run() {
                marketDataMessageListenerContainer.stop();
                marketDataMessageListenerContainer.shutdown();
                marketDataMessageListenerContainer.start();
                marketDataMessageListenerContainer.initialize();
            }
        }).start();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribeGenericEvents(final Class[] classes) {

        Validate.notNull(classes, "Classes is null");

        CommonConfig commonConfig = this.commonConfig;
        if (commonConfig.isSimulation() || commonConfig.isStartedStrategyBASE() || commonConfig.isSingleVM())
            return;

        // assemble the message selector
        List<String> selections = new ArrayList<String>();
        for (Class<?> clazz : classes) {
            selections.add("clazz='" + clazz.getName() + "'");
        }

        String messageSelector = StringUtils.join(selections, " OR ");
        if ("".equals(messageSelector)) {
            messageSelector = "false";
        }

        final DefaultMessageListenerContainer genericMessageListenerContainer = this.applicationContext.getBean("genericMessageListenerContainer", DefaultMessageListenerContainer.class);

        // update the message selector
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
