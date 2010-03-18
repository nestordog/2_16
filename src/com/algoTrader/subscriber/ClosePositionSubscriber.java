package com.algoTrader.subscriber;

import com.algoTrader.ServiceLocator;

public class ClosePositionSubscriber {

    public void update(int positionId) {

        ServiceLocator.instance().getActionService().closePosition(positionId);
    }
}
