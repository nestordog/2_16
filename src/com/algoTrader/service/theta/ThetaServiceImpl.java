package com.algoTrader.service.theta;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.supercsv.exception.SuperCSVException;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.SecurityFamily;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.Tick;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.stockOption.StockOptionUtil;
import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.vo.OrderVO;

public class ThetaServiceImpl extends ThetaServiceBase {

    private static boolean simulation = ConfigurationUtil.getBaseConfig().getBoolean("simulation");
    private static long firstBuyTime = ConfigurationUtil.getBaseConfig().getLong("simulation.firstBuyTime");
    private static long lastBuyTime = ConfigurationUtil.getBaseConfig().getLong("simulation.lastBuyTime");
    private static double initialMarginMarkup = ConfigurationUtil.getBaseConfig().getDouble("initialMarginMarkup");

    private static Logger logger = MyLogger.getLogger(ThetaServiceImpl.class.getName());

    private long FORTY_FIVE_DAYS = 3888000000l;

    @SuppressWarnings("unchecked")
    protected void handlePrefeedTicks(String strategyName) throws SuperCSVException, IOException {

        Security underlaying = getLookupService().getSecurityByStrategyName(strategyName);

        Configuration config = ConfigurationUtil.getStrategyConfig(strategyName);
        double kFastDays = Math.max(config.getDouble("callKFastDays"), config.getDouble("putKFastDays"));
        double kSlowDays = Math.max(config.getDouble("callKSlowDays"), config.getDouble("putKSlowDays"));
        double dSlowDays = Math.max(config.getDouble("callDSlowDays"), config.getDouble("putDSlowDays"));
        int numberOfTicks = (int) Math.ceil((kFastDays + kSlowDays + dSlowDays) * config.getLong("simulation.eventsPerDay"));

        // we need to get 2 x numberOfTicks so that KEEP_STOCHASTIC_VO has the
        // same number of ticks we need to initialize stochastic
        Collection<Tick> ticks = getLookupService().getPreFeedTicks(underlaying.getId(), 2 * numberOfTicks);

        getRuleService().initCoordination(strategyName);
        getRuleService().coordinate(strategyName, ticks, "dateTime");
        getRuleService().startCoordination(strategyName);
    }

    @SuppressWarnings("unchecked")
    protected void handleSetTrend(String strategyName, boolean bullish) {

        String parentKey = bullish ? "trend.bull" : "trend.bear";

        Configuration config = ConfigurationUtil.getStrategyConfig(strategyName);
        Configuration subset = config.subset(parentKey);
        Iterator<String> iterator = subset.getKeys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = subset.getString(key);
            config.setProperty(key, value);
            getRuleService().setProperty(strategyName, key, value);
        }

