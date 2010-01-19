package com.algoTrader.service;

import java.math.BigDecimal;

import com.algoTrader.entity.StockOption;
import com.algoTrader.util.PropertiesUtil;

public class ActionServiceImpl extends ActionServiceBase {

    private static boolean simulation = new Boolean(PropertiesUtil.getProperty("simulation")).booleanValue();

    protected void handleSetExitValue(int positionId, BigDecimal exitValue) throws java.lang.Exception {

        getStockOptionService().setExitValue(positionId, exitValue);
    }

    protected void handleSetMargins() throws java.lang.Exception {

        getStockOptionService().setMargins();
    }

    protected void handleClosePosition(int positionId) throws java.lang.Exception {

        getTransactionService().closePosition(positionId);
        startTimeTheMarket();
    }

    protected void handleExpireStockOptions() throws java.lang.Exception {

        getStockOptionService().expireStockOptions();
        startTimeTheMarket();
    }

    protected void handleTimeTheMarket(int securityId,  BigDecimal spot) throws Exception {

        StockOption stockOption = getStockOptionService().putOnWatchlist(securityId, spot);

        if (!simulation) getTickService().start(stockOption);

        getRuleService().activate("openPosition", new String[] { String.valueOf(stockOption.getId())});

    }

    protected void handleOpenPosition(int securityId, BigDecimal settlement, BigDecimal currentValue, BigDecimal underlaying) throws Exception {

        getTransactionService().openPosition(securityId, settlement, currentValue, underlaying);
    }

    private void startTimeTheMarket() throws java.lang.Exception {

        if (!getRuleService().isActive("timeTheMarket")) {
            StockOption stockOption = getStockOptionService().defaultPutOnWatchlist();

            if (stockOption != null) {

                if (!simulation) getTickService().start(stockOption);

                getRuleService().activate("timeTheMarket", new String[] { String.valueOf(stockOption.getId())});
            }
        }
    }
}
