package com.algoTrader.subscriber;

import com.algoTrader.ServiceLocator;

public class SetExitValueSubscriber {

    public void update(int positionId, double exitValue) {

        ServiceLocator.instance().getActionService().setExitValue(positionId, exitValue);
    }
}
