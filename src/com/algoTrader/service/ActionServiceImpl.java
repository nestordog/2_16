package com.algoTrader.service;

import java.math.BigDecimal;

import org.apache.log4j.Logger;

import com.algoTrader.entity.StockOption;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.PropertiesUtil;

public class ActionServiceImpl extends ActionServiceBase {

    private static boolean simulation = new Boolean(PropertiesUtil.getProperty("simulation")).booleanValue();
    private static Logger logger = MyLogger.getLogger(ActionServiceImpl.class.getName());

    protected void handleSetExitValue(int positionId, BigDecimal exitValue) throws java.lang.Exception {

        logger.info("setExitValue event");
        getStockOptionService().setExitValue(positionId, exitValue);
    }

    protected void handleSetMargins() throws java.lang.Exception {

        logger.info("setMargins event");
        getStockOptionService().setMargins();
    }

    protected void handleClosePosition(int positionId) throws java.lang.Exception {

        logger.info("closePosition event");
        getStockOptionService().closePosition(positionId);
    }

    protected void handleExpireStockOption(int positionId) throws java.lang.Exception {

        logger.info("expireStockOptions event");
        getStockOptionService().expireStockOption(positionId);
    }

    protected void handleStartTimeTheMarket(int underlayingId, BigDecimal spot) throws java.lang.Exception {

        logger.info("startTimeTheMarket event");
        if (!getRuleService().isActive("timeTheMarket") && !getRuleService().isActive("openPosition")) {

            StockOption stockOption = getStockOptionService().putOnWatchlist(underlayingId, spot);

            if (stockOption != null) {

                if (!simulation) getTickService().start(stockOption);

                getRuleService().activate("timeTheMarket", new String[] { String.valueOf(stockOption.getId())});
            }
        }
    }

    protected void handleTimeTheMarket(int stockOptionId, int underlayingId,  BigDecimal spot) throws Exception {

        logger.info("timeTheMarket event");
        StockOption newStockOption = getStockOptionService().putOnWatchlist(underlayingId, spot);

        if (newStockOption.getId() != stockOptionId) {

            // if we got a different stockOption, remove the old one from the watchlist
            getStockOptionService().removeFromWatchlist(stockOptionId);
        }

        if (!simulation) getTickService().start(newStockOption);

        getRuleService().activate("openPosition", new String[] { String.valueOf(newStockOption.getId())});
        getRuleService().deactivate("timeTheMarket");
    }


    protected void handleOpenPosition(int securityId, BigDecimal settlement, BigDecimal currentValue, BigDecimal underlaying) throws Exception {

        logger.info("openPosition event");
        getStockOptionService().openPosition(securityId, settlement, currentValue, underlaying);
        getRuleService().deactivate("openPosition");
    }
}
