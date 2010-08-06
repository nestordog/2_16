package com.algoTrader.service;

import org.apache.log4j.Logger;

import com.algoTrader.entity.StockOption;
import com.algoTrader.util.MyLogger;

public class WatchlistServiceImpl extends WatchlistServiceBase {

    private static Logger logger = MyLogger.getLogger(WatchlistServiceImpl.class.getName());

    @SuppressWarnings("unchecked")
    protected void handlePutOnWatchlist(StockOption stockOption) throws Exception {

        if (!stockOption.isOnWatchlist()) {
            stockOption.setOnWatchlist(true);
            getStockOptionDao().update(stockOption);
            getStockOptionDao().getStockOptionsOnWatchlist(false).add(stockOption);

            logger.info("put stockOption on watchlist " + stockOption.getSymbol());
        }
    }

    protected void handleRemoveFromWatchlist(StockOption stockOption) throws Exception {

        if (stockOption.isOnWatchlist()) {
            stockOption.setOnWatchlist(false);
            getStockOptionDao().update(stockOption);
            getStockOptionDao().getStockOptionsOnWatchlist(false).remove(stockOption);

            logger.info("removed stockOption from watchlist " + stockOption.getSymbol());
        }
    }

    protected void handlePutOnWatchlist(int stockOptionId) throws Exception {

        StockOption stockOption = (StockOption)getStockOptionDao().load(stockOptionId);
        putOnWatchlist(stockOption);
    }

    protected void handleRemoveFromWatchlist(int stockOptionId) throws Exception {

        StockOption stockOption = (StockOption)getStockOptionDao().load(stockOptionId);
        removeFromWatchlist(stockOption);
    }
}
