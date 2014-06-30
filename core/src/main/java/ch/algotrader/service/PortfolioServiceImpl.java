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
package ch.algotrader.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.strategy.CashBalance;
import ch.algotrader.entity.strategy.PortfolioValue;
import ch.algotrader.entity.strategy.PortfolioValueDao;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.util.DateUtil;
import ch.algotrader.util.MyLogger;
import ch.algotrader.util.RoundUtil;
import ch.algotrader.util.collection.DoubleMap;
import ch.algotrader.vo.BalanceVO;
import ch.algotrader.vo.CurrencyAmountVO;
import ch.algotrader.vo.PortfolioValueVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class PortfolioServiceImpl extends PortfolioServiceBase {

    private static Logger logger = MyLogger.getLogger(PortfolioServiceImpl.class.getName());

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

    @SuppressWarnings("rawtypes")
    @Override
    protected BigDecimal handleGetCashBalance(String filter, Map namedParameters, Date date) {
        return RoundUtil.getBigDecimal(getCashBalanceDouble(filter, namedParameters, date));
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
            amount += position.getMarketValueBase();
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected double handleGetCashBalanceDouble(String filter, Map namedParameters, Date date) {

        namedParameters.put("maxDate", date);

        //@formatter:off
        String query =
                "from TransactionImpl as t "
                + "where " + filter + " "
                + "and t.dateTime <= :maxDate";

        Collection<Transaction> transactions = (Collection<Transaction>) getGenericDao().find(query, namedParameters);

        return getCashBalanceDoubleInternal(transactions, new ArrayList<Position>(), date);
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
                int intervalDays = getCoreConfig().getIntervalDays();
                List<Tick> ticks = getTickDao().findTicksBySecurityAndMaxDate(1, 1, security.getId(), date, intervalDays);
                if (ticks.isEmpty()) {
                    ticks = getTickDao().findTicksBySecurityAndMinDate(1, 1, security.getId(), date, intervalDays);
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
            double fxRate = getForexDao().getRateDoubleByDate(entry.getKey(), getCommonConfig().getPortfolioBaseCurrency(), date);
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

    @SuppressWarnings("rawtypes")
    @Override
    protected BigDecimal handleGetSecuritiesCurrentValue(String filter, Map namedParameters, Date date) throws Exception {

        return RoundUtil.getBigDecimal(getSecuritiesCurrentValueDouble(filter, namedParameters, date));
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
                amount += openPosition.getMarketValueBase();
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected double handleGetSecuritiesCurrentValueDouble(String filter, Map namedParameters, Date date) {

        //@formatter:off
        String queryString =
                "select new Position(sum(t.quantity), s) "
                + "from TransactionImpl as t "
                + "join t.security as s "
                + "where s != null "
                + "and t.dateTime <= :maxDate "
                + "and " + filter + " "
                + "group by s.id "
                + "having sum(t.quantity) != 0 "
                + "order by s.id";
        //@formatter:on

        namedParameters.put("maxDate", date);

        List<Position> openPositions = (List<Position>) getGenericDao().find(queryString, namedParameters);

        return getSecuritiesCurrentValueDoubleInternal(openPositions, date);
    }

    private double getSecuritiesCurrentValueDoubleInternal(Collection<Position> openPositions, Date date) {

        // sum of all non-FX positions (FX counts as cash)
        DoubleMap<Currency> map = new DoubleMap<Currency>();

        for (Position openPosition : openPositions) {

            Security security = openPosition.getSecurityInitialized();
            if (!(security instanceof Forex)) {
                int intervalDays = getCoreConfig().getIntervalDays();
                List<Tick> ticks = getTickDao().findTicksBySecurityAndMaxDate(1, 1, security.getId(), date, intervalDays);
                if (ticks.isEmpty()) {
                    ticks = getTickDao().findTicksBySecurityAndMinDate(1, 1, security.getId(), date, intervalDays);
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
            double fxRate = getForexDao().getRateDoubleByDate(entry.getKey(), getCommonConfig().getPortfolioBaseCurrency(), date);
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

        return getCommonConfig().getInitialMarginMarkup().doubleValue() * getMaintenanceMarginDouble();
    }

    @Override
    protected double handleGetInitialMarginDouble(String strategyName) {

        return getCommonConfig().getInitialMarginMarkup().doubleValue() * getMaintenanceMarginDouble(strategyName);
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
        Collection<CashBalance> cashBalances = getCashBalanceDao().loadAll();
        Collection<Position> positions = getPositionDao().findOpenTradeablePositionsAggregated();

        return getBalances(currencies, cashBalances, positions);
    }

    @Override
    protected Collection<BalanceVO> handleGetBalances(String strategyName) {

        Collection<Currency> currencies = getCashBalanceDao().findHeldCurrenciesByStrategy(strategyName);
        Collection<CashBalance> cashBalances = getCashBalanceDao().findCashBalancesByStrategy(strategyName);
        Collection<Position> positions = getPositionDao().findOpenTradeablePositionsByStrategy(strategyName);

        return getBalances(currencies, cashBalances, positions);
    }

    private List<BalanceVO> getBalances(Collection<Currency> currencies, Collection<CashBalance> cashBalances, Collection<Position> positions) {

        DoubleMap<Currency> cashMap = new DoubleMap<Currency>();
        DoubleMap<Currency> securitiesMap = new DoubleMap<Currency>();

        for (Currency currency : currencies) {
            cashMap.increment(currency, 0.0);
            securitiesMap.increment(currency, 0.0);
        }

        // sum of all cashBalances
        for (CashBalance cashBalance : cashBalances) {
            Currency currency = cashBalance.getCurrency();
            cashMap.increment(currency, cashBalance.getAmountDouble());
        }

        // sum of all positions
        for (Position position : positions) {

            position.getSecurityInitialized();
            CurrencyAmountVO currencyAmount = position.getAttribution();
            if (currencyAmount.getAmount() != null) {
                if (position.isCashPosition()) {
                    cashMap.increment(currencyAmount.getCurrency(), currencyAmount.getAmount().doubleValue());
                } else {
                    securitiesMap.increment(currencyAmount.getCurrency(), currencyAmount.getAmount().doubleValue());
                }
            }
        }

        List<BalanceVO> balances = new ArrayList<BalanceVO>();
        for (Currency currency : currencies) {

            double cash = cashMap.get(currency);
            double securities = securitiesMap.get(currency);
            double netLiqValue = cash + securities;
            double exchangeRate = ServiceLocator.instance().getLookupService().getForexRateDouble(currency, getCommonConfig().getPortfolioBaseCurrency());
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
