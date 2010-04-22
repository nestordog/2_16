package com.algoTrader.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.log4j.Logger;

import com.algoTrader.criteria.StockOptionCriteria;
import com.algoTrader.entity.Account;
import com.algoTrader.entity.Order;
import com.algoTrader.entity.OrderImpl;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.StockOptionImpl;
import com.algoTrader.entity.Tick;
import com.algoTrader.entity.Transaction;
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

    private static Market market = Market.fromString(PropertiesUtil.getProperty("simulation.market"));
    private static Currency currency = Currency.fromString(PropertiesUtil.getProperty("simulation.currency"));
    private static OptionType optionType = OptionType.fromString(PropertiesUtil.getProperty("simulation.optionType"));

    private static int contractSize = PropertiesUtil.getIntProperty("simulation.contractSize");
    private static boolean simulation = PropertiesUtil.getBooleanProperty("simulation");
    private static int minAge = PropertiesUtil.getIntProperty("minAge");
    private static int openPositionRetries = PropertiesUtil.getIntProperty("openPositionRetries");

    private static Logger logger = MyLogger.getLogger(StockOptionServiceImpl.class.getName());

    private long FORTY_FIVE_DAYS = 3888000000l;

    protected StockOption handleGetStockOption(int underlayingSecurityId, BigDecimal underlayingSpot) throws Exception {

        Security underlaying = getSecurityDao().load(underlayingSecurityId);

        Date targetExpirationDate = new Date(DateUtil.getCurrentEPTime().getTime() + minAge);

        StockOption stockOption = findNearestStockOption(underlaying, targetExpirationDate, underlayingSpot, optionType);

        if (simulation) {
            if ((stockOption == null)
                    || (stockOption.getExpiration().getTime() > (targetExpirationDate.getTime() + FORTY_FIVE_DAYS ))
                    || (stockOption.getStrike().doubleValue() < underlayingSpot.doubleValue() - 50)) {

                stockOption = createDummyStockOption(underlaying, targetExpirationDate, underlayingSpot, optionType);

                getStockOptionDao().create(stockOption);
            }
        }
        return stockOption;
    }

    private StockOption createDummyStockOption(Security underlaying, Date expiration, BigDecimal strike, OptionType type) throws Exception {

        // set third Friday of the month
        expiration = DateUtil.getNextThirdFriday(expiration);

        // round to 50.-
        strike = RoundUtil.roundTo50(strike);

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
        stockOption.setDummy(true);
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
    private StockOption findNearestStockOption(Security underlaying, Date expiration, BigDecimal strike,
            OptionType type) throws Exception {

           StockOptionCriteria criteria = new StockOptionCriteria(underlaying, expiration, strike, type);
           criteria.setMaximumResultSize(new Integer(1));

           List<StockOption> list = getStockOptionDao().findByCriteria(criteria);
           if (list.size() > 0) {
               return list.get(0);
           } else {
               return null;
           }
    }

    protected void handleOpenPosition(int stockOptionId, BigDecimal settlement, BigDecimal currentValue, BigDecimal underlayingSpot) throws Exception {

        StockOption stockOption = (StockOption)getStockOptionDao().load(stockOptionId);

        Account account = getAccountDao().findByCurrency(stockOption.getCurrency());

        double availableAmount = account.getAvailableAmount().doubleValue();
        double margin = StockOptionUtil.getMargin(stockOption, settlement.doubleValue(), underlayingSpot.doubleValue());
        double currentDouble = currentValue.doubleValue();
        int contractSize = stockOption.getContractSize();

        long numberOfContracts = (long)((availableAmount / (margin - currentDouble)) / contractSize); // i.e. 2 (for 20 stockOptions)

        if (numberOfContracts <= 0) {
            if (stockOption.getPosition() == null || stockOption.getPosition().getQuantity() == 0) {
                getWatchlistService().removeFromWatchlist(stockOptionId);
            }
            return; // there is no money available
        }

        // the stockOption might have been removed from the watchlist by another statement (i.e. closePosition)
        if (!stockOption.isOnWatchlist()) {
            getWatchlistService().putOnWatchlist(stockOptionId);
        }

        for (int i = 0; i < openPositionRetries ; i++) {

            Order order = new OrderImpl();
            order.setSecurity(stockOption);
            order.setRequestedQuantity(numberOfContracts);
            order.setTransactionType(TransactionType.SELL);

            try {
                getDispatcherService().getTransactionService().executeTransaction(order);
            } catch (TransactionServiceException e) {
                // something went wrong executing the transaction -> keep going
                continue;
            }

            if (OrderStatus.EXECUTED.equals(order.getStatus()) ||
                    OrderStatus.AUTOMATIC.equals(order.getStatus())) {

                // we are done!
                setMargin(order);
                break;

            } else if (OrderStatus.PARTIALLY_EXECUTED.equals(order.getStatus())) {

                // only part of the order has gone through, so reduce the requested
                // numberOfContracts by this number and keep going
                numberOfContracts -= Math.abs(order.getExecutedQuantity());

                setMargin(order);
                continue;
            }
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

            getWatchlistService().removeFromWatchlist(stockOption);

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

            getWatchlistService().removeFromWatchlist(stockOption);
        }
    }

    @SuppressWarnings("unchecked")
    protected void handleSetMargins() throws Exception {

        List<Position> positions = getPositionDao().findOpenPositions();

        for (Position position : positions) {
            setMargin(position);
        }
    }

    protected void handleSetExitValue(int positionId, BigDecimal exitValue) {

        Position position = getPositionDao().load(positionId);

        // check if new exit-value is higher than old one and greater than 0.0
        if (position != null && exitValue.doubleValue() > 0.0) {

            if (position.getExitValue() != null && exitValue.doubleValue() > position.getExitValue().doubleValue()) {
                logger.warn("exit value " + exitValue + " is higher than existing exit value " + position.getExitValue() + " of " + position.getSecurity().getSymbol());
                return;
            }
            position.setExitValue(exitValue);
            getPositionDao().update(position);

            logger.info("set exit value " + position.getSecurity().getSymbol() + " to " + exitValue);
        }
    }

    @SuppressWarnings("unchecked")
    private void setMargin(Order order) throws Exception {

        Collection<Transaction> transactions = order.getTransactions();

        for (Transaction transaction : transactions) {
            setMargin(transaction.getPosition());
        }
    }
    private void setMargin(Position position) throws Exception {

        StockOption stockOption = (StockOption) position.getSecurity();
        Tick tick = stockOption.getLastTick();
        if (tick != null) {
            double underlayingSpot = stockOption.getUnderlaying().getLastTick().getCurrentValueDouble();

            double marginPerContract = StockOptionUtil.getMargin(stockOption, tick.getSettlementDouble(), underlayingSpot) * stockOption.getContractSize();
            long numberOfContracts = Math.abs(position.getQuantity());
            BigDecimal totalMargin = RoundUtil.getBigDecimal(marginPerContract * numberOfContracts);

            position.setMargin(totalMargin);

            getPositionDao().update(position);

            Account account = position.getAccount();

            int percent = (int)(account.getAvailableAmount().doubleValue() / account.getBalance().doubleValue() * 100d);
            if (account.getAvailableAmount().doubleValue() >= 0) {
                logger.info("set margin for " + stockOption.getSymbol() + " to " + RoundUtil.getBigDecimal(marginPerContract) + " total margin: " + account.getMargin() + " available amount: " + account.getAvailableAmount() + " (" + percent + "% of balance)");
            } else {
                logger.warn("set margin for " + stockOption.getSymbol() + " to " + RoundUtil.getBigDecimal(marginPerContract) + " total margin: " + account.getMargin() + " available amount: " + account.getAvailableAmount() + " (" + percent + "% of balance)");
            }
        } else {
            logger.warn("no last tick available to set margin on " + stockOption.getSymbol());
        }

    }
}
