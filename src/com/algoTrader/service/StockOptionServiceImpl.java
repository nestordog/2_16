package com.algoTrader.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.math.MathException;
import org.apache.log4j.Logger;

import com.algoTrader.entity.Account;
import com.algoTrader.entity.Order;
import com.algoTrader.entity.OrderImpl;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.StockOptionImpl;
import com.algoTrader.entity.Tick;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.Market;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.enumeration.OrderStatus;
import com.algoTrader.enumeration.RuleName;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.stockOption.StockOptionUtil;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.PropertiesUtil;
import com.algoTrader.util.RoundUtil;

public class StockOptionServiceImpl extends com.algoTrader.service.StockOptionServiceBase {

    private static boolean simulation = PropertiesUtil.getBooleanProperty("simulation");
    private static Market market = Market.fromString(PropertiesUtil.getProperty("strategie.market"));
    private static Currency currency = Currency.fromString(PropertiesUtil.getProperty("strategie.currency"));
    private static int contractSize = PropertiesUtil.getIntProperty("strategie.contractSize");
    private static double initialMarginMarkup = PropertiesUtil.getDoubleProperty("strategie.initialMarginMarkup");
    private static boolean numberOfContractsByRedemptionValueEnabled = PropertiesUtil.getBooleanProperty("numberOfContractsByRedemptionValueEnabled");
    private static boolean numberOfContractsByLeverageEnabled = PropertiesUtil.getBooleanProperty("numberOfContractsByLeverageEnabled");

    private static int minAge = PropertiesUtil.getIntProperty("minAge");

    private static Logger logger = MyLogger.getLogger(StockOptionServiceImpl.class.getName());

    private static SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");

    private long FORTY_FIVE_DAYS = 3888000000l;

    protected StockOption handleGetStockOption(int underlayingSecurityId, BigDecimal underlayingSpot, OptionType optionType) throws Exception {

        Security underlaying = getSecurityDao().load(underlayingSecurityId);

        Date targetExpirationDate = new Date(DateUtil.getCurrentEPTime().getTime() + minAge);

        StockOption stockOption = getStockOptionDao().findNearestStockOption(underlaying.getId(), targetExpirationDate, underlayingSpot, optionType.getValue());

        if (simulation) {
            if ((stockOption == null)
                    || (stockOption.getExpiration().getTime() > (targetExpirationDate.getTime() + this.FORTY_FIVE_DAYS ))
                    || (OptionType.CALL.equals(optionType) && stockOption.getStrike().doubleValue() > underlayingSpot.doubleValue() + 50)
                    || (OptionType.PUT.equals(optionType) && stockOption.getStrike().doubleValue() < underlayingSpot.doubleValue() - 50)) {

                stockOption = createDummyStockOption(underlaying, targetExpirationDate, underlayingSpot, optionType);

                getStockOptionDao().create(stockOption);
            }
        }
        return stockOption;
    }

    private StockOption createDummyStockOption(Security underlaying, Date targetExpirationDate, BigDecimal underlayingSpot, OptionType type) throws Exception {

        // set third Friday of the month
        Date expiration = DateUtil.getNextThirdFriday(targetExpirationDate);

        BigDecimal strike = RoundUtil.roundToNextN(underlayingSpot, 50, type);

        // symbol
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(expiration);
        String symbol = "O" +
        underlaying.getSymbol() + " " +
        new SimpleDateFormat("MMM").format(cal.getTime()).toUpperCase() + "/" +
        (cal.get(Calendar.YEAR) + "-").substring(2) +
        type.toString().substring(0, 1) + " " +
        strike.intValue() + " " +
        contractSize;

        StockOption stockOption = new StockOptionImpl();
        stockOption.setIsin(null); // dummys don't have a isin
        stockOption.setSymbol(symbol);
        stockOption.setMarket(market);
        stockOption.setCurrency(currency);
        stockOption.setOnWatchlist(false);
        stockOption.setStrike(strike);
        stockOption.setExpiration(expiration);
        stockOption.setType(type);
        stockOption.setContractSize(contractSize);
        stockOption.setUnderlaying(underlaying);
        stockOption.setMarketOpen(format.parse("09:00:00"));
        stockOption.setMarketClose(format.parse("17:00:00"));

        getStockOptionDao().create(stockOption);

        logger.info("created dummy option " + stockOption.getSymbol());

        return stockOption;
    }

