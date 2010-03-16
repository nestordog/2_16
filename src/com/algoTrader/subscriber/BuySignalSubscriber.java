package com.algoTrader.subscriber;

import java.math.BigDecimal;

import com.algoTrader.ServiceLocator;

public class BuySignalSubscriber {

    public void update(int underlayingId, BigDecimal spot) {

        ServiceLocator.instance().getActionService().buySignal(underlayingId, spot);
    }
}
