package com.algoTrader.service;

public abstract class SecurityRetrieverServiceImpl extends SecurityRetrieverServiceBase {

    @Override
    protected abstract void handleRetrieve(int securityFamilyId) throws Exception;
}
