package com.algoTrader.entity;

import java.util.List;

public class StockOptionDaoImpl extends StockOptionDaoBase {

    private List<StockOption> stockOptionsOnWatchlist; // cache stockOptionsOnWatchlist because it gets called very often

    @SuppressWarnings("unchecked")
    public List<StockOption> handleGetStockOptionsOnWatchlist(boolean forceReload) {

        if (this.stockOptionsOnWatchlist == null || forceReload) {
            this.stockOptionsOnWatchlist = super.findStockOptionsOnWatchlist();
        }

        return this.stockOptionsOnWatchlist;
    }
}
