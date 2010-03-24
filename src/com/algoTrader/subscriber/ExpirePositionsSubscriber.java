package com.algoTrader.subscriber;

import com.algoTrader.ServiceLocator;

public class ExpirePositionsSubscriber {

    public void update(int positionId) {

        ServiceLocator.instance().getActionService().expirePosition(positionId);
    }
}
