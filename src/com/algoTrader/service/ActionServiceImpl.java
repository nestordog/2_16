package com.algoTrader.service;

import java.math.BigDecimal;

import org.apache.log4j.Logger;

import com.algoTrader.entity.StockOption;
import com.algoTrader.enumeration.RuleName;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.PropertiesUtil;

public class ActionServiceImpl extends ActionServiceBase {

    private static boolean simulation = new Boolean(PropertiesUtil.getProperty("simulation")).booleanValue();

    private static Logger logger = MyLogger.getLogger(ActionServiceImpl.class.getName());

    protected void handleRetrieveTicks() {

        if (simulation) return; // unfortunately timer.at pattern get's executed in simulation

        logger.debug("retrieveTicks event");
        getTickService().processSecuritiesOnWatchlist();
    }

    protected void handleSetExitValue(int positionId, BigDecimal exitValue) throws java.lang.Exception {

        logger.debug("setExitValue event");
        getStockOptionService().setExitValue(positionId, exitValue);
    }

    protected void handleSetMargins() throws java.lang.Exception {

        logger.debug("setMargins event");
        getStockOptionService().setMargins();
    }

    protected void handleClosePosition(int positionId) throws java.lang.Exception {

        logger.debug("closePosition event");
        getStockOptionService().closePosition(positionId);
    }

    protected void handleExpireStockOption(int positionId) throws java.lang.Exception {

        logger.debug("expireStockOptions event");
        getStockOptionService().expireStockOption(positionId);
    }

    protected void handleStartTimeTheMarket(int underlayingId, BigDecimal spot) throws java.lang.Exception {

        logger.debug("startTimeTheMarket event");
        if (!getRuleService().isActive(RuleName.TIME_THE_MARKET) && !getRuleService().isActive(RuleName.OPEN_POSITION)) {

            StockOption stockOption = getStockOptionService().getStockOption(underlayingId, spot);

            if (stockOption != null) {
                getWatchlistService().putOnWatchlist(stockOption);
                getRuleService().activate(RuleName.TIME_THE_MARKET, stockOption);
            }
        }
    }

    protected void handleTimeTheMarket(int stockOptionId, int underlayingId,  BigDecimal spot) throws Exception {

        logger.debug("timeTheMarket event");
        StockOption newStockOption = getStockOptionService().getStockOption(underlayingId, spot);

        // if we got a different stockOption, remove the old one from the watchlist
        if (newStockOption.getId() != stockOptionId) {
            getWatchlistService().putOnWatchlist(newStockOption);
            getWatchlistService().removeFromWatchlist(stockOptionId);
        }

        getRuleService().activate(RuleName.OPEN_POSITION, newStockOption);
        getRuleService().deactivate(RuleName.TIME_THE_MARKET);
    }


    protected void handleOpenPosition(int securityId, BigDecimal settlement, BigDecimal currentValue, BigDecimal underlaying) throws Exception {

        logger.debug("openPosition event");
        getStockOptionService().openPosition(securityId, settlement, currentValue, underlaying);
        getRuleService().deactivate(RuleName.OPEN_POSITION);
    }
}
