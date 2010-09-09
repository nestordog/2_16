package com.algoTrader.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.util.MathUtils;
import org.apache.log4j.Logger;

import com.algoTrader.criteria.CallOptionCriteria;
import com.algoTrader.criteria.PutOptionCriteria;
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

    private static int minAge = PropertiesUtil.getIntProperty("minAge");

    private static Logger logger = MyLogger.getLogger(StockOptionServiceImpl.class.getName());

    private long FORTY_FIVE_DAYS = 3888000000l;

    protected StockOption handleGetStockOption(int underlayingSecurityId, BigDecimal underlayingSpot, OptionType optionType) throws Exception {

        Security underlaying = getSecurityDao().load(underlayingSecurityId);

        Date targetExpirationDate = new Date(DateUtil.getCurrentEPTime().getTime() + minAge);

        StockOption stockOption = findNearestStockOption(underlaying, targetExpirationDate, underlayingSpot, optionType);

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


        BigDecimal strike;
        if (OptionType.CALL.equals(type)) {
            // increase by strikeOffset and round to upper 50
            strike = RoundUtil.getBigDecimal(MathUtils.round((underlayingSpot.doubleValue()) / 50.0, 0, BigDecimal.ROUND_CEILING) * 50.0);
        } else {
            // reduce by strikeOffset and round to lower 50
            strike = RoundUtil.getBigDecimal(MathUtils.round((underlayingSpot.doubleValue()) / 50.0, 0, BigDecimal.ROUND_FLOOR) * 50.0);
        }

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

        getStockOptionDao().create(stockOption);

        logger.info("created dummy option " + stockOption.getSymbol());

        return stockOption;
    }

    @SuppressWarnings("unchecked")
    private StockOption findNearestStockOption(Security underlaying, Date targetExpirationDate, BigDecimal underlayingSpot,
            OptionType type) throws Exception {

        List<StockOption> list;
        if (OptionType.CALL.equals(type)) {
            BigDecimal targetStrike = RoundUtil.getBigDecimal(underlayingSpot.doubleValue());
            CallOptionCriteria criteria = new CallOptionCriteria(underlaying, targetExpirationDate, targetStrike, type);
            criteria.setMaximumResultSize(new Integer(1));
            list = getStockOptionDao().findCallOptionByCriteria(criteria);

        } else {
            BigDecimal targetStrike = RoundUtil.getBigDecimal(underlayingSpot.doubleValue());
            PutOptionCriteria criteria = new PutOptionCriteria(underlaying, targetExpirationDate, targetStrike, type);
            criteria.setMaximumResultSize(new Integer(1));
            list = getStockOptionDao().findPutOptionByCriteria(criteria);
        }

        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
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
        double exitValueByVola = StockOptionUtil.getExitValue(stockOption, underlayingValueDouble, volatility);

        // get the exitValue on the max loss for this position
        // invested capital: maintenanceMargin (=additionalMargin)
        //         max risk: exitValue - current Value
        //         atRiskRatioPerTrade = max risk / invested capital
        double exitValueByMaxAtRiskRatio = PropertiesUtil.getDoubleProperty("maxAtRiskRatioPerTrade") * maintenanceMargin + currentValueDouble;

        // choose which ever is lower
        logger.info("exitValueByVola: " + exitValueByVola + " exitValueByMaxAtRiskRatio: " + exitValueByMaxAtRiskRatio);
        double exitValue = Math.min(exitValueByVola, exitValueByMaxAtRiskRatio);

        // get numberOfContracts based on margin
        // (how many options can we sell for the available amount of cash)
        long numberOfContractsByMargin = getNumberOfContractsByMargin(contractSize, initialMargin);

        // get maxNumberOfContracts based on RedemptionValue
        //         available cash after this trade: cashbalance now + quantity * contractSize * currentValue
        //        total redemptionValue = quantity * contractSize * exitValue + RedemptionValue of the other positions
        //        atRiskRatioOfPortfolio = total redemptionValue / available cash after this trade
        //        (we could adjust the exitValue or the quantity, but we trust the exitValue set above and only adjust the quantity)
        double maxAtRiskRatioOfPortfolio = PropertiesUtil.getDoubleProperty("maxAtRiskRatioOfPortfolio");
        long numberOfContractsByRedemptionValue =
            (long)((maxAtRiskRatioOfPortfolio * account.getCashBalanceDouble() - account.getRedemptionValue()) /
            (contractSize *(exitValue - maxAtRiskRatioOfPortfolio * currentValueDouble)));

        // choose which ever is lower
        logger.info("numberOfContractsByMargin: " + numberOfContractsByMargin + " numberOfContractsByRedemptionValue: " + numberOfContractsByRedemptionValue);
        long numberOfContracts= Math.min(numberOfContractsByMargin, numberOfContractsByRedemptionValue);

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
        }

        setExitValue(stockOption.getPosition(), exitValue);
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

    protected void handleSetExitValue(int positionId, double exitValue) throws ConvergenceException, FunctionEvaluationException {

        // we don't want to set the exitValue to Zero
        if (exitValue <= 0.05) {
            return;
        }

        Position position = getPositionDao().load(positionId);

        if (position == null) {
            throw new StockOptionServiceException("position does not exist: " + positionId);
        }


        if (position.getExitValue() == null) {
            throw new StockOptionServiceException("no exitValue was set for position: " + positionId);
        }

        if (exitValue > position.getExitValue().doubleValue()) {
            throw new StockOptionServiceException("exit value " + exitValue + " is higher than existing exit value " + position.getExitValue() + " of position " + positionId);
        }

        setExitValue(position, exitValue);

    }

    private void setMargin(Position position) throws Exception {

        StockOption stockOption = (StockOption) position.getSecurity();
        Tick stockOptionTick = stockOption.getLastTick();
        Tick underlayingTick = stockOption.getUnderlaying().getLastTick();

        if (stockOptionTick != null && underlayingTick != null) {

            double marginPerContract = StockOptionUtil.getMaintenanceMargin(stockOption, stockOptionTick.getSettlementDouble(), underlayingTick.getSettlementDouble()) * stockOption.getContractSize();
            long numberOfContracts = Math.abs(position.getQuantity());
            BigDecimal totalMargin = RoundUtil.getBigDecimal(marginPerContract * numberOfContracts);

            position.setMaintenanceMargin(totalMargin);

            getPositionDao().update(position);

            Account account = position.getAccount();

            int percent = (int)(account.getAvailableFundsDouble() / account.getCashBalanceDouble() * 100.0);
            if (account.getAvailableFundsDouble() >= 0) {
                logger.info("set margin for " + stockOption.getSymbol() + " to " + RoundUtil.getBigDecimal(marginPerContract) + " total margin: " + account.getMaintenanceMargin() + " available amount: " + account.getAvailableFunds() + " (" + percent + "% of balance)");
            } else {
                logger.warn("set margin for " + stockOption.getSymbol() + " to " + RoundUtil.getBigDecimal(marginPerContract) + " total margin: " + account.getMaintenanceMargin() + " available amount: " + account.getAvailableFunds() + " (" + percent + "% of balance)");
            }
        } else {
            logger.warn("no last tick available to set margin on " + stockOption.getSymbol());
        }

    }


    private void setExitValue(Position position, double exitValue) throws ConvergenceException, FunctionEvaluationException {

        double currentValue = position.getSecurity().getLastTick().getCurrentValueDouble();
        if (exitValue < currentValue ) {
            throw new StockOptionServiceException("ExitValue (" + exitValue + ") for position " + position.getId() + " is lower than currentValue: " + currentValue);
        }

        position.setExitValue(RoundUtil.getBigDecimal(exitValue));
        getPositionDao().update(position);

        logger.info("set exit value " + position.getSecurity().getSymbol() + " to " + exitValue);
    }

    private long getNumberOfContractsByMargin(int contractSize, double initialMargin) {

        if (simulation) {

            Account account = getAccountDao().findByCurrency(currency);
            return (long) ((account.getAvailableFundsDouble() / initialMargin) / contractSize);
        } else {

            return getDispatcherService().getAccountService().getNumberOfContractsByMargin(contractSize * initialMargin);
        }
    }
}