    protected void handleOpenPosition(int stockOptionId, BigDecimal currentValue, BigDecimal underlayingSpot, double volatility, BigDecimal stockOptionSettlement, BigDecimal underlayingSettlement)
            throws Exception {

        StockOption stockOption = (StockOption)getStockOptionDao().load(stockOptionId);
        Account account = getAccountDao().findByCurrency(stockOption.getCurrency());
        int contractSize = stockOption.getContractSize();
        double currentValueDouble = currentValue.doubleValue();
        double underlayingValueDouble = underlayingSpot.doubleValue();
        double stockOptionSettlementDouble = stockOptionSettlement.doubleValue();
        double underlayingSettlementDouble = underlayingSettlement.doubleValue();

        double maintenanceMargin = StockOptionUtil.getMaintenanceMargin(stockOption, stockOptionSettlementDouble, underlayingSettlementDouble);
        double initialMargin = maintenanceMargin * initialMarginMarkup;

        // get the exitValue based on the current Volatility
        double exitValueByVola = StockOptionUtil.getExitValueDouble(stockOption, underlayingValueDouble, volatility);

        // get the exitValue based on the max loss for this position
        double exitValueByMaxAtRiskRatioPerTrade = getExitValueByMaxAtRiskRatioPerTrade(currentValueDouble, maintenanceMargin);

        // choose which ever is lower
        logger.info("exitValueByVola: " + exitValueByVola + " exitValueByMaxAtRiskRatio: " + exitValueByMaxAtRiskRatioPerTrade);
        double exitValue = Math.min(exitValueByVola, exitValueByMaxAtRiskRatioPerTrade);

        // get numberOfContracts based on margin
        long numberOfContractsByMargin = getNumberOfContractsByMargin(contractSize, initialMargin);

        // get numberOfContracts based on redemption value
        long numberOfContractsByRedemptionValue = getNumberOfContractsByRedemptionValue(account, contractSize, currentValueDouble, exitValue);

        // get numberOfContracts based on leverage
        long numberOfContractsByLeverage = getNumberOfContractsByLeverage(stockOption, account, contractSize, currentValueDouble);

        // choose which ever is lower
        logger.info("numberOfContractsByMargin: " + numberOfContractsByMargin + " numberOfContractsByRedemptionValue: " + numberOfContractsByRedemptionValue + " numberOfContractsByLeverage: " + numberOfContractsByLeverage);
        long numberOfContracts = Math.min(numberOfContractsByMargin, Math.min(numberOfContractsByRedemptionValue, numberOfContractsByLeverage));

        if (numberOfContracts <= 0) {
            if (stockOption.getPosition() == null || !stockOption.getPosition().isOpen()) {
                getDispatcherService().getTickService().removeFromWatchlist(stockOptionId);
            }
            return; // there is no money available
        }

        // the stockOption might have been removed from the watchlist by another statement (i.e. closePosition)
        if (!stockOption.isOnWatchlist()) {
            getDispatcherService().getTickService().putOnWatchlist(stockOptionId);
        }

        Order order = new OrderImpl();
        order.setSecurity(stockOption);
        order.setRequestedQuantity(numberOfContracts);
        order.setTransactionType(TransactionType.SELL);

        getDispatcherService().getTransactionService().executeTransaction(order);

        Position position = order.getSecurity().getPosition();
        if (position != null) {
            setMargin(position);
            setExitValue(stockOption.getPosition(), exitValue);
        }
    }

