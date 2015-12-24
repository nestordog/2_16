/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.simulation;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.multimap.MultiHashMap;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.accounting.PositionTracker;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.marketData.MarketDataEventVO;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.strategy.CashBalance;
import ch.algotrader.entity.strategy.PortfolioValue;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.LimitOrderI;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.enumeration.TransactionType;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.event.dispatch.EventRecipient;
import ch.algotrader.service.MarketDataCache;
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
 */
public class Simulator {

    private static final Logger LOGGER = LogManager.getLogger(Simulator.class);

    private final Map<Pair<String, Currency>, CashBalance> cashBalances;
    private final Map<Pair<String, Security>, Position> positionsByStrategyAndSecurity;
    private final MultiMap<String, Position> positionsByStrategy;
    private final MultiMap<Security, Position> positionsBySecurity;

    private final MarketDataCache marketDataCache;
    private final PositionTracker positionTracker;
    private final EventDispatcher eventDispatcher;
    private final EngineManager engineManager;

    public Simulator(final MarketDataCache marketDataCache, final PositionTracker positionTracker, final EventDispatcher eventDispatcher, final EngineManager engineManager) {

        Validate.notNull(marketDataCache, "MarketDataCache is null");
        Validate.notNull(positionTracker, "PositionTracker is null");
        Validate.notNull(eventDispatcher, "EventDispatcher is null");
        Validate.notNull(engineManager, "EngineManager is null");

        this.marketDataCache = marketDataCache;
        this.positionTracker = positionTracker;
        this.eventDispatcher = eventDispatcher;
        this.engineManager = engineManager;

        this.cashBalances = new HashMap<>();
        this.positionsByStrategyAndSecurity = new HashMap<>();
        this.positionsByStrategy = new MultiHashMap<>();
        this.positionsBySecurity = new MultiHashMap<>();
    }

    protected MarketDataCache getMarketDataCache() {
        return this.marketDataCache;
    }

    public void clear() {
        this.positionsByStrategyAndSecurity.clear();
        this.positionsByStrategy.clear();
        this.positionsBySecurity.clear();
        this.cashBalances.clear();
    }

