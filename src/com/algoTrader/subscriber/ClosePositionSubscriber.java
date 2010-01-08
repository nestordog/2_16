package com.algoTrader.subscriber;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Position;

public class ClosePositionSubscriber {

    public void update(Position position) {

        ServiceLocator.instance().getActionService().closePosition(position);
    }
}
