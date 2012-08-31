package com.algoTrader.service;

import java.util.List;

public abstract class ReconciliationServiceImpl extends ReconciliationServiceBase {

    @Override
    protected abstract void handleReconcile() throws Exception;

    @Override
    protected abstract void handleReconcile(List<String> fileNames) throws Exception;
}
