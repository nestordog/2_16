package com.algoTrader.subscriber;

import java.math.BigDecimal;

import com.algoTrader.ServiceLocator;

public class TimeTheMarketSubscriber {

    public void update(int stockOptionId, int underlayingId, BigDecimal spot) {

        ServiceLocator.instance().getActionService().timeTheMarket(stockOptionId, underlayingId, spot);
    }
}
