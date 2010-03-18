package com.algoTrader.starter;

import com.algoTrader.ServiceLocator;
import com.algoTrader.enumeration.RuleName;


public class TickStarterImmediateOpenPosition {

    public static void main(String[] args) {

        start();
    }

    public static void start() {

        TickStarter.start();
        ServiceLocator.instance().getRuleService().activate(RuleName.IMMEDIATE_BUY_SIGNAL);
    }
}
