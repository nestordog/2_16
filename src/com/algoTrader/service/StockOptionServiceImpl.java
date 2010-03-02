package com.algoTrader.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.algoTrader.criteria.StockOptionCriteria;
import com.algoTrader.entity.Account;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.StockOptionImpl;
import com.algoTrader.entity.Tick;
import com.algoTrader.entity.Transaction;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.Market;
import com.algoTrader.enumeration.OptionType;
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
    private static int contractSize = Integer.parseInt(PropertiesUtil.getProperty("simulation.contractSize"));
    private static boolean simulation = new Boolean(PropertiesUtil.getProperty("simulation")).booleanValue();
    private static int minAge = Integer.parseInt(PropertiesUtil.getProperty("minAge"));

    private static Logger logger = MyLogger.getLogger(StockOptionServiceImpl.class.getName());

    private long FORTY_FIVE_DAYS = 3888000000l;

    protected StockOption handleGetStockOption(int securityId, BigDecimal spot) throws Exception {

        Security underlaying = getSecurityDao().load(securityId);

        Date targetExpirationDate = new Date(DateUtil.getCurrentEPTime().getTime() + minAge);

        StockOption stockOption = findNearestStockOption(underlaying, targetExpirationDate, spot, optionType);

        if (simulation) {
            if ((stockOption == null) || ( stockOption.getExpiration().getTime() > (targetExpirationDate.getTime() + FORTY_FIVE_DAYS ))) {
                stockOption = createDummyStockOption(underlaying, targetExpirationDate, spot, optionType);

                getStockOptionDao().create(stockOption);
            }
        }
        return stockOption;
    }

    private StockOption createDummyStockOption(Security underlaying, Date expiration, BigDecimal strike, OptionType type) throws Exception {

        // set third Friday of the month
        expiration = DateUtil.getNextThirdFriday(expiration);

        // round to 50.-
        strike = StockOptionUtil.roundTo50(strike);

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

    private StockOption findNearestStockOption(Security underlaying, Date expiration, BigDecimal strike,
            OptionType type) throws Exception {

           StockOptionCriteria criteria = new StockOptionCriteria(underlaying, expiration, strike, type);
           criteria.setMaximumResultSize(new Integer(1));

           return (StockOption)getStockOptionDao().findByCriteria(criteria).get(0);
    }

    protected void handleOpenPosition(int securityId, BigDecimal settlement, BigDecimal currentValue, BigDecimal underlaying) throws Exception {

        StockOption stockOption = (StockOption)getStockOptionDao().load(securityId);

        Account account = getAccountDao().findByCurrency(stockOption.getCurrency());

        double availableAmount = account.getAvailableAmount().doubleValue();
        double margin = StockOptionUtil.getMargin(stockOption, settlement, underlaying).doubleValue();
        double currentDouble = currentValue.doubleValue();
        int contractSize = stockOption.getContractSize();

        int numberOfContracts = (int)((availableAmount / (margin - currentDouble)) / contractSize); // i.e. 2 (for 20 stockOptions)
        BigDecimal currentValuePerContract =  RoundUtil.getBigDecimal(currentDouble * contractSize); // CHF 160.- per contract (= CHF 16 per stockOptions)

        Transaction transaction = getTransactionService().executeTransaction(numberOfContracts, stockOption, currentValuePerContract, TransactionType.SELL);

        setMargin(transaction.getPosition());
    }

    protected void handleClosePosition(int positionId) throws Exception {

        Position position = getPositionDao().load(positionId);

        StockOption stockOption = (StockOption)position.getSecurity();
        double currentDouble = stockOption.getLastTick().getCurrentValue().doubleValue();
        int contractSize = stockOption.getContractSize();

        int numberOfContracts = Math.abs(position.getQuantity());
        BigDecimal currentValuePerContract =  RoundUtil.getBigDecimal(currentDouble * contractSize); // CHF 160.- per contract (= CHF 16 per stockOptions)

        getTransactionService().executeTransaction(numberOfContracts, stockOption, currentValuePerContract, TransactionType.BUY);

        getWatchlistService().removeFromWatchlist(stockOption);
    }

    protected void handleExpireStockOption(int positionId) throws Exception {

        Position position = getPositionDao().load(positionId);

        StockOption stockOption = (StockOption)position.getSecurity();
        double currentDouble = stockOption.getLastTick().getCurrentValue().doubleValue();
        int contractSize = stockOption.getContractSize();

        int numberOfContracts = Math.abs(position.getQuantity());
        BigDecimal currentValuePerContract =  RoundUtil.getBigDecimal(currentDouble * contractSize); // CHF 160.- per contract (= CHF 16 per stockOptions)

        getTransactionService().executeTransaction(numberOfContracts, stockOption, currentValuePerContract, TransactionType.EXPIRATION);

        getWatchlistService().removeFromWatchlist(stockOption);
    }

    protected void handleSetMargins() throws Exception {

        List list = getPositionDao().findOpenPositions();

        for (Iterator it = list.iterator(); it.hasNext(); ) {

            Position position = (Position)it.next();
            setMargin(position);
        }
    }

    private void setMargin(Position position) throws Exception {

        if (position.getSecurity() instanceof StockOption) {

            StockOption stockOption = (StockOption) position.getSecurity();
            Tick tick = stockOption.getLastTick();
            if (tick != null) {
                BigDecimal settlement = tick.getSettlement();
                BigDecimal underlaying = stockOption.getUnderlaying().getLastTick().getCurrentValue();

                double marginPerContract = StockOptionUtil.getMargin(stockOption, settlement, underlaying).doubleValue() * stockOption.getContractSize();
                int numberOfContracts = Math.abs(position.getQuantity());
                BigDecimal totalMargin = RoundUtil.getBigDecimal(marginPerContract * numberOfContracts);

                position.setMargin(totalMargin);

                getPositionDao().update(position);

                Account account = position.getAccount();

                if (account.getAvailableAmount().doubleValue() >= 0) {
                    logger.info("set margin for " + stockOption.getSymbol() + " to " + RoundUtil.getBigDecimal(marginPerContract) + " total margin: " + account.getMargin() + " available amount: " + account.getAvailableAmount());
                } else {
                    int percent = (int)(-account.getAvailableAmount().doubleValue() / account.getBalance().doubleValue() * 100d);
                    logger.warn("set margin for " + stockOption.getSymbol() + " to " + RoundUtil.getBigDecimal(marginPerContract) + " total margin: " + account.getMargin() + " available amount: " + account.getAvailableAmount() + " (" + percent + "% of balance)");
                }
            } else {
                logger.warn("no last tick available to set margin on " + stockOption.getSymbol());
            }
        }
    }

    protected void handleSetExitValue(int positionId, BigDecimal exitValue) {

        Position position = getPositionDao().load(positionId);
        position.setExitValue(exitValue);
        getPositionDao().update(position);

        logger.info("set exit value " + position.getSecurity().getSymbol() + " to " + exitValue);
    }
}
