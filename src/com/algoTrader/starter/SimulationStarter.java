package com.algoTrader.starter;

import com.algoTrader.ServiceLocator;

public class SimulationStarter {

    public static void main(String[] args) {

        start();
    }

    public static void start() {

        ServiceLocator.instance().getRuleService().activateAll();
        ServiceLocator.instance().getSimulationService().run();
    }
}
