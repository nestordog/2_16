package com.algoTrader.subscriber;

import java.math.BigDecimal;

import com.algoTrader.ServiceLocator;

public class ExpirePositionSubscriber {

    public void update(int positionId, int underlayingId, BigDecimal underlayingSpot) {

        ServiceLocator.instance().getActionService().expirePosition(positionId, underlayingId, underlayingSpot);
    }
}
