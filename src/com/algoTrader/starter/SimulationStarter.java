package com.algoTrader.starter;

import com.algoTrader.ServiceLocator;

public class SimulationStarter {

    public static void main(String[] args) {

        ServiceLocator.instance().getRuleService().activateAll();
        ServiceLocator.instance().getSimulationService().simulateWatchlist();
    }
}
