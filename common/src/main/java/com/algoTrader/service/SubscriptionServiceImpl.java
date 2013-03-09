/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package com.algoTrader.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import com.algoTrader.entity.Subscription;
import com.algoTrader.util.StrategyUtil;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class SubscriptionServiceImpl extends SubscriptionServiceBase {

    private @Value("${simulation}") boolean simulation;

    public DefaultMessageListenerContainer marketDataMessageListenerContainer;
    public DefaultMessageListenerContainer genericMessageListenerContainer;
    public DefaultMessageListenerContainer strategyMessageListenerContainer;

    public void setMarketDataMessageListenerContainer(DefaultMessageListenerContainer marketDataMessageListenerContainer) {
        this.marketDataMessageListenerContainer = marketDataMessageListenerContainer;
    }

    public void setGenericMessageListenerContainer(DefaultMessageListenerContainer genericMessageListenerContainer) {
        this.genericMessageListenerContainer = genericMessageListenerContainer;
    }

    public void setStrategyMessageListenerContainer(DefaultMessageListenerContainer strategyMessageListenerContainer) {
        this.strategyMessageListenerContainer = strategyMessageListenerContainer;
    }

    @Override
    protected void handleUnsubscribeMarketDataEvent(String strategyName, int securityId) throws Exception {

        getMarketDataService().unsubscribe(strategyName, securityId);

        initMarketDataEventSubscriptions();
    }

    @Override
    protected void handleSubscribeMarketDataEvent(String strategyName, int securityId) throws Exception {

        getMarketDataService().subscribe(strategyName, securityId);

        initMarketDataEventSubscriptions();
    }

    @Override
    protected void handleInitMarketDataEventSubscriptions() throws Exception {

        if (this.simulation || StrategyUtil.isStartedStrategyBASE())
            return;

        // assemble the message selector
        List<String> selections = new ArrayList<String>();
        for (Subscription subscription : getLookupService().getSubscriptionsByStrategyInclComponents(StrategyUtil.getStartedStrategyName())) {
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
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected void handleSubscribeGenericEvents(Class[] classes) throws Exception {

        if (this.simulation || StrategyUtil.isStartedStrategyBASE())
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
    }
}
