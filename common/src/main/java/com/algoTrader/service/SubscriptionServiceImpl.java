package com.algoTrader.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.Subscription;
import com.algoTrader.util.StrategyUtil;

public class SubscriptionServiceImpl extends SubscriptionServiceBase {

    private @Value("${simulation}") boolean simulation;

    public DefaultMessageListenerContainer marketDataMessageListenerContainer;
    public DefaultMessageListenerContainer strategyMessageListenerContainer;

    public void setMarketDataMessageListenerContainer(DefaultMessageListenerContainer marketDataMessageListenerContainer) {
        this.marketDataMessageListenerContainer = marketDataMessageListenerContainer;
    }

    public void setStrategyMessageListenerContainer(DefaultMessageListenerContainer strategyMessageListenerContainer) {
        this.strategyMessageListenerContainer = strategyMessageListenerContainer;
    }

    @Override
    protected void handleUnsubscribe(String strategyName, int securityId) throws Exception {

        getMarketDataService().unsubscribe(strategyName, securityId);

        initSubscriptions(strategyName);
    }

    @Override
    protected void handleSubscribe(String strategyName, int securityId) throws Exception {

        getMarketDataService().subscribe(strategyName, securityId);

        initSubscriptions(strategyName);
    }

    @Override
    protected void handleInitSubscriptions(String strategyName) throws Exception {

        if (this.simulation || StrategyUtil.isStartedStrategyBASE())
            return;

        // assemble the message selector
        List<String> selections = new ArrayList<String>();
        Strategy strategy = getLookupService().getStrategyByName(StrategyUtil.getStartedStrategyName());
        for (Subscription subscription : strategy.getSubscriptions()) {
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
}
