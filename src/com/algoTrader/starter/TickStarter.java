package com.algoTrader.starter;

import com.algoTrader.ServiceLocator;
import com.algoTrader.enumeration.RuleName;
import com.algoTrader.service.RuleService;
import com.algoTrader.service.SimulationService;

public class TickStarter {

    public static void main(String[] args) {

        start();
    }

    public static void start() {
        RuleService ruleService = ServiceLocator.instance().getRuleService();
        SimulationService simulationService = ServiceLocator.instance().getSimulationService();

        // run all the recorded ticks through to initialize the oscilators
        ruleService.activate(RuleName.CREATE_K_FAST);
        ruleService.activate(RuleName.CREATE_K_SLOW);
        ruleService.activate(RuleName.CREATE_D_SLOW);
        //ruleService.activate(RuleName.PRINT_STOCHASTIC);

        simulationService.simulateByUnderlayings();

        // switch to internalClock and activate the rest of the rules
        ruleService.setInternalClock();
        ruleService.activateAll();
    }
}
