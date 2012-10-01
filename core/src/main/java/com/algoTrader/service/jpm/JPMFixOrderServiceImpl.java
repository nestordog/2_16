package com.algoTrader.service.jpm;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;

import quickfix.field.Account;
import quickfix.field.ExDestination;
import quickfix.field.HandlInst;
import quickfix.field.TransactTime;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelReplaceRequest;
import quickfix.fix42.OrderCancelRequest;

import com.algoTrader.entity.trade.SimpleOrder;
import com.algoTrader.enumeration.Market;
import com.algoTrader.enumeration.MarketChannel;

public class JPMFixOrderServiceImpl extends JPMFixOrderServiceBase {

    private static final long serialVersionUID = -8881034489922372443L;

    private @Value("${jpm.account}") String account;

    @Override
    public void handleInit() throws Exception {

        getFixClient().createSession(getMarketChannel());
    }

    @Override
    protected void handleSendOrder(SimpleOrder order, NewOrderSingle newOrder) {

        newOrder.set(new Account(this.account));
        newOrder.set(new HandlInst('1'));
        newOrder.set(new TransactTime(new Date()));
    }

    @Override
    protected void handleModifyOrder(SimpleOrder order, OrderCancelReplaceRequest replaceRequest) {

        replaceRequest.set(new Account(this.account));
        replaceRequest.set(new HandlInst('1'));
        replaceRequest.set(new TransactTime(new Date()));
    }

    @Override
    protected void handleCancelOrder(SimpleOrder order, OrderCancelRequest cancelRequest) {

        cancelRequest.set(new Account(this.account));
        cancelRequest.set(new TransactTime(new Date()));
    }

    @Override
    protected MarketChannel handleGetMarketChannel() {

        return MarketChannel.JPM_FIX;
    }

    @Override
    protected ExDestination handleGetExDestination(Market market) throws Exception {

        if (Market.CBOE.equals(market)) {
            return new ExDestination("XCBO");
        } else if (Market.CFE.equals(market)) {
            return new ExDestination("XCBF");
        } else {
            throw new UnsupportedOperationException("market not supported " + market);
        }
    }
}
