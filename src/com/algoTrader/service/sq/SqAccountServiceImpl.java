package com.algoTrader.service.sq;

import com.algoTrader.entity.Strategy;

public class SqAccountServiceImpl extends SqAccountServiceBase {

    protected long handleGetNumberOfContractsByMargin(String strategyName, double initialMarginPerContract) {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        return (long) (strategy.getAvailableFundsDouble() / initialMarginPerContract);
    }

    protected void handleProcessCashTransactions() throws Exception {
        // do nothing
    }
}
