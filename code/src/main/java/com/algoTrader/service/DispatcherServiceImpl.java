package com.algoTrader.service;

import com.algoTrader.enumeration.MarketChannel;
import com.algoTrader.util.ConfigurationUtil;

public class DispatcherServiceImpl extends DispatcherServiceBase {

    private static String marketChannel = ConfigurationUtil.getBaseConfig().getString("marketChannel");

    protected StockOptionRetrieverService handleGetStockOptionRetrieverService() {

        if (MarketChannel.SQ.getValue().equals(marketChannel)) {
            return getSqStockOptionRetrieverService();
        } else if (MarketChannel.IB.getValue().equals(marketChannel)) {
            return getIbStockOptionRetrieverService();
        } else {
            throw new UnsupportedOperationException("market Channel " + marketChannel + " does not exist");
        }
    }

    protected TransactionService handleGetTransactionService() {

        if (MarketChannel.SQ.getValue().equals(marketChannel)) {
            return getSqTransactionService();
        } else if (MarketChannel.IB.getValue().equals(marketChannel)) {
            return getIbTransactionService();
        } else {
            throw new UnsupportedOperationException("market Channel " + marketChannel + " does not exist");
        }
    }

    protected TickService handleGetTickService() {

        if (MarketChannel.SQ.getValue().equals(marketChannel)) {
            return getSqTickService();
        } else if (MarketChannel.IB.getValue().equals(marketChannel)) {
            return getIbTickService();
        } else {
            throw new UnsupportedOperationException("market Channel " + marketChannel + " does not exist");
        }
    }

    protected AccountService handleGetAccountService() {

        if (MarketChannel.SQ.getValue().equals(marketChannel)) {
            return getSqAccountService();
        } else if (MarketChannel.IB.getValue().equals(marketChannel)) {
            return getIbAccountService();
        } else {
            throw new UnsupportedOperationException("market Channel " + marketChannel + " does not exist");
        }
    }

    protected HistoricalDataService handleGetHistoricalDataService() throws Exception {

        if (MarketChannel.SQ.getValue().equals(marketChannel)) {
            return getSqHistoricalDataService();
        } else if (MarketChannel.IB.getValue().equals(marketChannel)) {
            return getIbHistoricalDataService();
        } else {
            throw new UnsupportedOperationException("market Channel " + marketChannel + " does not exist");
        }
    }
}
