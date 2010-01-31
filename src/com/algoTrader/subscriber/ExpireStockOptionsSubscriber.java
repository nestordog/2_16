package com.algoTrader.subscriber;

import com.algoTrader.ServiceLocator;

public class ExpireStockOptionsSubscriber {

    public void update(int positionId) {

        ServiceLocator.instance().getActionService().expireStockOption(positionId);
    }
}
