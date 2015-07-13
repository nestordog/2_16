/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.simulation;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.multimap.MultiHashMap;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.ServiceLocator;
import ch.algotrader.accounting.PositionTracker;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.marketData.MarketDataEvent;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.strategy.CashBalance;
import ch.algotrader.entity.strategy.PortfolioValue;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.LimitOrderI;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.TransactionType;
import ch.algotrader.service.LocalLookupService;
import ch.algotrader.util.RoundUtil;
import ch.algotrader.util.collection.Pair;
import ch.algotrader.vo.CurrencyAmountVO;
import ch.algotrader.vo.TradePerformanceVO;

/**
 * Utility that can be used by strategies during in-memory simulations. It
 * provides similar functionality to the standard position, cash balance and
 * transaction management but is caclulated in memory and not persisted to the
 * database for performance reasons.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class Simulator {

    private static final Logger LOGGER = LogManager.getLogger(Simulator.class);

    private final Map<Pair<String, Currency>, CashBalance> cashBalances;
    private final Map<Pair<String, Security>, Position> positionsByStrategyAndSecurity;
    private final MultiMap<String, Position> positionsByStrategy;
    private final MultiMap<Security, Position> positionsBySecurity;

    private final LocalLookupService localLookupService;
    private final PositionTracker positionTracker;

    public Simulator(final LocalLookupService localLookupService, final PositionTracker positionTracker) {

        Validate.notNull(localLookupService, "LocalLookupService is null");
        Validate.notNull(positionTracker, "PositionTracker is null");

        this.localLookupService = localLookupService;
        this.positionTracker = positionTracker;

        this.cashBalances = new HashMap<>();
        this.positionsByStrategyAndSecurity = new HashMap<>();
        this.positionsByStrategy = new MultiHashMap<>();
        this.positionsBySecurity = new MultiHashMap<>();
    }

    protected LocalLookupService getLocalLookupService() {
        return this.localLookupService;
    }

    public void clear() {
        this.positionsByStrategyAndSecurity.clear();
        this.positionsByStrategy.clear();
        this.positionsBySecurity.clear();
        this.cashBalances.clear();
    }

    public void createCashBalance(String strategyName, Currency currency, BigDecimal amount) {

        if (findCashBalanceByStrategyAndCurrency(strategyName, currency) != null) {
            throw new IllegalStateException("cashBalance already exists");
        }

        Strategy strategy = Strategy.Factory.newInstance();
        strategy.setName(strategyName);

        CashBalance cashBalance = CashBalance.Factory.newInstance(currency, amount, strategy);

        createCashBalance(cashBalance);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("created cashBalance: {}", cashBalance);
        }
    }

    public void sendOrder(Order order) {

        // validate strategy and security
        Validate.notNull(order.getStrategy(), "missing strategy for order " + order);
        Validate.notNull(order.getSecurity(), "missing security for order " + order);

        // get the price
        BigDecimal price = new BigDecimal(0);
        if (order instanceof LimitOrderI) {

            // limitorders are executed at their limit price
            price = ((LimitOrderI) order).getLimit();
        } else {
            throw new UnsupportedOperationException("only MarketOrders allowed at this time");
        }

        // create one fill per order
        Fill fill = new Fill();
        fill.setSide(order.getSide());
        fill.setQuantity(order.getQuantity());
        fill.setPrice(price);
        fill.setOrder(order);

        // create the transaction
        createTransaction(fill);
    }

    protected Transaction createTransaction(Fill fill) {

        Order order = fill.getOrder();
        Security security = order.getSecurity();
        Strategy strategy = order.getStrategy();

        SecurityFamily securityFamily = security.getSecurityFamily();
        TransactionType transactionType = Side.BUY.equals(fill.getSide()) ? TransactionType.BUY : TransactionType.SELL;
        long quantity = Side.BUY.equals(fill.getSide()) ? fill.getQuantity() : -fill.getQuantity();

        BigDecimal executionCommission = getExecutionCommission(fill);
        BigDecimal clearingCommission = getClearingCommission(fill);

        Transaction transaction = Transaction.Factory.newInstance();
        transaction.setUuid(UUID.randomUUID().toString());
        transaction.setDateTime(fill.getExtDateTime());
        transaction.setExtId(fill.getExtId());
        transaction.setQuantity(quantity);
        transaction.setPrice(fill.getPrice());
        transaction.setType(transactionType);
        transaction.setSecurity(security);
        transaction.setStrategy(strategy);
        transaction.setCurrency(securityFamily.getCurrency());
        transaction.setExecutionCommission(executionCommission);
        transaction.setClearingCommission(clearingCommission);
        transaction.setAccount(order.getAccount());

        persistTransaction(transaction);

        return transaction;
    }

    protected BigDecimal getClearingCommission(Fill fill) {

        double clearingCommissionPerContract = fill.getOrder().getSecurity().getSecurityFamily().getClearingCommission(fill.getOrder().getAccount().getBroker()).doubleValue();
        return RoundUtil.getBigDecimal(Math.abs(clearingCommissionPerContract * Math.abs(fill.getQuantity())));
    }


    protected BigDecimal getExecutionCommission(Fill fill) {

        double executionCommissionPerContract = fill.getOrder().getSecurity().getSecurityFamily().getExecutionCommission(fill.getOrder().getAccount().getBroker()).doubleValue();
        return RoundUtil.getBigDecimal(Math.abs(executionCommissionPerContract * Math.abs(fill.getQuantity())));
    }

    /**
     * @param reason
     * @copy ch.algotrader.service.TransactionServiceImpl.handlePersistTransaction(Transaction)
     */
    protected Position persistTransaction(Transaction transaction) {

        TradePerformanceVO tradePerformance = null;

        // position handling (incl ClosePositionVO and TradePerformanceVO)

        // create a new position if necessary
        Position position = findPositionByStrategyAndSecurity(transaction.getStrategy().getName(), transaction.getSecurity());
        if (position == null) {

            position = this.positionTracker.processFirstTransaction(transaction);

            // associate strategy
            position.setStrategy(transaction.getStrategy());

            createPosition(position);

            // associate reverse-relations (after position has received an id)
            transaction.setPosition(position);

        } else {

            // process the transaction (adjust quantity, cost and realizedPL)
            tradePerformance = this.positionTracker.processTransaction(position, transaction);

            // associate the position
            transaction.setPosition(position);
        }

        // add the amount to the corresponding cashBalance
        processTransaction(transaction);

        // propagate tradePerformance
        if (tradePerformance != null && tradePerformance.getProfit() != 0.0) {

            // propagate the TradePerformance event
            sendEvent(tradePerformance);
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("executed transaction: {}", transaction);
        }
        return position;
    }

    protected void sendEvent(TradePerformanceVO tradePerformance) {
        ServiceLocator.instance().getEventDispatcher().broadcast(tradePerformance);
    }

    /**
     * @copy ch.algotrader.service.CashBalanceServiceImpl.handleProcessTransaction(Transaction)
     */
    private void processTransaction(Transaction transaction) {

        // process all currenyAmounts
        for (CurrencyAmountVO currencyAmount : transaction.getAttributions()) {
            processAmount(transaction.getStrategy(), currencyAmount);
        }
    }

    /**
     * @copy ch.algotrader.service.CashBalanceServiceImpl.handleProcessAmount(String, CurrencyAmountVO)
     */
    private void processAmount(Strategy strategy, CurrencyAmountVO currencyAmount) {

        CashBalance cashBalance = findCashBalanceByStrategyAndCurrency(strategy.getName(), currencyAmount.getCurrency());

        // create the cashBalance, if it does not exist yet
        if (cashBalance == null) {

            cashBalance = CashBalance.Factory.newInstance();

            // associate currency, amount and strategy
            cashBalance.setCurrency(currencyAmount.getCurrency());
            cashBalance.setAmount(currencyAmount.getAmount());
            cashBalance.setStrategy(strategy);

            createCashBalance(cashBalance);

        } else {

            cashBalance.setAmount(cashBalance.getAmount().add(currencyAmount.getAmount()));
        }
    }

    protected void createPosition(Position position) {

        String name = position.getStrategy().getName();
        Security security = position.getSecurity();

        this.positionsByStrategyAndSecurity.put(new Pair<>(name,security), position);
        this.positionsByStrategy.put(name, position);
        this.positionsBySecurity.put(security, position);
    }

    private void createCashBalance(CashBalance cashBalance) {
        this.cashBalances.put(new Pair<>(cashBalance.getStrategy().getName(), cashBalance.getCurrency()), cashBalance);
    }

    public Collection<Position> findAllPositions() {
        return this.positionsByStrategy.values();
    }

    public Position findPositionByStrategyAndSecurity(String strategyName, Security security) {
        return this.positionsByStrategyAndSecurity.get(new Pair<>(strategyName, security));
    }

    public Collection<Position> findPositionsByStrategy(String strategyName) {
        return this.positionsByStrategy.get(strategyName);
    }

    public Collection<Position> findPositionsBySecurity(Security security) {
        return this.positionsBySecurity.get(security);
    }

    public CashBalance findCashBalanceByStrategyAndCurrency(String strategyName, Currency currency) {
        return this.cashBalances.get(new Pair<>(strategyName, currency));
    }

    /**
     * @copy ch.algotrader.service.PortfolioServiceImpl.handleGetPortfolioValue(String)
     */
    public PortfolioValue getPortfolioValue() {

        BigDecimal cashBalance = RoundUtil.getBigDecimal(getCashBalanceDoubleInternal(this.cashBalances.values()));
        BigDecimal securitiesCurrentValue = RoundUtil.getBigDecimal(getSecuritiesCurrentValueDoubleInternal(this.positionsByStrategyAndSecurity.values()));

        PortfolioValue portfolioValue = PortfolioValue.Factory.newInstance();

        portfolioValue.setDateTime(getCurrentTime());
        portfolioValue.setCashBalance(cashBalance);
        portfolioValue.setSecuritiesCurrentValue(securitiesCurrentValue); // might be null if there was no last tick for a particular security
        portfolioValue.setNetLiqValue(securitiesCurrentValue != null ? cashBalance.add(securitiesCurrentValue) : null); // add here to prevent another lookup

        return portfolioValue;
    }

    protected Date getCurrentTime() {
        return ServiceLocator.instance().getEngineManager().getCurrentEPTime();
    }

    protected PositionTracker getPositionTracker() {
        return this.positionTracker;
    }

    /**
     * @copy ch.algotrader.service.PortfolioServiceImpl.getSecuritiesCurrentValueDoubleInternal(Collection<Position>)
     */
    private double getSecuritiesCurrentValueDoubleInternal(Collection<Position> openPositions) {

        // sum of all positions
        double amount = 0.0;
        for (Position openPosition : openPositions) {
            final MarketDataEvent marketDataEvent = this.localLookupService.getCurrentMarketDataEvent(openPosition.getSecurity().getId());
            amount += openPosition.getMarketValue(marketDataEvent);
        }
        return amount;
    }

    /**
     * @copy ch.algotrader.service.PortfolioServiceImpl.getCashBalanceDoubleInternal(Collection<CashBalance>, List<Position>)
     */
    private double getCashBalanceDoubleInternal(Collection<CashBalance> cashBalances) {

        // sum of all cashBalances
        double amount = 0.0;
        for (CashBalance cashBalance : cashBalances) {
            amount += cashBalance.getAmount().doubleValue();
        }

        return amount;
    }
}
