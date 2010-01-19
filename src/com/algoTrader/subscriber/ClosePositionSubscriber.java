package com.algoTrader.subscriber;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Position;

public class ClosePositionSubscriber {

    public void update(int positionId) {

        ServiceLocator.instance().getActionService().closePosition(positionId);
    }
}
