package com.algoTrader.subscriber;

import java.math.BigDecimal;

import com.algoTrader.ServiceLocator;

public class RollPositionSubscriber {

    public void update(int securityId, int underlayingId, BigDecimal underlayingSpot) {

        ServiceLocator.instance().getActionService().rollPosition(securityId, underlayingId, underlayingSpot);
    }
}
