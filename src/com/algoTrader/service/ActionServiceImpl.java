package com.algoTrader.service;

import java.math.BigDecimal;

import com.algoTrader.entity.Position;
import com.algoTrader.entity.StockOption;
import com.algoTrader.util.PropertiesUtil;

public class ActionServiceImpl extends ActionServiceBase {

    private static boolean simulation = new Boolean(PropertiesUtil.getProperty("simulation")).booleanValue();

    protected void handleSetExitValue(Position position, BigDecimal exitValue) throws java.lang.Exception {

        getStockOptionService().setExitValue(position, exitValue);
    }

    protected void handleSetMargins() throws java.lang.Exception {

        getStockOptionService().setMargins();
    }

    protected void handleClosePosition(Position position) throws java.lang.Exception {

        getTransactionService().closePosition(position);
        startMarketTiming();
    }

    protected void handleExpireStockOptions() throws java.lang.Exception {

        getStockOptionService().expireStockOptions();
        startMarketTiming();
    }

    protected void handleStartMarketTiming() throws java.lang.Exception {

        StockOption stockOption = getStockOptionService().putOnWatchlist();

        if (stockOption == null) return;

        if (!simulation) getTickService().start(stockOption);

        getRuleService().deactivate("marketTiming");
        getRuleService().activate("marketTiming", new String[] { String.valueOf(stockOption.getId())});
    }

    protected void handlePutOnWatchlist() throws java.lang.Exception {

        getStockOptionService().putOnWatchlist();
    }

    protected void handleOpenPosition() throws java.lang.Exception {

        //TODO implement open position
    }
}
