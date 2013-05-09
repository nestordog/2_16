/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.security.Forex;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.strategy.CashBalance;
import com.algoTrader.entity.strategy.PortfolioValue;
import com.algoTrader.entity.strategy.PortfolioValueDao;
import com.algoTrader.entity.strategy.Strategy;
import com.algoTrader.entity.strategy.StrategyImpl;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.util.collection.DoubleMap;
import com.algoTrader.vo.BalanceVO;
import com.algoTrader.vo.CurrencyAmountVO;
import com.algoTrader.vo.PortfolioValueVO;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class PortfolioServiceImpl extends PortfolioServiceBase {

    private static Logger logger = MyLogger.getLogger(PortfolioServiceImpl.class.getName());

    private @Value("${order.initialMarginMarkup}") double initialMarginMarkup;
    private @Value("#{T(com.algoTrader.enumeration.Currency).fromString('${misc.portfolioBaseCurrency}')}") Currency portfolioBaseCurrency;
    private @Value("${misc.intervalDays}") int intervalDays;

    @Override
    protected BigDecimal handleGetCashBalance() {
        return RoundUtil.getBigDecimal(getCashBalanceDouble());
    }

    @Override
    protected BigDecimal handleGetCashBalance(String strategyName) {
        return RoundUtil.getBigDecimal(getCashBalanceDouble(strategyName));
    }

    @Override
    protected BigDecimal handleGetCashBalance(Date date) {
        return RoundUtil.getBigDecimal(getCashBalanceDouble(date));
    }

    @Override
    protected BigDecimal handleGetCashBalance(String strategyName, Date date) {
        return RoundUtil.getBigDecimal(getCashBalanceDouble(strategyName, date));
    }

    @Override
    protected double handleGetCashBalanceDouble() {

        Collection<CashBalance> cashBalances = getCashBalanceDao().loadAll();

        List<Position> positions = getPositionDao().findOpenFXPositionsAggregated();

        return getCashBalanceDoubleInternal(cashBalances, positions);
    }

    @Override
    protected double handleGetCashBalanceDouble(String strategyName) {

        Collection<CashBalance> cashBalances = getCashBalanceDao().findCashBalancesByStrategy(strategyName);

        List<Position> positions = getPositionDao().findOpenFXPositionsByStrategy(strategyName);

        return getCashBalanceDoubleInternal(cashBalances, positions);
    }

    private double getCashBalanceDoubleInternal(Collection<CashBalance> cashBalances, List<Position> positions) {

        // sum of all cashBalances
        double amount = 0.0;
        for (CashBalance cashBalance : cashBalances) {
            amount += cashBalance.getAmountBaseDouble();
        }

        // sum of all FX positions
        for (Position position : positions) {
            amount += position.getMarketValueBaseDouble();
        }

        return amount;
    }

    @Override
    protected double handleGetCashBalanceDouble(Date date) {

        Collection<Transaction> transactions = getTransactionDao().findByMaxDate(date);

        Collection<Position> openPositions = getPositionDao().findOpenPositionsByMaxDateAggregated(date);

        return getCashBalanceDoubleInternal(transactions, openPositions, date);
    }

    @Override
    protected double handleGetCashBalanceDouble(String strategyName, Date date) {

        Collection<Transaction> transactions = getTransactionDao().findByStrategyAndMaxDate(strategyName, date);

        Collection<Position> openPositions = getPositionDao().findOpenPositionsByStrategyAndMaxDate(strategyName, date);

        return getCashBalanceDoubleInternal(transactions, openPositions, date);
    }

    private double getCashBalanceDoubleInternal(Collection<Transaction> transactions, Collection<Position> openPositions, Date date) {

        // sum of all transactions
        DoubleMap<Currency> map = new DoubleMap<Currency>();
        for (Transaction transaction : transactions) {

            // process all currenyAmounts
            for (CurrencyAmountVO currencyAmount : transaction.getAttributions()) {
                map.increment(currencyAmount.getCurrency(), currencyAmount.getAmount().doubleValue());
            }
        }

        // sum of all FX positions
        for (Position openPosition : openPositions) {

            Security security = openPosition.getSecurityInitialized();
            if (security instanceof Forex) {
                List<Tick> ticks = getTickDao().findTicksBySecurityAndMaxDate(1, 1, security.getId(), date, this.intervalDays);
                if (ticks.isEmpty()) {
                    ticks = getTickDao().findTicksBySecurityAndMinDate(1, 1, security.getId(), date, this.intervalDays);
                    if (ticks.isEmpty()) {
                        logger.warn("no tick available for " + security + " on " + date);
                        continue;
                    }
                    logger.info("no prior tick available on " + date + " next tick is " + ((ticks.get(0).getDateTime().getTime() - date.getTime()) / 86400000.0) + " days later for " + security);
                }

                double amount = openPosition.getQuantity() * ticks.get(0).getMarketValueDouble(openPosition.getDirection());
                map.increment(security.getSecurityFamily().getCurrency(), amount);
            }
        }

        // convert non baseCurrencies
        double amount = 0.0;
        for (Map.Entry<Currency, Double> entry : map.entrySet()) {
            double fxRate = getForexDao().getRateDoubleByDate(entry.getKey(), this.portfolioBaseCurrency, date);
            amount += entry.getValue() * fxRate;
        }
        return amount;
    }

    @Override
    protected BigDecimal handleGetSecuritiesCurrentValue() {
        return RoundUtil.getBigDecimal(getSecuritiesCurrentValueDouble());
    }

    @Override
    protected BigDecimal handleGetSecuritiesCurrentValue(String strategyName) {

        return RoundUtil.getBigDecimal(getSecuritiesCurrentValueDouble(strategyName));
    }

    @Override
    protected BigDecimal handleGetSecuritiesCurrentValue(Date date) {
        return RoundUtil.getBigDecimal(getSecuritiesCurrentValueDouble(date));
    }

    @Override
    protected BigDecimal handleGetSecuritiesCurrentValue(String strategyName, Date date) {

        return RoundUtil.getBigDecimal(getSecuritiesCurrentValueDouble(strategyName, date));
    }

    @Override
    protected double handleGetSecuritiesCurrentValueDouble() {

        Collection<Position> openPositions = getPositionDao().findOpenTradeablePositionsAggregated();

        return getSecuritiesCurrentValueDoubleInternal(openPositions);
    }

    @Override
    protected double handleGetSecuritiesCurrentValueDouble(String strategyName) {

        List<Position> openPositions = getPositionDao().findOpenTradeablePositionsByStrategy(strategyName);

        return getSecuritiesCurrentValueDoubleInternal(openPositions);
    }

    private double getSecuritiesCurrentValueDoubleInternal(Collection<Position> openPositions) {

        // sum of all non-FX positions (FX counts as cash)
        double amount = 0.0;
        for (Position openPosition : openPositions) {

            Security security = openPosition.getSecurityInitialized();
            if (!(security instanceof Forex)) {
                amount += openPosition.getMarketValueBaseDouble();
            }
        }
        return amount;
    }

    @Override
    protected double handleGetSecuritiesCurrentValueDouble(Date date) {

        Collection<Position> openPositions = getPositionDao().findOpenPositionsByMaxDateAggregated(date);

        return getSecuritiesCurrentValueDoubleInternal(openPositions, date);
    }

    @Override
    protected double handleGetSecuritiesCurrentValueDouble(String strategyName, Date date) {

        Collection<Position> openPositions = getPositionDao().findOpenPositionsByStrategyAndMaxDate(strategyName, date);

        return getSecuritiesCurrentValueDoubleInternal(openPositions, date);
    }

    private double getSecuritiesCurrentValueDoubleInternal(Collection<Position> openPositions, Date date) {

        // sum of all non-FX positions (FX counts as cash)
        DoubleMap<Currency> map = new DoubleMap<Currency>();

        for (Position openPosition : openPositions) {

            Security security = openPosition.getSecurityInitialized();
            if (!(security instanceof Forex)) {
                List<Tick> ticks = getTickDao().findTicksBySecurityAndMaxDate(1, 1, security.getId(), date, this.intervalDays);
                if (ticks.isEmpty()) {
                    ticks = getTickDao().findTicksBySecurityAndMinDate(1, 1, security.getId(), date, this.intervalDays);
                    if (ticks.isEmpty()) {
                        logger.warn("no tick available for " + security + " on " + date);
                        continue;
                    }
                    logger.info("no prior tick available on " + date + " next tick is " + ((ticks.get(0).getDateTime().getTime() - date.getTime()) / 86400000.0) + " days later for " + security);
                }

                double marketValue = openPosition.getQuantity() * ticks.get(0).getMarketValueDouble(openPosition.getDirection()) * security.getSecurityFamily().getContractSize();
                map.increment(security.getSecurityFamily().getCurrency(), marketValue);
            }
        }

        double amount = 0.0;
        for (Map.Entry<Currency, Double> entry : map.entrySet()) {
            double fxRate = getForexDao().getRateDoubleByDate(entry.getKey(), this.portfolioBaseCurrency, date);
            amount += entry.getValue() * fxRate;
        }
        return amount;
    }

    @Override
    protected BigDecimal handleGetMaintenanceMargin() {
        return RoundUtil.getBigDecimal(getMaintenanceMarginDouble());
    }

    @Override
    protected BigDecimal handleGetMaintenanceMargin(String strategyName) {
        return RoundUtil.getBigDecimal(getMaintenanceMarginDouble(strategyName));
    }

    @Override
    protected double handleGetMaintenanceMarginDouble() {

        double margin = 0.0;
        Collection<Position> positions = getPositionDao().findOpenTradeablePositions();
        for (Position position : positions) {
            margin += position.getMaintenanceMarginBaseDouble();
        }
        return margin;
    }

    @Override
    protected double handleGetMaintenanceMarginDouble(String strategyName) {

        double margin = 0.0;
        List<Position> positions = getPositionDao().findOpenTradeablePositionsByStrategy(strategyName);
        for (Position position : positions) {
            margin += position.getMaintenanceMarginBaseDouble();
        }
        return margin;
    }

    @Override
    protected BigDecimal handleGetInitialMargin() {

        return RoundUtil.getBigDecimal(getInitialMarginDouble());
    }

    @Override
    protected BigDecimal handleGetInitialMargin(String strategyName) {

        return RoundUtil.getBigDecimal(getInitialMarginDouble(strategyName));
    }

    @Override
    protected double handleGetInitialMarginDouble() {

        return this.initialMarginMarkup * getMaintenanceMarginDouble();
    }

    @Override
    protected double handleGetInitialMarginDouble(String strategyName) {

        return this.initialMarginMarkup * getMaintenanceMarginDouble(strategyName);
    }

    @Override
    protected BigDecimal handleGetNetLiqValue() {
        return RoundUtil.getBigDecimal(getNetLiqValueDouble());
    }

    @Override
    protected BigDecimal handleGetNetLiqValue(String strategyName) {

        return RoundUtil.getBigDecimal(getNetLiqValueDouble(strategyName));
    }

    @Override
    protected double handleGetNetLiqValueDouble() {

        return getCashBalanceDouble() + getSecuritiesCurrentValueDouble();
    }

    @Override
    protected double handleGetNetLiqValueDouble(String strategyName) {

        return getCashBalanceDouble(strategyName) + getSecuritiesCurrentValueDouble(strategyName);
    }

    @Override
    protected BigDecimal handleGetAvailableFunds() {
        return RoundUtil.getBigDecimal(getAvailableFundsDouble());
    }

    @Override
    protected BigDecimal handleGetAvailableFunds(String strategyName) {

        return RoundUtil.getBigDecimal(getAvailableFundsDouble(strategyName));
    }

    @Override
    protected double handleGetAvailableFundsDouble() {

        return getNetLiqValueDouble() - getInitialMarginDouble();
    }

    @Override
    protected double handleGetAvailableFundsDouble(String strategyName) {

        return getNetLiqValueDouble(strategyName) - getInitialMarginDouble(strategyName);
    }

    @Override
    protected double handleGetLeverage() {

        double exposure = 0.0;
        Collection<Position> positions = getPositionDao().findOpenTradeablePositions();
        for (Position position : positions) {
            exposure += position.getExposure();
        }
        return exposure / getNetLiqValueDouble();
    }

    @Override
    protected double handleGetLeverage(String strategyName) {

        double exposure = 0.0;
        List<Position> positions = getPositionDao().findOpenTradeablePositionsByStrategy(strategyName);
        for (Position position : positions) {
            exposure += position.getExposure();
        }

        return exposure / getNetLiqValueDouble(strategyName);
    }

    @Override
    protected double handleGetPerformance() {

        return getPerformance(StrategyImpl.BASE);
    }

    @Override
    protected double handleGetPerformance(String strategyName) {

        Date date = DateUtils.truncate(new Date(), Calendar.MONTH);
        List<PortfolioValueVO> portfolioValues = (List<PortfolioValueVO>) getPortfolioValuesInclPerformanceSinceDate(strategyName, date);

        // the performance of the last portfolioValue represents the performance of the entire timeperiod
        if (portfolioValues.size() > 0) {
            return portfolioValues.get(portfolioValues.size() - 1).getPerformance();
        } else {
            return Double.NaN;
        }
    }

    @Override
    protected PortfolioValue handleGetPortfolioValue() {

        return getPortfolioValue(StrategyImpl.BASE);
    }

    @Override
    protected PortfolioValue handleGetPortfolioValue(String strategyName) {

        Strategy strategy = getStrategyDao().findByName(strategyName);

        BigDecimal cashBalance;
        BigDecimal securitiesCurrentValue;
        BigDecimal maintenanceMargin;
        double leverage;
        if (strategy.isBase()) {
            cashBalance = getCashBalance();
            securitiesCurrentValue = getSecuritiesCurrentValue();
            maintenanceMargin = getMaintenanceMargin();
            leverage = getLeverage();

        } else {
            cashBalance = getCashBalance(strategy.getName());
            securitiesCurrentValue = getSecuritiesCurrentValue(strategy.getName());
            maintenanceMargin = getMaintenanceMargin(strategy.getName());
            leverage = getLeverage(strategy.getName());
        }

        PortfolioValue portfolioValue = PortfolioValue.Factory.newInstance();

        portfolioValue.setStrategy(strategy);
        portfolioValue.setDateTime(DateUtil.getCurrentEPTime());
        portfolioValue.setCashBalance(cashBalance);
        portfolioValue.setSecuritiesCurrentValue(securitiesCurrentValue); // might be null if there was no last tick for a particular security
        portfolioValue.setMaintenanceMargin(maintenanceMargin);
        portfolioValue.setNetLiqValue(securitiesCurrentValue != null ? cashBalance.add(securitiesCurrentValue) : null); // add here to prevent another lookup
        portfolioValue.setLeverage(Double.isNaN(leverage) ? 0 : leverage);
        portfolioValue.setAllocation(strategy.getAllocation());

        return portfolioValue;
    }

    @Override
    protected PortfolioValue handleGetPortfolioValue(String strategyName, Date date) {

        Strategy strategy = getStrategyDao().findByName(strategyName);

        BigDecimal cashBalance;
        BigDecimal securitiesCurrentValue;
        if (strategy.isBase()) {
            cashBalance = getCashBalance(date);
            securitiesCurrentValue = getSecuritiesCurrentValue(date);

        } else {
            cashBalance = getCashBalance(strategy.getName(), date);
            securitiesCurrentValue = getSecuritiesCurrentValue(strategy.getName(), date);
        }

        PortfolioValue portfolioValue = PortfolioValue.Factory.newInstance();

        portfolioValue.setStrategy(strategy);
        portfolioValue.setDateTime(date);
        portfolioValue.setCashBalance(cashBalance);
        portfolioValue.setSecuritiesCurrentValue(securitiesCurrentValue);
        portfolioValue.setNetLiqValue(cashBalance.add(securitiesCurrentValue)); // add here to prevent another lookup
        portfolioValue.setMaintenanceMargin(new BigDecimal(0));

        return portfolioValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Collection<PortfolioValueVO> handleGetPortfolioValuesInclPerformanceSinceDate(String strategyName, Date minDate) {

        Collection<PortfolioValueVO> portfolioValues = (Collection<PortfolioValueVO>) getPortfolioValueDao().findByStrategyAndMinDate(PortfolioValueDao.TRANSFORM_PORTFOLIOVALUEVO, strategyName, minDate);

        // calculate the performance
        double lastNetLiqValue = 0;
        double lastDayNetLiqValue = 0;
        double performance = 1.0;
        double dailyPerformance = 1.0;
        for (PortfolioValueVO portfolioValue : portfolioValues) {

            // for BASE reset performance at the 24:00 based on NetLiqValue of prior day
            if (StrategyImpl.BASE.equals(strategyName) && DateUtils.getFragmentInHours(portfolioValue.getDateTime(), Calendar.DAY_OF_YEAR) == 0) {
                if (lastDayNetLiqValue != 0) {
                    dailyPerformance = dailyPerformance
                            * (portfolioValue.getNetLiqValue().doubleValue() / (lastDayNetLiqValue + (portfolioValue.getCashFlow() != null ? portfolioValue.getCashFlow().doubleValue() : 0)));
                    performance = dailyPerformance;
                    portfolioValue.setPerformance(performance - 1.0);
                }

                lastDayNetLiqValue = portfolioValue.getNetLiqValue().doubleValue();
                lastNetLiqValue = portfolioValue.getNetLiqValue().doubleValue();

            } else {
                if (lastNetLiqValue != 0) {
                    performance = performance
                            * (portfolioValue.getNetLiqValue().doubleValue() / (lastNetLiqValue + (portfolioValue.getCashFlow() != null ? portfolioValue.getCashFlow().doubleValue() : 0)));
                    portfolioValue.setPerformance(performance - 1.0);
                }

                lastNetLiqValue = portfolioValue.getNetLiqValue().doubleValue();
            }
        }

        return portfolioValues;
    }

    @Override
    protected Collection<BalanceVO> handleGetBalances() {

        Collection<Currency> currencies = getCashBalanceDao().findHeldCurrencies();
        DoubleMap<Currency> cashMap = new DoubleMap<Currency>();
        DoubleMap<Currency> securitiesMap = new DoubleMap<Currency>();

        for (Currency currency : currencies) {
            cashMap.increment(currency, 0.0);
            securitiesMap.increment(currency, 0.0);
        }

        // sum of all cashBalances
        Collection<CashBalance> cashBalances = getCashBalanceDao().loadAll();
        for (CashBalance cashBalance : cashBalances) {
            Currency currency = cashBalance.getCurrency();
            cashMap.increment(currency, cashBalance.getAmountDouble());
        }

        // sum of all positions
        Collection<Position> positions = getPositionDao().findOpenTradeablePositionsAggregated();
        for (Position position : positions) {

            position.getSecurityInitialized();
            CurrencyAmountVO currencyAmount = position.getAttribution();
            if (position.isCashPosition()) {
                cashMap.increment(currencyAmount.getCurrency(), currencyAmount.getAmount().doubleValue());
            } else {
                securitiesMap.increment(currencyAmount.getCurrency(), currencyAmount.getAmount().doubleValue());
            }
        }

        List<BalanceVO> balances = new ArrayList<BalanceVO>();
        for (Currency currency : currencies) {

            double cash = cashMap.get(currency);
            double securities = securitiesMap.get(currency);
            double netLiqValue = cash + securities;
            double exchangeRate = ServiceLocator.instance().getLookupService().getForexRateDouble(currency, this.portfolioBaseCurrency);
            double cashBase = cash * exchangeRate;
            double securitiesBase = securities * exchangeRate;
            double netLiqValueBase = netLiqValue * exchangeRate;

            BalanceVO balance = new BalanceVO();
            balance.setCurrency(currency);
            balance.setCash(RoundUtil.getBigDecimal(cash));
            balance.setSecurities(RoundUtil.getBigDecimal(securities));
            balance.setNetLiqValue(RoundUtil.getBigDecimal(netLiqValue));
            balance.setCashBase(RoundUtil.getBigDecimal(cashBase));
            balance.setSecuritiesBase(RoundUtil.getBigDecimal(securitiesBase));
            balance.setNetLiqValueBase(RoundUtil.getBigDecimal(netLiqValueBase));
            balance.setExchangeRate(exchangeRate);

            balances.add(balance);
        }

        return balances;
    }
}
