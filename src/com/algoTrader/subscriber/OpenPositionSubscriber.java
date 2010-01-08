package com.algoTrader.subscriber;

import com.algoTrader.ServiceLocator;

public class OpenPositionSubscriber {

    public void update() {

        ServiceLocator.instance().getActionService().openPosition();
    }
}
