package com.algoTrader.service;

import java.math.BigDecimal;

import org.apache.log4j.Logger;

import com.algoTrader.entity.Position;
import com.algoTrader.entity.StockOption;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.enumeration.RuleName;
import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.EsperService;
import com.algoTrader.util.MyLogger;

public class ActionServiceImpl extends ActionServiceBase {

    private static boolean simulation = ConfigurationUtil.getBaseConfig().getBoolean("simulation");
    private static long firstBuyTime = ConfigurationUtil.getBaseConfig().getLong("simulation.firstBuyTime");
    private static long lastBuyTime = ConfigurationUtil.getBaseConfig().getLong("simulation.lastBuyTime");

    private static Logger logger = MyLogger.getLogger(ActionServiceImpl.class.getName());

    protected void handleRetrieveTicks() {

        if (simulation) return; // unfortunately timer.at pattern get's executed in simulation

        long startTime = System.currentTimeMillis();
        logger.debug("retrieveTicks start");

        getDispatcherService().getTickService().processSecuritiesOnWatchlist();

        logger.debug("retrieveTicks end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
    }

    protected void handleSetExitValue(int positionId, double exitValue) throws java.lang.Exception {

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

    protected void handleExpirePosition(int positionId, int underlayingId, BigDecimal underlayingSpot) throws java.lang.Exception {

        long startTime = System.currentTimeMillis();
        logger.debug("expireStockOptions start");

        getStockOptionService().expirePosition(positionId);

        logger.debug("expireStockOptions end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
    }

    protected void handleBuySignal(int underlayingId,  BigDecimal underlayingSpot) throws Exception {

        if (firstBuyTime != 0 && EsperService.getCurrentTime() < firstBuyTime) {
            logger.debug("ignoring signal, because we are before firstBuyTime");
            return;
        }

        if (lastBuyTime != 0 && EsperService.getCurrentTime() > lastBuyTime) {
            logger.debug("ignoring signal, because we are after lastBuyTime");
            return;
        }

        long startTime = System.currentTimeMillis();
        logger.debug("buySignal start");

        if (!getRuleService().isActive(RuleName.OPEN_POSITION)) {
            StockOption stockOption = getStockOptionService().getStockOption(underlayingId, underlayingSpot, OptionType.PUT);
            getDispatcherService().getTickService().putOnWatchlist(stockOption);
            getRuleService().activate(RuleName.OPEN_POSITION, stockOption);
        }

        logger.debug("buySignal end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
    }

    protected void handleSellSignal(int underlayingId,  BigDecimal underlayingSpot) throws Exception {

        if (firstBuyTime != 0 && EsperService.getCurrentTime() < firstBuyTime) {
            logger.debug("ignoring signal, because we are before firstBuyTime");
            return;
        }

        if (lastBuyTime != 0 && EsperService.getCurrentTime() > lastBuyTime) {
            logger.debug("ignoring signal, because we are after lastBuyTime");
            return;
        }

        long startTime = System.currentTimeMillis();
        logger.debug("sellSignal start");

        if (!getRuleService().isActive(RuleName.OPEN_POSITION)) {
            StockOption stockOption = getStockOptionService().getStockOption(underlayingId, underlayingSpot, OptionType.CALL);
            getDispatcherService().getTickService().putOnWatchlist(stockOption);
            getRuleService().activate(RuleName.OPEN_POSITION, stockOption);
        }

        logger.debug("sellSignal end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
    }

    protected void handleOpenPosition(int securityId, BigDecimal currentValue, BigDecimal underlayingSpot, double volatility, BigDecimal stockOptionSettlement, BigDecimal underlayingSettlement)
            throws Exception {

        long startTime = System.currentTimeMillis();
        logger.debug("openPosition start");

        getStockOptionService().openPosition(securityId, currentValue, underlayingSpot, volatility, stockOptionSettlement, underlayingSettlement);
        getRuleService().deactivate(RuleName.OPEN_POSITION);

        logger.debug("openPosition end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
    }

    protected void handleRollPosition(int positionId, int underlayingId, BigDecimal underlayingSpot) {

        long startTime = System.currentTimeMillis();
        logger.debug("rollPosition start");

        getStockOptionService().closePosition(positionId);

        if (!getRuleService().isActive(RuleName.OPEN_POSITION)) {
            Position position = getLookupService().getPosition(positionId);
            StockOption oldStockOption = (StockOption)position.getSecurity();

            StockOption stockOption = getStockOptionService().getStockOption(underlayingId, underlayingSpot, oldStockOption.getType());
            getDispatcherService().getTickService().putOnWatchlist(stockOption);
            getRuleService().activate(RuleName.OPEN_POSITION, stockOption);
        }

        logger.debug("rollPosition end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
    }

    protected void handleRunDailyJobs() {

        setMargins();

        getDispatcherService().getAccountService().processCashTransactions();
    }
}
