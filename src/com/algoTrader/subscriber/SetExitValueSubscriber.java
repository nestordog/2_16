package com.algoTrader.subscriber;

import java.math.BigDecimal;

import com.algoTrader.ServiceLocator;

public class SetExitValueSubscriber {

    public void update(int positionId, BigDecimal exitValue) {

        ServiceLocator.instance().getActionService().setExitValue(positionId, exitValue);
    }
}
