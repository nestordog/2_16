package com.algoTrader.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.WatchListItem;
import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.StrategyUtil;

public class WatchListServiceImpl extends WatchListServiceBase {

    private static boolean simulation = ConfigurationUtil.getBaseConfig().getBoolean("simulation");

    public DefaultMessageListenerContainer marketDataMessageListenerContainer;
    public DefaultMessageListenerContainer strategyMessageListenerContainer;

    public void setMarketDataMessageListenerContainer(DefaultMessageListenerContainer marketDataMessageListenerContainer) {
        this.marketDataMessageListenerContainer = marketDataMessageListenerContainer;
    }

    public void setStrategyMessageListenerContainer(DefaultMessageListenerContainer strategyMessageListenerContainer) {
        this.strategyMessageListenerContainer = strategyMessageListenerContainer;
    }

    @Override
    protected void handleRemoveFromWatchlist(String strategyName, int securityId) throws Exception {

        getMarketDataService().removeFromWatchlist(strategyName, securityId);

        initWatchlist(strategyName);
    }

    @Override
    protected void handlePutOnWatchlist(String strategyName, int securityId) throws Exception {

        getMarketDataService().putOnWatchlist(strategyName, securityId);

        initWatchlist(strategyName);
    }

    @Override
    protected void handleInitWatchlist(String strategyName) throws Exception {

        if (simulation)
            return;

        // assemble the message selector
        List<String> selections = new ArrayList<String>();
        Strategy strategy = getLookupService().getStrategyByName(StrategyUtil.getStartedStrategyName());
        for (WatchListItem watchlistItem : strategy.getWatchListItems()) {
            selections.add("securityId=" + watchlistItem.getSecurity().getId());
        }

        String messageSelector = StringUtils.join(selections, " OR ");
        if ("".equals(messageSelector)) {
            messageSelector = "false";
        }

        // update the message selector
        this.marketDataMessageListenerContainer.setMessageSelector(messageSelector);
    }
}
