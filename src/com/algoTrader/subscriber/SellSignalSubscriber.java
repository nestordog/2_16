package com.algoTrader.subscriber;

import java.math.BigDecimal;

import com.algoTrader.ServiceLocator;

public class SellSignalSubscriber {

    public void update(int underlayingId, BigDecimal underlayingSpot) {

        ServiceLocator.instance().getActionService().sellSignal(underlayingId, underlayingSpot);
    }
}
