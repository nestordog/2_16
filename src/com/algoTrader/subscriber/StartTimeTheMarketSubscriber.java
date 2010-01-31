package com.algoTrader.subscriber;

import java.math.BigDecimal;

import com.algoTrader.ServiceLocator;

public class StartTimeTheMarketSubscriber {

    public void update(int underlayingId, BigDecimal spot) {

        ServiceLocator.instance().getActionService().startTimeTheMarket(underlayingId, spot);
    }
}
