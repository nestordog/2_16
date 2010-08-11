package com.algoTrader.entity;

import java.util.List;

public class StockOptionDaoImpl extends StockOptionDaoBase {

    private List<StockOption> stockOptionsOnWatchlist; // cache stockOptionsOnWatchlist because it gets called very often

    @SuppressWarnings("unchecked")
    public List<StockOption> handleGetStockOptionsOnWatchlist(boolean forceReload) {

        if (stockOptionsOnWatchlist == null || forceReload) {
            stockOptionsOnWatchlist = super.findStockOptionsOnWatchlist();
        }

        return stockOptionsOnWatchlist;
    }
}
