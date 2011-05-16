package com.algoTrader.service;

import com.algoTrader.enumeration.MarketChannel;

public class DispatcherServiceImpl extends DispatcherServiceBase {

    protected StockOptionRetrieverService handleGetStockOptionRetrieverService(MarketChannel marketChannel) {

        if (MarketChannel.SQ.getValue().equals(marketChannel)) {
            return getSqStockOptionRetrieverService();
        } else if (MarketChannel.IB.getValue().equals(marketChannel)) {
            return getIbStockOptionRetrieverService();
        } else {
            throw new UnsupportedOperationException("market Channel " + marketChannel + " does not exist");
        }
    }

    protected TransactionService handleGetTransactionService(MarketChannel marketChannel) {

        if (MarketChannel.SQ.getValue().equals(marketChannel)) {
            return getSqTransactionService();
        } else if (MarketChannel.IB.getValue().equals(marketChannel)) {
            return getIbTransactionService();
        } else {
            throw new UnsupportedOperationException("market Channel " + marketChannel + " does not exist");
        }
    }

    protected MarketDataService handleGetMarketDataService(MarketChannel marketChannel) {

        if (MarketChannel.SQ.getValue().equals(marketChannel)) {
            return getSqMarketDataService();
        } else if (MarketChannel.IB.getValue().equals(marketChannel)) {
            return getIbMarketDataService();
        } else {
            throw new UnsupportedOperationException("market Channel " + marketChannel + " does not exist");
        }
    }

    protected AccountService handleGetAccountService(MarketChannel marketChannel) {

        if (MarketChannel.SQ.getValue().equals(marketChannel)) {
            return getSqAccountService();
        } else if (MarketChannel.IB.getValue().equals(marketChannel)) {
            return getIbAccountService();
        } else {
            throw new UnsupportedOperationException("market Channel " + marketChannel + " does not exist");
        }
    }

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
