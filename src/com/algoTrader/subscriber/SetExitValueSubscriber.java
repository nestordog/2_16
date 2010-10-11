package com.algoTrader.subscriber;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Security;

public class SetExitValueSubscriber {

    public void update(Security security, double exitValue) {

        ServiceLocator.instance().getActionService().setExitValue(security.getPosition().getId(), exitValue);
    }
}
