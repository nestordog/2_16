package com.algoTrader.service.sim;

public class SimAccountServiceImpl extends SimAccountServiceBase {

    @Override
    protected long handleGetQuantityByMargin(String strategyName, double initialMarginPerContractInBase) throws Exception {
        return (long) (getPortfolioService().getAvailableFundsDouble(strategyName) / initialMarginPerContractInBase);
    }

    @Override
    protected long handleGetQuantityByAllocation(String strategyName, long requestedQuantity) throws Exception {
        return requestedQuantity;
    }
}