    protected void handleClosePosition(int positionId) throws Exception {

        Position position = getPositionDao().load(positionId);

        StockOption stockOption = (StockOption)position.getSecurity();

        long numberOfContracts = Math.abs(position.getQuantity());

        Order order = new OrderImpl();
        order.setSecurity(stockOption);
        order.setRequestedQuantity(numberOfContracts);
        order.setTransactionType(TransactionType.BUY);

        getDispatcherService().getTransactionService().executeTransaction(order);

        // only remove the stockOption from the watchlist, if the transaction did execute fully.
        // otherwise the next tick will execute the reminder of the order
        if (OrderStatus.EXECUTED.equals(order.getStatus()) ||
                OrderStatus.AUTOMATIC.equals(order.getStatus())) {

            getDispatcherService().getTickService().removeFromWatchlist(stockOption);

            // if there is a and OPEN_POSITION rule acitve for this stockOption deactivate it
            getRuleService().deactivate(RuleName.OPEN_POSITION, stockOption);
        }
    }

    protected void handleExpirePosition(int positionId) throws Exception {

        Position position = getPositionDao().load(positionId);

        if (position.getExitValue() == null || position.getExitValue().doubleValue() == 0d) {
            logger.warn(position.getSecurity().getSymbol() + " expired but did not have a exit value specified");
        }

        StockOption stockOption = (StockOption)position.getSecurity();

        long numberOfContracts = Math.abs(position.getQuantity());

        Order order = new OrderImpl();
        order.setSecurity(stockOption);
        order.setRequestedQuantity(numberOfContracts);
        order.setTransactionType(TransactionType.EXPIRATION);

        getDispatcherService().getTransactionService().executeTransaction(order);

        // only remove the stockOption from the watchlist, if the transaction did execute fully.
        // otherwise the next tick will execute the reminder of the order
        if (OrderStatus.EXECUTED.equals(order.getStatus()) ||
                OrderStatus.AUTOMATIC.equals(order.getStatus())) {

            getDispatcherService().getTickService().removeFromWatchlist(stockOption);
        }
    }

    @SuppressWarnings("unchecked")
    protected void handleSetMargins() throws Exception {

        List<Position> positions = getPositionDao().findOpenPositions();

        for (Position position : positions) {
            setMargin(position);
        }
    }

    protected void handleSetExitValue(int positionId, double exitValue) throws MathException {

        // we don't want to set the exitValue to Zero
        if (exitValue <= 0.05) {
            return;
        }

        Position position = getPositionDao().load(positionId);

        if (position == null) {
            throw new StockOptionServiceException("position does not exist: " + positionId);
        }


        if (position.getExitValue() == null) {
            logger.warn("no exitValue was set for position: " + positionId);
            return;
        }

        if (exitValue > position.getExitValue().doubleValue()) {
            logger.warn("exit value " + exitValue + " is higher than existing exit value " + position.getExitValue() + " of position " + positionId);
            return;
        }

        setExitValue(position, exitValue);

    }

    private void setMargin(Position position) throws Exception {

        StockOption stockOption = (StockOption) position.getSecurity();
        Tick stockOptionTick = stockOption.getLastTick();
        Tick underlayingTick = stockOption.getUnderlaying().getLastTick();

        if (stockOptionTick != null && underlayingTick != null && stockOptionTick.getCurrentValueDouble() > 0.0) {

            double marginPerContract = 0;
            try {
                marginPerContract = StockOptionUtil.getMaintenanceMargin(stockOption, stockOptionTick.getSettlement().doubleValue(), underlayingTick.getSettlement().doubleValue()) * stockOption.getContractSize();
            } catch (IllegalArgumentException e) {
                logger.warn("could not calculate margin for " + stockOption.getSymbol(), e);
                return;
            }
            long numberOfContracts = Math.abs(position.getQuantity());
            BigDecimal totalMargin = RoundUtil.getBigDecimal(marginPerContract * numberOfContracts);

            position.setMaintenanceMargin(totalMargin);

            getPositionDao().update(position);

            Account account = position.getAccount();

            int percent = (int)(account.getAvailableFundsDouble() / account.getCashBalanceDouble() * 100.0);
            if (account.getAvailableFundsDouble() >= 0) {
                logger.info("set margin for " + stockOption.getSymbol() + " to " + RoundUtil.getBigDecimal(marginPerContract) + " total margin: " + account.getMaintenanceMargin()
                        + " availableFunds: " + account.getAvailableFunds() + " (" + percent + "% of balance)");
            } else {
                logger.warn("set margin for " + stockOption.getSymbol() + " to " + RoundUtil.getBigDecimal(marginPerContract) + " total margin: " + account.getMaintenanceMargin()
                        + " availableFunds: " + account.getAvailableFunds() + " (" + percent + "% of balance)");
            }
        } else {
            logger.warn("no last tick available or currentValue to low to set margin on " + stockOption.getSymbol());
        }

    }


