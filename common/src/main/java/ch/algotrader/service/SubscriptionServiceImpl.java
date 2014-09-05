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
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.Subscription;
import ch.algotrader.enumeration.FeedType;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class SubscriptionServiceImpl implements SubscriptionService {

    public final DefaultMessageListenerContainer marketDataMessageListenerContainer;
    public final DefaultMessageListenerContainer genericMessageListenerContainer;
    public final DefaultMessageListenerContainer strategyMessageListenerContainer;

    private final CommonConfig commonConfig;

    private final MarketDataService marketDataService;

    private final LookupService lookupService;

    public SubscriptionServiceImpl(
            final CommonConfig commonConfig,
            final MarketDataService marketDataService,
            final LookupService lookupService,
            final DefaultMessageListenerContainer marketDataMessageListenerContainer,
            final DefaultMessageListenerContainer genericMessageListenerContainer,
            final DefaultMessageListenerContainer strategyMessageListenerContainer) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(marketDataService, "MarketDataService is null");
        Validate.notNull(lookupService, "LookupService is null");

        this.marketDataMessageListenerContainer = marketDataMessageListenerContainer;
        this.genericMessageListenerContainer = genericMessageListenerContainer;
        this.strategyMessageListenerContainer = strategyMessageListenerContainer;
        this.commonConfig = commonConfig;
        this.marketDataService = marketDataService;
        this.lookupService = lookupService;

    }

    public SubscriptionServiceImpl(
            final CommonConfig commonConfig,
            final MarketDataService marketDataService,
            final LookupService lookupService) {
        this(commonConfig, marketDataService, lookupService, null, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribeMarketDataEvent(final String strategyName, final int securityId) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        try {
            this.marketDataService.subscribe(strategyName, securityId);

            initMarketDataEventSubscriptions();
        } catch (Exception ex) {
            throw new SubscriptionServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribeMarketDataEvent(final String strategyName, final int securityId, final FeedType feedType) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(feedType, "Feed type is null");

        try {
            this.marketDataService.subscribe(strategyName, securityId, feedType);

            initMarketDataEventSubscriptions();
        } catch (Exception ex) {
            throw new SubscriptionServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribeMarketDataEvent(final String strategyName, final int securityId) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        try {
            this.marketDataService.unsubscribe(strategyName, securityId);

            initMarketDataEventSubscriptions();
        } catch (Exception ex) {
            throw new SubscriptionServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribeMarketDataEvent(final String strategyName, final int securityId, final FeedType feedType) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(feedType, "Feed type is null");

        try {
            this.marketDataService.unsubscribe(strategyName, securityId, feedType);

            initMarketDataEventSubscriptions();
        } catch (Exception ex) {
            throw new SubscriptionServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initMarketDataEventSubscriptions() {

        try {
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

            // update the message selector
            this.marketDataMessageListenerContainer.setMessageSelector(messageSelector);

            // restart the container (must do this in a separate thread to prevent dead-locks)
            (new Thread() {
                @Override
                public void run() {
                    SubscriptionServiceImpl.this.marketDataMessageListenerContainer.stop();
                    SubscriptionServiceImpl.this.marketDataMessageListenerContainer.shutdown();
                    SubscriptionServiceImpl.this.marketDataMessageListenerContainer.start();
                    SubscriptionServiceImpl.this.marketDataMessageListenerContainer.initialize();
                }
            }).start();
        } catch (Exception ex) {
            throw new SubscriptionServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribeGenericEvents(final Class[] classes) {

        Validate.notNull(classes, "Classes is null");

        try {
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

            // update the message selector
            this.genericMessageListenerContainer.setMessageSelector(messageSelector);

            // restart the container (must do this in a separate thread to prevent dead-locks)
            (new Thread() {
                @Override
                public void run() {
                    SubscriptionServiceImpl.this.genericMessageListenerContainer.stop();
                    SubscriptionServiceImpl.this.genericMessageListenerContainer.shutdown();
                    SubscriptionServiceImpl.this.genericMessageListenerContainer.start();
                    SubscriptionServiceImpl.this.genericMessageListenerContainer.initialize();
                }
            }).start();
        } catch (Exception ex) {
            throw new SubscriptionServiceException(ex.getMessage(), ex);
        }
    }
}
