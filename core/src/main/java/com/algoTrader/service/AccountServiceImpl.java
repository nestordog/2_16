package com.algoTrader.service;

public abstract class AccountServiceImpl extends AccountServiceBase {

    @Override
    protected abstract long handleGetQuantityByMargin(String strategyName, double initialMarginPerContractInBase) throws Exception;

    @Override
    protected abstract long handleGetQuantityByAllocation(String strategyName, long requestedQuantity) throws Exception;
}
