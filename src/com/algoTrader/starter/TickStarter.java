package com.algoTrader.starter;

import com.algoTrader.ServiceLocator;
import com.algoTrader.enumeration.RuleName;
import com.algoTrader.service.RuleService;
import com.algoTrader.service.TickService;
import com.algoTrader.util.EsperService;

public class TickStarter {

    public static void main(String[] args) {

        start();
    }

    public static void start() {
        RuleService ruleService = ServiceLocator.instance().getRuleService();
        TickService tickService = ServiceLocator.instance().getDispatcherService().getTickService();

        // run all the recorded ticks through to initialize MACD and stochastic
        ruleService.activate(RuleName.CREATE_END_OF_DAY_TICK);
        ruleService.activate(RuleName.CREATE_MACD);
        ruleService.activate(RuleName.CREATE_MACD_SIGNAL);
        ruleService.activate(RuleName.SET_TREND);

        ruleService.activate(RuleName.CREATE_K_FAST);
        ruleService.activate(RuleName.CREATE_K_SLOW);
        ruleService.activate(RuleName.CREATE_D_SLOW);

        tickService.prefeedTicks();

        // switch to internalClock
        EsperService.setInternalClock(true);
        EsperService.enableJmx();

        //activate the rest of the rules
        ruleService.activateAll();
    }
}
