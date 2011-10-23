package com.algoTrader.service;

import com.algoTrader.enumeration.MarketChannel;

public class DispatcherServiceImpl extends DispatcherServiceBase {

    @Override
    protected StockOptionRetrieverService handleGetStockOptionRetrieverService(MarketChannel marketChannel) {

        if (MarketChannel.SQ.getValue().equals(marketChannel)) {
            return getSQStockOptionRetrieverService();
        } else if (MarketChannel.IB.getValue().equals(marketChannel)) {
            return getIBStockOptionRetrieverService();
        } else {
            throw new UnsupportedOperationException("market Channel " + marketChannel + " does not exist");
        }
    }

    @Override
    protected OrderService handleGetOrderService(MarketChannel marketChannel) {

        if (MarketChannel.IB.getValue().equals(marketChannel)) {
            return getIBOrderService();
        } else {
            throw new UnsupportedOperationException("market Channel " + marketChannel + " does not exist");
        }
    }

    @Override
    protected MarketDataService handleGetMarketDataService(MarketChannel marketChannel) {

        if (MarketChannel.IB.getValue().equals(marketChannel)) {
            return getIBMarketDataService();
        } else {
            throw new UnsupportedOperationException("market Channel " + marketChannel + " does not exist");
        }
    }

    @Override
    protected AccountService handleGetAccountService(MarketChannel marketChannel) {

        if (MarketChannel.IB.getValue().equals(marketChannel)) {
            return getIBAccountService();
        } else {
            throw new UnsupportedOperationException("market Channel " + marketChannel + " does not exist");
        }
    }

    @Override
    protected HistoricalDataService handleGetHistoricalDataService(MarketChannel marketChannel) throws Exception {

        if (MarketChannel.IB.getValue().equals(marketChannel)) {
            return getIBHistoricalDataService();
        } else {
            throw new UnsupportedOperationException("market Channel " + marketChannel + " does not exist");
        }
    }
}