    public void createCashBalance(final String strategyName, final Currency currency, final BigDecimal amount) {

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

    public void sendOrder(final Order order) {

        Strategy strategy = order.getStrategy();
        Validate.notNull(strategy, "missing strategy for order " + order);
        Security security = order.getSecurity();
        Validate.notNull(security, "missing security for order " + order);

        final Date now = getCurrentTime();
        if (order.getDateTime() == null) {
            order.setDateTime(now);
        }

        if (order.getExchange() == null) {
            order.setExchange(security.getSecurityFamily().getExchange());
        }

        // propagate order
        this.eventDispatcher.sendEvent(strategy.getName(), order.convertToVO());

        // get the price
        BigDecimal price;
        if (order instanceof LimitOrderI) {

            // limit orders are executed at their limit price
            price = ((LimitOrderI) order).getLimit();
        } else {
            throw new UnsupportedOperationException("only LimitOrders allowed at this time");
        }

        // create one fill per order
        Fill fill = new Fill();
        fill.setDateTime(order.getDateTime());
        fill.setSide(order.getSide());
        fill.setQuantity(order.getQuantity());
        fill.setPrice(price);
        fill.setOrder(order);

        // propagate order
        this.eventDispatcher.sendEvent(strategy.getName(), fill);

        // create and OrderStatus
        OrderStatus orderStatus = OrderStatus.Factory.newInstance();
        orderStatus.setIntId(order.getIntId());
        orderStatus.setDateTime(getCurrentTime());
        orderStatus.setStatus(Status.EXECUTED);
        orderStatus.setFilledQuantity(order.getQuantity());
        orderStatus.setRemainingQuantity(0);
        orderStatus.setOrder(order);
        orderStatus.setAvgPrice(price);

        // propagate order status
        this.eventDispatcher.sendEvent(strategy.getName(), orderStatus.convertToVO());

        // create the transaction
        createTransaction(fill);
    }

    protected Transaction createTransaction(final Fill fill) {

        Order order = fill.getOrder();
        Security security = order.getSecurity();
        Strategy strategy = order.getStrategy();

        SecurityFamily securityFamily = security.getSecurityFamily();
        TransactionType transactionType = Side.BUY.equals(fill.getSide()) ? TransactionType.BUY : TransactionType.SELL;
        long quantity = Side.BUY.equals(fill.getSide()) ? fill.getQuantity() : -fill.getQuantity();

        BigDecimal executionCommission = getExecutionCommission(order, fill);
        BigDecimal clearingCommission = getClearingCommission(order, fill);

        Transaction transaction = Transaction.Factory.newInstance();
        transaction.setUuid(UUID.randomUUID().toString());
        transaction.setDateTime(fill.getDateTime());
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

    protected BigDecimal getClearingCommission(final Order order, final Fill fill) {

        double clearingCommissionPerContract = order.getSecurity().getSecurityFamily().getClearingCommission(order.getAccount().getBroker()).doubleValue();
        return RoundUtil.getBigDecimal(Math.abs(clearingCommissionPerContract * Math.abs(fill.getQuantity())));
    }


    protected BigDecimal getExecutionCommission(final Order order, final Fill fill) {

        double executionCommissionPerContract = order.getSecurity().getSecurityFamily().getExecutionCommission(order.getAccount().getBroker()).doubleValue();
        return RoundUtil.getBigDecimal(Math.abs(executionCommissionPerContract * Math.abs(fill.getQuantity())));
    }

    /**
     * @copy ch.algotrader.service.TransactionServiceImpl.handlePersistTransaction(Transaction)
     */
    protected Position persistTransaction(final Transaction transaction) {

        TradePerformanceVO tradePerformance = null;

        Strategy strategy = transaction.getStrategy();
        Position position = findPositionByStrategyAndSecurity(strategy.getName(), transaction.getSecurity());
        if (position == null) {

            position = this.positionTracker.processFirstTransaction(transaction);

            // associate strategy
            position.setStrategy(strategy);

            createPosition(position);

            // associate reverse-relations (after position has received an id)
            transaction.setPosition(position);

        } else {

            // process the transaction (adjust quantity, cost and realizedPL)
            tradePerformance = this.positionTracker.processTransaction(position, transaction);

            // associate the position
            transaction.setPosition(position);
        }

        this.eventDispatcher.sendEvent(strategy.getName(), position.convertToVO());

        // add the amount to the corresponding cashBalance
        processTransaction(transaction);

        // propagate Transaction
        this.eventDispatcher.sendEvent(strategy.getName(), transaction.convertToVO());

        // propagate tradePerformance
        if (tradePerformance != null && tradePerformance.getProfit() != 0.0) {

            // propagate the TradePerformance event
            this.eventDispatcher.broadcast(tradePerformance, EventRecipient.SERVER_ENGINE_ONLY);
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("executed transaction: {}", transaction);
        }
        return position;
    }

    /**
     * @copy ch.algotrader.service.CashBalanceServiceImpl.handleProcessTransaction(Transaction)
     */
    private void processTransaction(final Transaction transaction) {

        // process all currenyAmounts
        for (CurrencyAmountVO currencyAmount : transaction.getAttributions()) {
            processAmount(transaction.getStrategy(), currencyAmount);
        }
    }

    /**
     * @copy ch.algotrader.service.CashBalanceServiceImpl.handleProcessAmount(String, CurrencyAmountVO)
     */
    private void processAmount(final Strategy strategy, final CurrencyAmountVO currencyAmount) {

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

    protected void createPosition(final Position position) {

        String name = position.getStrategy().getName();
        Security security = position.getSecurity();

        this.positionsByStrategyAndSecurity.put(new Pair<>(name,security), position);
        this.positionsByStrategy.put(name, position);
        this.positionsBySecurity.put(security, position);
    }

    private void createCashBalance(final CashBalance cashBalance) {
        this.cashBalances.put(new Pair<>(cashBalance.getStrategy().getName(), cashBalance.getCurrency()), cashBalance);
    }

    public Collection<Position> findAllPositions() {
        return this.positionsByStrategy.values();
    }

    public Collection<Position> findOpenPositions() {
        return CollectionUtils.select(this.positionsByStrategy.values(), entity -> entity.getQuantity() != 0);
    }
    public Position findPositionByStrategyAndSecurity(final String strategyName, final Security security) {
        return this.positionsByStrategyAndSecurity.get(new Pair<>(strategyName, security));
    }

    public Collection<Position> findPositionsByStrategy(final String strategyName) {
        return this.positionsByStrategy.get(strategyName);
    }

    public Collection<Position> findPositionsBySecurity(final Security security) {
        return this.positionsBySecurity.get(security);
    }

    public CashBalance findCashBalanceByStrategyAndCurrency(final String strategyName, final Currency currency) {
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
        return this.engineManager.getCurrentEPTime();
    }

    protected PositionTracker getPositionTracker() {
        return this.positionTracker;
    }

    /**
     * @copy ch.algotrader.service.PortfolioServiceImpl.getSecuritiesCurrentValueDoubleInternal(Collection<Position>)
     */
    private double getSecuritiesCurrentValueDoubleInternal(final Collection<Position> openPositions) {

        // sum of all positions
        double amount = 0.0;
        for (Position openPosition : openPositions) {
            final MarketDataEventVO marketDataEvent = this.marketDataCache.getCurrentMarketDataEvent(openPosition.getSecurity().getId());
            amount += openPosition.getMarketValue(marketDataEvent).doubleValue();
        }
        return amount;
    }

    /**
     * @copy ch.algotrader.service.PortfolioServiceImpl.getCashBalanceDoubleInternal(Collection<CashBalance>, List<Position>)
     */
    private double getCashBalanceDoubleInternal(final Collection<CashBalance> cashBalances) {

        // sum of all cashBalances
        double amount = 0.0;
        for (CashBalance cashBalance : cashBalances) {
            amount += cashBalance.getAmount().doubleValue();
        }

        return amount;
    }
}
