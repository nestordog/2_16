package com.algoTrader.service.sq;

import com.algoTrader.entity.Strategy;

public class SqAccountServiceImpl extends SqAccountServiceBase {

    protected long handleGetNumberOfContractsByMargin(String strategyName, double initialMarginPerContractInBase) {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        return (long) (strategy.getAvailableFundsDouble() / initialMarginPerContractInBase);
    }

    protected void handleProcessCashTransactions() throws Exception {
        // do nothing
    }
}
