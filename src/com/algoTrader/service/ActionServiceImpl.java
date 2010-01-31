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
        startTimeTheMarket();
    }

    protected void handleExpireStockOptions() throws java.lang.Exception {

        logger.info("expireStockOptions event");
        getStockOptionService().expireStockOptions();
        startTimeTheMarket();
    }

    protected void handleTimeTheMarket(int securityId,  BigDecimal spot) throws Exception {

        logger.info("timeTheMarket event");
        StockOption stockOption = getStockOptionService().putOnWatchlist(securityId, spot);

        if (!simulation) getTickService().start(stockOption);

        getRuleService().activate("openPosition", new String[] { String.valueOf(stockOption.getId())});
        getRuleService().deactivate("timeTheMarket");
    }


    private void startTimeTheMarket() throws java.lang.Exception {

        if (!getRuleService().isActive("timeTheMarket") && !getRuleService().isActive("openPosition")) {
            StockOption stockOption = getStockOptionService().defaultPutOnWatchlist();

            if (stockOption != null) {

                if (!simulation) getTickService().start(stockOption);

                getRuleService().activate("timeTheMarket", new String[] { String.valueOf(stockOption.getId())});
            }
        }
    }

    protected void handleOpenPosition(int securityId, BigDecimal settlement, BigDecimal currentValue, BigDecimal underlaying) throws Exception {

        logger.info("openPosition event");
        getStockOptionService().openPosition(securityId, settlement, currentValue, underlaying);
        getRuleService().deactivate("openPosition");
    }
}
