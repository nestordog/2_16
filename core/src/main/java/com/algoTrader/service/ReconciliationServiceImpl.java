package com.algoTrader.service;

public abstract class ReconciliationServiceImpl extends ReconciliationServiceBase {

    @Override
    protected abstract void handleReconcile(String fileName, byte[] data) throws Exception;
}
