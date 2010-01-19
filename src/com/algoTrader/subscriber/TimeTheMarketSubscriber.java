package com.algoTrader.subscriber;

import java.math.BigDecimal;

import com.algoTrader.ServiceLocator;

public class TimeTheMarketSubscriber {

    public void update(int securityId, BigDecimal spot) {

        ServiceLocator.instance().getActionService().timeTheMarket(securityId, spot);
    }
}
