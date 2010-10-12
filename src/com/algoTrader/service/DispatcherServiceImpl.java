package com.algoTrader.service;

import java.util.HashSet;
import java.util.Set;

import com.algoTrader.enumeration.MarketChannel;
import com.algoTrader.service.ib.IbService;
import com.algoTrader.util.PropertiesUtil;

public class DispatcherServiceImpl extends com.algoTrader.service.DispatcherServiceBase {

    private static String marketChannel = PropertiesUtil.getProperty("marketChannel");

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

    protected Set<IbService> handleGetAllIbServices() {

        Set<IbService> ibServices = new HashSet<IbService>();
        ibServices.add(getIbTickService());
        ibServices.add(getIbTransactionService());
        ibServices.add(getIbAccountService());
        ibServices.add(getIbStockOptionRetrieverService());

        return ibServices;
    }
}
