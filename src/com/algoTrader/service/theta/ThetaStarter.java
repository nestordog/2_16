package com.algoTrader.service.theta;

import com.algoTrader.ServiceLocator;
import com.algoTrader.service.RuleService;
import com.algoTrader.service.StrategyService;
import com.algoTrader.util.ConfigurationUtil;

public class ThetaStarter {

    public static void main(String[] args) {

        start();
    }

    public static void start() {

        String strategyName = ConfigurationUtil.getBaseConfig().getString("strategyName");

        RuleService ruleService = ServiceLocator.commonInstance().getRuleService();
        ThetaService thetaService = ServiceLocator.commonInstance().getThetaService();
        StrategyService strategyService = ServiceLocator.commonInstance().getStrategyService();

        ruleService.initServiceProvider(strategyName);

        // run all the recorded ticks through to initialize MACD and stochastic
        ruleService.activateInit(strategyName);
        thetaService.prefeedTicks(strategyName);

        // switch to internalClock
        ruleService.setInternalClock(strategyName, true);

        //activate the rest of the rules
        ruleService.activateAll(strategyName);

        // register the strategy with BASE so we can receive events
        strategyService.registerStrategy(strategyName);
    }
}