    private void setExitValue(Position position, double exitValue) throws MathException {

        double currentValue = position.getSecurity().getLastTick().getCurrentValueDouble();
        if (exitValue < currentValue ) {
            throw new StockOptionServiceException("ExitValue (" + exitValue + ") for position " + position.getId() + " is lower than currentValue: " + currentValue);
        }

        position.setExitValue(exitValue);
        getPositionDao().update(position);

        logger.info("set exit value " + position.getSecurity().getSymbol() + " to " + exitValue);
    }

    /**
     * invested capital: maintenanceMargin (=additionalMargin) max risk:
     * exitValue - current Value atRiskRatioPerTrade = max risk / invested
     * capital
     */
    private double getExitValueByMaxAtRiskRatioPerTrade(double currentValueDouble, double maintenanceMargin) {

        return PropertiesUtil.getDoubleProperty("maxAtRiskRatioPerTrade") * maintenanceMargin + currentValueDouble;
    }

    /**
     * how many options can we sell for the available amount of cash
     */
    private long getNumberOfContractsByMargin(int contractSize, double initialMargin) {

        if (simulation) {

            Account account = getAccountDao().findByCurrency(currency);
            return (long) ((account.getAvailableFundsDouble() / initialMargin) / contractSize);
        } else {

            return getDispatcherService().getAccountService().getNumberOfContractsByMargin(contractSize * initialMargin);
        }
    }

    /**
     * available cash after this trade: cashbalance now + quantity * contractSize * currentValue
     * total redemptionValue = quantity * contractSize * exitValue + RedemptionValue of the other positions
     * atRiskRatioOfPortfolio = total redemptionValue / available cash after this trade
     * (we could adjust the exitValue or the quantity, but we trust
     * the exitValue set above and only adjust the quantity)
     */
    private long getNumberOfContractsByRedemptionValue(Account account, int contractSize, double currentValueDouble, double exitValue) {

        if (!numberOfContractsByRedemptionValueEnabled)
            return Long.MAX_VALUE;

        double maxAtRiskRatioOfPortfolio = PropertiesUtil.getDoubleProperty("maxAtRiskRatioOfPortfolio");

        return (long) ((maxAtRiskRatioOfPortfolio * account.getCashBalanceDouble() - account.getRedemptionValue()) / (contractSize * (exitValue - maxAtRiskRatioOfPortfolio * currentValueDouble)));
    }

    /**
     * makes sure, that we do not exceed the maximum leverage on the portfolio
     * additional DeltaRisk / NetLiq + existing leverage = max leverage
     */
    private long getNumberOfContractsByLeverage(StockOption stockOption, Account account, int contractSize, double currentValueDouble) {

        if (!numberOfContractsByLeverageEnabled)
            return Long.MAX_VALUE;

        double maxLeverage = PropertiesUtil.getDoubleProperty("strategie.maxLeverage");
        double signedMaxLeverage = OptionType.PUT.equals(stockOption.getType()) ? maxLeverage : -maxLeverage;

        return -(long) ((signedMaxLeverage - account.getLeverage()) * account.getNetLiqValueDouble() / stockOption.getLeverage() / contractSize / currentValueDouble);
    }
}
