package com.algoTrader.service;

import com.algoTrader.entity.trade.SimpleOrder;

public abstract class ExternalOrderServiceImpl extends ExternalOrderServiceBase {

    protected abstract void handleSendOrder(SimpleOrder order) throws Exception;

    protected abstract void handleValidateOrder(SimpleOrder order) throws Exception;

    protected abstract void handleCancelOrder(SimpleOrder order) throws Exception;

    protected abstract void handleModifyOrder(SimpleOrder order) throws Exception;
}
