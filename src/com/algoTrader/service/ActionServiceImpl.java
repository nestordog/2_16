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

        long startTime = System.currentTimeMillis();
        logger.debug("retrieveTicks start");
        getTickService().processSecuritiesOnWatchlist();
        logger.debug("retrieveTicks end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
    }

    protected void handleSetExitValue(int positionId, BigDecimal exitValue) throws java.lang.Exception {

        long startTime = System.currentTimeMillis();
        logger.debug("setExitValue start");
        getStockOptionService().setExitValue(positionId, exitValue);
        logger.debug("setExitValue end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
    }

    protected void handleSetMargins() throws java.lang.Exception {

        long startTime = System.currentTimeMillis();
        logger.debug("setMargins start");
        getStockOptionService().setMargins();
        logger.debug("setMargins end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
    }

    protected void handleClosePosition(int positionId) throws java.lang.Exception {

        long startTime = System.currentTimeMillis();
        logger.debug("closePosition start");
        getStockOptionService().closePosition(positionId);
        logger.debug("closePosition end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
    }

    protected void handleExpireStockOption(int positionId) throws java.lang.Exception {

        long startTime = System.currentTimeMillis();
        logger.debug("expireStockOptions start");
        getStockOptionService().expireStockOption(positionId);
        logger.debug("expireStockOptions end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
    }

    protected void handleStartTimeTheMarket(int underlayingId, BigDecimal spot) throws java.lang.Exception {

        long startTime = System.currentTimeMillis();
        logger.debug("startTimeTheMarket start");
        if (!getRuleService().isActive(RuleName.TIME_THE_MARKET) && !getRuleService().isActive(RuleName.OPEN_POSITION)) {

            StockOption stockOption = getStockOptionService().getStockOption(underlayingId, spot);

            if (stockOption != null) {
                getWatchlistService().putOnWatchlist(stockOption);
                getRuleService().activate(RuleName.TIME_THE_MARKET, stockOption);
            }
        }
        logger.debug("startTimeTheMarket end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
    }

    protected void handleTimeTheMarket(int stockOptionId, int underlayingId,  BigDecimal spot) throws Exception {

        long startTime = System.currentTimeMillis();
        logger.debug("timeTheMarket start");
        StockOption newStockOption = getStockOptionService().getStockOption(underlayingId, spot);

        // if we got a different stockOption, remove the old one from the watchlist
        if (newStockOption.getId() != stockOptionId) {
            getWatchlistService().putOnWatchlist(newStockOption);
            getWatchlistService().removeFromWatchlist(stockOptionId);
        }

        getRuleService().activate(RuleName.OPEN_POSITION, newStockOption);
        getRuleService().deactivate(RuleName.TIME_THE_MARKET);
        logger.debug("timeTheMarket end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
    }


    protected void handleOpenPosition(int securityId, BigDecimal settlement, BigDecimal currentValue, BigDecimal underlaying) throws Exception {

        long startTime = System.currentTimeMillis();
        logger.debug("openPosition start");
        getStockOptionService().openPosition(securityId, settlement, currentValue, underlaying);
        getRuleService().deactivate(RuleName.OPEN_POSITION);
        logger.debug("openPosition end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
    }
}
