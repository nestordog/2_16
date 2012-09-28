package com.algoTrader.service.ib;

import quickfix.field.CustomerOrFirm;
import quickfix.field.ExDestination;
import quickfix.field.HandlInst;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelReplaceRequest;
import quickfix.fix42.OrderCancelRequest;

import com.algoTrader.entity.trade.SimpleOrder;
import com.algoTrader.enumeration.Market;
import com.algoTrader.enumeration.MarketChannel;

public class IBFixOrderServiceImpl extends IBFixOrderServiceBase {

    @Override
    protected void handleSendOrder(SimpleOrder order, NewOrderSingle newOrder) {

        newOrder.set(new HandlInst('2'));
        newOrder.set(new CustomerOrFirm(0));
    }

    @Override
    protected void handleModifyOrder(SimpleOrder order, OrderCancelReplaceRequest replaceRequest) {

        replaceRequest.set(new HandlInst('2'));
        replaceRequest.set(new CustomerOrFirm(0));
    }

    @Override
    protected void handleCancelOrder(SimpleOrder order, OrderCancelRequest cancelRequest) {

        // do nothing
    }

    @Override
    protected MarketChannel handleGetMarketChannel() {

        return MarketChannel.IB_FIX;
    }

    @Override
    protected ExDestination handleGetExDestination(Market market) throws Exception {

        return new ExDestination(market.toString());
    }
}
