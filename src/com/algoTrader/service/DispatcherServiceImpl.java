package com.algoTrader.service;

import com.algoTrader.enumeration.MarketChannel;
import com.algoTrader.util.PropertiesUtil;

public class DispatcherServiceImpl extends com.algoTrader.service.DispatcherServiceBase {

    private static String marketChannel = PropertiesUtil.getProperty("marketChannel");

    protected StockOptionRetrieverService handleGetStockOptionRetrieverService() {

        if (MarketChannel.SWISSQUOTE.getValue().equals(marketChannel)) {
            return getSwissquoteStockOptionRetrieverService();
        } else {
            throw new UnsupportedOperationException("market Channel " + marketChannel + " does not exist");
        }
    }

    protected TransactionService handleGetTransactionService() {

        if (MarketChannel.SWISSQUOTE.getValue().equals(marketChannel)) {
            return getSwissquoteTransactionService();
        } else {
            throw new UnsupportedOperationException("market Channel " + marketChannel + " does not exist");
        }
    }

    protected TickService handleGetTickService() {

        if (MarketChannel.SWISSQUOTE.getValue().equals(marketChannel)) {
            return getSwissquoteTickService();
        } else {
            throw new UnsupportedOperationException("market Channel " + marketChannel + " does not exist");
        }
    }
}