        // only log INFO if we are in realtime
        String message = "switched trend to " + (bullish ? "bullish" : "bearish");
        if (getRuleService().getInternalClock(strategyName) == true) {
            logger.info(message);
        } else {
            logger.debug(message);
        }
    }

    protected void handleOpenPosition(String strategyName, int stockOptionId, BigDecimal currentValue, BigDecimal underlayingSpot,
            double volatility, BigDecimal stockOptionSettlement, BigDecimal underlayingSettlement) throws Exception {

        Strategy strategy = getLookupService().getStrategyByNameFetched(strategyName);
        StockOption stockOption = (StockOption) getLookupService().getSecurityFetched(stockOptionId);

        double currentValueDouble = currentValue.doubleValue();
        double underlayingValueDouble = underlayingSpot.doubleValue();
        double stockOptionSettlementDouble = stockOptionSettlement.doubleValue();
        double underlayingSettlementDouble = underlayingSettlement.doubleValue();
        int contractSize = stockOption.getSecurityFamily().getContractSize();

        double maintenanceMargin = StockOptionUtil.getMaintenanceMargin(stockOption, stockOptionSettlementDouble, underlayingSettlementDouble);
        double initialMargin = maintenanceMargin * initialMarginMarkup;

        // get the exitValue based on the current Volatility
        double exitValueByVola = ThetaUtil.getExitValueDouble(strategy.getName(), stockOption, underlayingValueDouble, volatility);

        // get the exitValue based on the max loss for this position
        double exitValueByMaxAtRiskRatioPerTrade = getExitValueByMaxAtRiskRatioPerTrade(strategy, currentValueDouble, maintenanceMargin);

        // choose which ever is lower
        logger.info("exitValueByVola: " + exitValueByVola + " exitValueByMaxAtRiskRatio: " + exitValueByMaxAtRiskRatioPerTrade);
        double exitValue = Math.min(exitValueByVola, exitValueByMaxAtRiskRatioPerTrade);

        // get numberOfContracts based on margin
        long numberOfContractsByMargin = getNumberOfContractsByMargin(strategy, contractSize, initialMargin);

        // get numberOfContracts based on redemption value
        long numberOfContractsByRedemptionValue = getNumberOfContractsByRedemptionValue(strategy, contractSize, currentValueDouble, exitValue);

        // get numberOfContracts based on leverage
        long numberOfContractsByLeverage = getNumberOfContractsByLeverage(stockOption, strategy, contractSize, currentValueDouble);

        // choose which ever is lower
        logger.info("numberOfContractsByMargin: " + numberOfContractsByMargin + " numberOfContractsByRedemptionValue: " + numberOfContractsByRedemptionValue + " numberOfContractsByLeverage: " + numberOfContractsByLeverage);
        long numberOfContracts = Math.min(numberOfContractsByMargin, Math.min(numberOfContractsByRedemptionValue, numberOfContractsByLeverage));

        if (numberOfContracts <= 0) {

            // if there is no money left, and there is no open position, remove the stockOption from the watchlist
            Position position = getLookupService().getPositionBySecurityAndStrategy(stockOption.getId(), strategyName);
            if (position == null || !position.isOpen()) {
                getDispatcherService().getTickService().removeFromWatchlist(strategy, stockOption);
            }
            getRuleService().deactivate(strategy.getName(), "OPEN_POSITION");
            return;
        }

        // the stockOption might have been removed from the watchlist by another statement (i.e. closePosition)
        if (!stockOption.isOnWatchlist()) {
            getDispatcherService().getTickService().putOnWatchlist(strategy, stockOption);
        }

        OrderVO order = new OrderVO();
        order.setStrategyName(strategyName);
        order.setSecurityId(stockOption.getId());
        order.setRequestedQuantity(numberOfContracts);
        order.setTransactionType(TransactionType.SELL);

        getDispatcherService().getTransactionService().executeTransaction(strategy.getName(), order);

        // if a position was open (or already existed) set margin and exitValue
        Position position = getLookupService().getPositionBySecurityAndStrategy(stockOption.getId(), strategyName);
        if (position != null) {
            getStockOptionService().setMargin(position.getId());
            getPositionService().setExitValue(position.getId(), exitValue, true);
        }

        getRuleService().deactivate(strategy.getName(), "OPEN_POSITION");
    }

    /**
     * invested capital: maintenanceMargin (=additionalMargin)
     * max risk: exitValue - current Value
     * atRiskRatioPerTrade = max risk / invested capital
     */
    private double getExitValueByMaxAtRiskRatioPerTrade(Strategy strategy, double currentValueDouble, double maintenanceMargin) {

        double maxAtRiskRatioPerTrade = ConfigurationUtil.getStrategyConfig(strategy.getName()).getDouble("maxAtRiskRatioPerTrade");
        return maxAtRiskRatioPerTrade * maintenanceMargin + currentValueDouble;
    }

    /**
     * how many options can we sell for the available amount of cash
     */
    private long getNumberOfContractsByMargin(Strategy strategy, int contractSize, double initialMargin) {

        long numberOfContractsByAvailableFunds = (long) ((strategy.getAvailableFundsDouble() / initialMargin) / contractSize);

        if (simulation) {

            return numberOfContractsByAvailableFunds;

        } else {

            // check max numberOfContracts for ManagedAccounts
            long numberOfContractsFromAccounts = getDispatcherService().getAccountService().getNumberOfContractsByMargin(strategy.getName(), contractSize * initialMargin);

            return Math.min(numberOfContractsByAvailableFunds, numberOfContractsFromAccounts);
        }
    }

    /**
     * available cash after this trade: cashbalance now + quantity * contractSize * currentValue
     * total redemptionValue after this trade:  quantity * contractSize * exitValue + RedemptionValue of the other positions
     * atRiskRatioOfPortfolio = total redemptionValue / available cash after this trade
     * (we could adjust the exitValue or the quantity, but we trust
     * the exitValue set above and only adjust the quantity)
     */
    private long getNumberOfContractsByRedemptionValue(Strategy strategy, int contractSize, double currentValueDouble, double exitValue) {

        if (!ConfigurationUtil.getStrategyConfig(strategy.getName()).getBoolean("numberOfContractsByRedemptionValueEnabled"))
            return Long.MAX_VALUE;

        double maxAtRiskRatioOfPortfolio = ConfigurationUtil.getStrategyConfig(strategy.getName()).getDouble("maxAtRiskRatioOfPortfolio");

        return (long) ((maxAtRiskRatioOfPortfolio * strategy.getCashBalanceDouble() - strategy.getRedemptionValue()) / (contractSize * (exitValue - maxAtRiskRatioOfPortfolio * currentValueDouble)));
    }

    /**
     * makes sure, that we do not exceed the maximum leverage on the portfolio
     * additional DeltaRisk / NetLiq + existing leverage = max leverage
     */
    private long getNumberOfContractsByLeverage(StockOption stockOption, Strategy strategy, int contractSize, double currentValueDouble) {

        if (!ConfigurationUtil.getStrategyConfig(strategy.getName()).getBoolean("numberOfContractsByLeverageEnabled"))
            return Long.MAX_VALUE;

        double maxLeverage = ConfigurationUtil.getStrategyConfig(strategy.getName()).getDouble("maxLeverage");
        double signedMaxLeverage = OptionType.PUT.equals(stockOption.getType()) ? maxLeverage : -maxLeverage;

        return -(long) ((signedMaxLeverage - strategy.getLeverage()) * strategy.getNetLiqValueDouble() / stockOption.getLeverage() / contractSize / currentValueDouble);
    }

    protected void handleBuySignal(String strategyName, int underlayingId, BigDecimal underlayingSpot) throws Exception {

        if (firstBuyTime != 0 && getRuleService().getCurrentTime(strategyName) < firstBuyTime) {
            logger.debug("ignoring signal, because we are before firstBuyTime");
            return;
        }

        if (lastBuyTime != 0 && getRuleService().getCurrentTime(strategyName) > lastBuyTime) {
            logger.debug("ignoring signal, because we are after lastBuyTime");
            return;
        }

        if (!getRuleService().isActive(strategyName, "OPEN_POSITION")) {
            StockOption stockOption = getStockOption(strategyName, underlayingId, underlayingSpot, OptionType.PUT);
            getDispatcherService().getTickService().putOnWatchlist(strategyName, stockOption.getId());
            getRuleService().activate(strategyName, "OPEN_POSITION", stockOption.getId());
        }
    }

    protected void handleSellSignal(String strategyName, int underlayingId, BigDecimal underlayingSpot) throws Exception {

        if (firstBuyTime != 0 && getRuleService().getCurrentTime(strategyName) < firstBuyTime) {
            logger.debug("ignoring signal, because we are before firstBuyTime");
            return;
        }

        if (lastBuyTime != 0 && getRuleService().getCurrentTime(strategyName) > lastBuyTime) {
            logger.debug("ignoring signal, because we are after lastBuyTime");
            return;
        }

        if (!getRuleService().isActive(strategyName, "OPEN_POSITION")) {
            StockOption stockOption = getStockOption(strategyName, underlayingId, underlayingSpot, OptionType.CALL);
            getDispatcherService().getTickService().putOnWatchlist(strategyName, stockOption.getId());
            getRuleService().activate(strategyName, "OPEN_POSITION", stockOption.getId());
        }
    }

    protected void handleRollPosition(String strategyName, int positionId, int underlayingId, BigDecimal underlayingSpot) throws Exception {

        getPositionService().closePosition(positionId);

        if (!getRuleService().isActive(strategyName, "OPEN_POSITION")) {
            StockOption oldStockOption = (StockOption) getLookupService().getPosition(positionId).getSecurity();
            StockOption newStockOption = getStockOption(strategyName, underlayingId, underlayingSpot, oldStockOption.getType());

            getDispatcherService().getTickService().putOnWatchlist(strategyName, newStockOption.getId());
            getRuleService().activate(strategyName, "OPEN_POSITION", newStockOption.getId());
        }
    }

    private StockOption getStockOption(String strategyName, int underlayingSecurityId, BigDecimal underlayingSpot, OptionType optionType) throws Exception {

        int minAge = ConfigurationUtil.getStrategyConfig(strategyName).getInt("minAge");

        SecurityFamily family = getLookupService().getStockOptionFamilyByUnderlaying(underlayingSecurityId);

        Date targetExpirationDate = new Date(DateUtil.getCurrentEPTime().getTime() + minAge);

        StockOption stockOption = getLookupService().getNearestStockOption(underlayingSecurityId, targetExpirationDate, underlayingSpot, optionType.getValue());

        if (simulation) {
            if ((stockOption == null)
                    || (stockOption.getExpiration().getTime() > (targetExpirationDate.getTime() + this.FORTY_FIVE_DAYS ))
                    || (OptionType.CALL.equals(optionType) && stockOption.getStrike().doubleValue() > underlayingSpot.doubleValue() + 50)
                    || (OptionType.PUT.equals(optionType) && stockOption.getStrike().doubleValue() < underlayingSpot.doubleValue() - 50)) {

                stockOption = getStockOptionService().createDummyStockOption(family.getId(), targetExpirationDate, underlayingSpot, optionType);
            }
        }
        return stockOption;
    }

    public static class TrendSubscriber {

        public void update(String strategyName, boolean bullish) {

            long startTime = System.currentTimeMillis();
            logger.debug("setTrend start");

            ServiceLocator.commonInstance().getThetaService().setTrend(strategyName, bullish);

            logger.debug("setTrend end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
        }
    }

    public static class OpenPositionSubscriber {

        public void update(String strategyName, int securityId, BigDecimal currentValue, BigDecimal underlayingSpot, BigDecimal stockOptionSettlement, BigDecimal underlayingSettlement, double volatility) {

            long startTime = System.currentTimeMillis();
            logger.debug("openPosition start");

            ServiceLocator.commonInstance().getThetaService().openPosition(strategyName, securityId, currentValue, underlayingSpot, volatility, stockOptionSettlement, underlayingSettlement);

            logger.debug("openPosition end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
        }
    }

    public static class BuySignalSubscriber {

        public void update(String strategyName, int underlayingId, BigDecimal underlayingSpot) {

            long startTime = System.currentTimeMillis();
            logger.debug("buySignal start");

            ServiceLocator.commonInstance().getThetaService().buySignal(strategyName, underlayingId, underlayingSpot);

            logger.debug("buySignal end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
        }
    }

    public static class SellSignalSubscriber {

        public void update(String strategyName, int underlayingId, BigDecimal underlayingSpot) {

            long startTime = System.currentTimeMillis();
            logger.debug("sellSignal start");

            ServiceLocator.commonInstance().getThetaService().sellSignal(strategyName, underlayingId, underlayingSpot);

            logger.debug("sellSignal end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
        }
    }

    public static class RollPositionSubscriber {

        public void update(String strategyName, int positionId, int underlayingId, BigDecimal underlayingSpot) {

            long startTime = System.currentTimeMillis();
            logger.debug("rollPosition start");

            ServiceLocator.commonInstance().getThetaService().rollPosition(strategyName, positionId, underlayingId, underlayingSpot);

            logger.debug("rollPosition end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
        }
    }
}
