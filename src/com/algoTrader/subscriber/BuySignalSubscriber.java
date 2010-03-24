package com.algoTrader.subscriber;

import java.math.BigDecimal;

import com.algoTrader.ServiceLocator;

public class BuySignalSubscriber {

    public void update(int underlayingId, BigDecimal underlayingSpot) {

        ServiceLocator.instance().getActionService().buySignal(underlayingId, underlayingSpot);
    }
}
