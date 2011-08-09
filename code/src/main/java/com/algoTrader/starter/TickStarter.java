package com.algoTrader.starter;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.service.RuleService;

public class TickStarter {

    public static void main(String[] args) {

        start();
    }

    public static void start() {

        // initialitze IB services
        ServiceLocator.serverInstance().getIbService().init();

        // start all BASE rules
        RuleService ruleService = ServiceLocator.serverInstance().getRuleService();
        ruleService.initServiceProvider(StrategyImpl.BASE);
        ruleService.setInternalClock(StrategyImpl.BASE, true);
        ruleService.deployAllModules(StrategyImpl.BASE);
    }
}
