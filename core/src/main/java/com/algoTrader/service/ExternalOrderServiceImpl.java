package com.algoTrader.service;

import com.algoTrader.entity.trade.SimpleOrder;
import com.algoTrader.enumeration.MarketChannel;

public abstract class ExternalOrderServiceImpl extends ExternalOrderServiceBase {

    @Override
    protected abstract void handleSendOrder(SimpleOrder order) throws Exception;

    @Override
    protected abstract void handleValidateOrder(SimpleOrder order) throws Exception;

    @Override
    protected abstract void handleCancelOrder(SimpleOrder order) throws Exception;

    @Override
    protected abstract void handleModifyOrder(SimpleOrder order) throws Exception;

    @Override
    protected abstract MarketChannel handleGetMarketChannel() throws Exception;
}
