package com.algoTrader.esper;

import java.util.List;

import com.algoTrader.entity.trade.OrderStatus;

public class FullExecutionTradeCallback extends TradeCallback {

    public FullExecutionTradeCallback() {
        super(true);
    }

    @Override
    public void onTradeCompleted(List<OrderStatus> orderStatus) throws Exception {
        // do nothing
    }
}
