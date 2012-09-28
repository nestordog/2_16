package com.algoTrader.service.sim;

import com.algoTrader.entity.security.Security;

public class SimMarketDataServiceImpl extends SimMarketDataServiceBase {

    @Override
    protected void handleExternalSubscribe(Security security) throws Exception {
        throw new UnsupportedOperationException("ExternalSubscribe not allowed in simulation");
    }

    @Override
    protected void handleExternalUnsubscribe(Security security) throws Exception {
        throw new UnsupportedOperationException("ExternalUnsubscribe not allowed in simulation");
    }
}
