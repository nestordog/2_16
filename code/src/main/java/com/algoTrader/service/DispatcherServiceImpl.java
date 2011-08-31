package com.algoTrader.service;

import com.algoTrader.enumeration.MarketChannel;

public class DispatcherServiceImpl extends DispatcherServiceBase {

    @Override
    protected StockOptionRetrieverService handleGetStockOptionRetrieverService(MarketChannel marketChannel) {

        if (MarketChannel.SQ.getValue().equals(marketChannel)) {
            return getSqStockOptionRetrieverService();
        } else if (MarketChannel.IB.getValue().equals(marketChannel)) {
            return getIbStockOptionRetrieverService();
        } else {
            throw new UnsupportedOperationException("market Channel " + marketChannel + " does not exist");
        }
    }

    @Override
    protected SyncOrderService handleGetTransactionService(MarketChannel marketChannel) {

        if (MarketChannel.SQ.getValue().equals(marketChannel)) {
            return getSqSyncOrderService();
        } else if (MarketChannel.IB.getValue().equals(marketChannel)) {
            return getIbSyncOrderService();
        } else {
            throw new UnsupportedOperationException("market Channel " + marketChannel + " does not exist");
        }
    }

    @Override
    protected SyncMarketDataService handleGetMarketDataService(MarketChannel marketChannel) {

        if (MarketChannel.SQ.getValue().equals(marketChannel)) {
            return getSqSyncMarketDataService();
        } else if (MarketChannel.IB.getValue().equals(marketChannel)) {
            return getIbSyncMarketDataService();
        } else {
            throw new UnsupportedOperationException("market Channel " + marketChannel + " does not exist");
        }
    }

    @Override
    protected AccountService handleGetAccountService(MarketChannel marketChannel) {

        if (MarketChannel.SQ.getValue().equals(marketChannel)) {
            return getSqAccountService();
        } else if (MarketChannel.IB.getValue().equals(marketChannel)) {
            return getIbAccountService();
        } else {
            throw new UnsupportedOperationException("market Channel " + marketChannel + " does not exist");
        }
    }

    @Override
    protected HistoricalDataService handleGetHistoricalDataService(MarketChannel marketChannel) throws Exception {

        if (MarketChannel.SQ.getValue().equals(marketChannel)) {
            return getSqHistoricalDataService();
        } else if (MarketChannel.IB.getValue().equals(marketChannel)) {
            return getIbHistoricalDataService();
        } else {
            throw new UnsupportedOperationException("market Channel " + marketChannel + " does not exist");
        }
    }
}
