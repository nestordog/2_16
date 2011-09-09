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
    protected SyncOrderService handleGetTransactionService(MarketChannel marketChannel) {

        if (MarketChannel.SQ.getValue().equals(marketChannel)) {
            return getSQSyncOrderService();
        } else if (MarketChannel.IB.getValue().equals(marketChannel)) {
            return getIBSyncOrderService();
        } else {
            throw new UnsupportedOperationException("market Channel " + marketChannel + " does not exist");
        }
    }

    @Override
    protected SyncMarketDataService handleGetMarketDataService(MarketChannel marketChannel) {

        if (MarketChannel.SQ.getValue().equals(marketChannel)) {
            return getSQSyncMarketDataService();
        } else if (MarketChannel.IB.getValue().equals(marketChannel)) {
            return getIBSyncMarketDataService();
        } else {
            throw new UnsupportedOperationException("market Channel " + marketChannel + " does not exist");
        }
    }

    @Override
    protected AccountService handleGetAccountService(MarketChannel marketChannel) {

        if (MarketChannel.SQ.getValue().equals(marketChannel)) {
            return getSQAccountService();
        } else if (MarketChannel.IB.getValue().equals(marketChannel)) {
            return getIBAccountService();
        } else {
            throw new UnsupportedOperationException("market Channel " + marketChannel + " does not exist");
        }
    }

    @Override
    protected HistoricalDataService handleGetHistoricalDataService(MarketChannel marketChannel) throws Exception {

        if (MarketChannel.SQ.getValue().equals(marketChannel)) {
            return getSQHistoricalDataService();
        } else if (MarketChannel.IB.getValue().equals(marketChannel)) {
            return getIBHistoricalDataService();
        } else {
            throw new UnsupportedOperationException("market Channel " + marketChannel + " does not exist");
        }
    }
}
