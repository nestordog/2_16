package com.algoTrader.service.sq;

import com.algoTrader.entity.Strategy;

public class SQAccountServiceImpl extends SQAccountServiceBase {

    @Override
    protected long handleGetNumberOfContractsByMargin(String strategyName, double initialMarginPerContractInBase) {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        return (long) (strategy.getAvailableFundsDouble() / initialMarginPerContractInBase);
    }

    @Override
    protected void handleProcessCashTransactions() throws Exception {
        // do nothing
    }
}
