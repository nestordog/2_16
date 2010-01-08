package com.algoTrader.subscriber;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Position;
import com.algoTrader.util.MyLogger;

public class SetExitValueSubscriber {

    public void update(Position position, BigDecimal exitValue) {

        ServiceLocator.instance().getActionService().setExitValue(position, exitValue);
    }
}
