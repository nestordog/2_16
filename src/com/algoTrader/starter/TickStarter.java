package com.algoTrader.starter;

import com.algoTrader.ServiceLocator;

public class TickStarter {

    public static void main(String[] args) throws InterruptedException {

        ServiceLocator.instance().getRuleService().activateAll();
    }
}
