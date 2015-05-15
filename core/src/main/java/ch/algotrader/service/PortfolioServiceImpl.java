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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.collections15.keyvalue.MultiKey;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.CoreConfig;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.PositionDao;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.TransactionDao;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.marketData.TickDao;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexDao;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.strategy.CashBalance;
import ch.algotrader.entity.strategy.CashBalanceDao;
import ch.algotrader.entity.strategy.PortfolioValue;
import ch.algotrader.entity.strategy.PortfolioValueDao;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyDao;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.hibernate.GenericDao;
import ch.algotrader.util.DateUtil;
import ch.algotrader.util.MyLogger;
import ch.algotrader.util.RoundUtil;
import ch.algotrader.util.collection.DoubleMap;
import ch.algotrader.util.spring.HibernateSession;
import ch.algotrader.vo.BalanceVO;
import ch.algotrader.vo.CurrencyAmountVO;
import ch.algotrader.vo.PortfolioValueVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@HibernateSession
public class PortfolioServiceImpl implements PortfolioService {

    private static Logger logger = MyLogger.getLogger(PortfolioServiceImpl.class.getName());

    private final CommonConfig commonConfig;

    private final CoreConfig coreConfig;

    private final LookupService lookupService;

    private final SessionFactory sessionFactory;

    private final GenericDao genericDao;

    private final StrategyDao strategyDao;

    private final TransactionDao transactionDao;

    private final PositionDao positionDao;

    private final CashBalanceDao cashBalanceDao;

    private final PortfolioValueDao portfolioValueDao;

    private final TickDao tickDao;

    private final ForexDao forexDao;

    public PortfolioServiceImpl(final CommonConfig commonConfig,
            final CoreConfig coreConfig,
            final LookupService lookupService,
            final SessionFactory sessionFactory,
            final GenericDao genericDao,
            final StrategyDao strategyDao,
            final TransactionDao transactionDao,
            final PositionDao positionDao,
            final CashBalanceDao cashBalanceDao,
            final PortfolioValueDao portfolioValueDao,
            final TickDao tickDao,
            final ForexDao forexDao) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(coreConfig, "CoreConfig is null");
        Validate.notNull(lookupService, "LookupService is null");
        Validate.notNull(sessionFactory, "SessionFactory is null");
        Validate.notNull(genericDao, "GenericDao is null");
        Validate.notNull(strategyDao, "StrategyDao is null");
        Validate.notNull(transactionDao, "TransactionDao is null");
        Validate.notNull(positionDao, "PositionDao is null");
        Validate.notNull(cashBalanceDao, "CashBalanceDao is null");
        Validate.notNull(portfolioValueDao, "PortfolioValueDao is null");
        Validate.notNull(tickDao, "TickDao is null");
        Validate.notNull(forexDao, "ForexDao is null");

        this.commonConfig = commonConfig;
        this.coreConfig = coreConfig;
        this.lookupService = lookupService;
        this.sessionFactory = sessionFactory;
        this.genericDao = genericDao;
        this.strategyDao = strategyDao;
        this.transactionDao = transactionDao;
        this.positionDao = positionDao;
        this.cashBalanceDao = cashBalanceDao;
        this.portfolioValueDao = portfolioValueDao;
        this.tickDao = tickDao;
        this.forexDao = forexDao;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getCashBalance() {

        return RoundUtil.getBigDecimal(getCashBalanceDouble());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getCashBalance(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is nempty");

        return RoundUtil.getBigDecimal(getCashBalanceDouble(strategyName));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getCashBalance(final Date date) {

        Validate.notNull(date, "Date is null");

        return RoundUtil.getBigDecimal(getCashBalanceDouble(date));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getCashBalance(final String strategyName, final Date date) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(date, "Date is null");

        return RoundUtil.getBigDecimal(getCashBalanceDouble(strategyName, date));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getCashBalance(final String filter, final Map namedParameters, final Date date) {

        Validate.notEmpty(filter, "Filter is empty");
        Validate.notNull(namedParameters, "Named parameters is null");
        Validate.notNull(date, "Date is null");

        return RoundUtil.getBigDecimal(getCashBalanceDouble(filter, namedParameters, date));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getCashBalanceDouble() {

        Collection<CashBalance> cashBalances = this.cashBalanceDao.loadAll();

        List<Position> positions = this.positionDao.findOpenFXPositionsAggregated();

        return getCashBalanceDoubleInternal(cashBalances, positions);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getCashBalanceDouble(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is null");

        Collection<CashBalance> cashBalances = this.cashBalanceDao.findCashBalancesByStrategy(strategyName);

        List<Position> positions = this.positionDao.findOpenFXPositionsByStrategy(strategyName);

        return getCashBalanceDoubleInternal(cashBalances, positions);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getCashBalanceDouble(final Date date) {

        Validate.notNull(date, "Date is null");

        Collection<Transaction> transactions = this.transactionDao.findByMaxDate(date);

        Collection<Position> openPositions = this.positionDao.findOpenPositionsByMaxDateAggregated(date);

        return getCashBalanceDoubleInternal(transactions, openPositions, date);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getCashBalanceDouble(final String strategyName, final Date date) {

        Validate.notEmpty(strategyName, "Strategy name is null");
        Validate.notNull(date, "Date is null");

        Collection<Transaction> transactions = this.transactionDao.findByStrategyAndMaxDate(strategyName, date);

        Collection<Position> openPositions = this.positionDao.findOpenPositionsByStrategyAndMaxDate(strategyName, date);

        return getCashBalanceDoubleInternal(transactions, openPositions, date);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getCashBalanceDouble(final String filter, final Map namedParameters, final Date date) {

        Validate.notEmpty(filter, "Filter is empty");
        Validate.notNull(namedParameters, "Named parameters is null");
        Validate.notNull(date, "Date is null");

        namedParameters.put("maxDate", date);

        //@formatter:off
            String query =
                    "from TransactionImpl as t "
                    + "where " + filter + " "
                    + "and t.dateTime <= :maxDate";
          //@formatter:on
        Collection<Transaction> transactions = (Collection<Transaction>) this.genericDao.find(query, namedParameters);

        return getCashBalanceDoubleInternal(transactions, new ArrayList<Position>(), date);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getSecuritiesCurrentValue() {

        return RoundUtil.getBigDecimal(getSecuritiesCurrentValueDouble());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getSecuritiesCurrentValue(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        return RoundUtil.getBigDecimal(getSecuritiesCurrentValueDouble(strategyName));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getSecuritiesCurrentValue(final Date date) {

        Validate.notNull(date, "Date is null");

        return RoundUtil.getBigDecimal(getSecuritiesCurrentValueDouble(date));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getSecuritiesCurrentValue(final String strategyName, final Date date) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(date, "Date is null");

        return RoundUtil.getBigDecimal(getSecuritiesCurrentValueDouble(strategyName, date));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getSecuritiesCurrentValue(final String filter, final Map namedParameters, final Date date) {

        Validate.notEmpty(filter, "Filter is empty");
        Validate.notNull(namedParameters, "Named parameters is null");
        Validate.notNull(date, "Date is null");

        return RoundUtil.getBigDecimal(getSecuritiesCurrentValueDouble(filter, namedParameters, date));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getSecuritiesCurrentValueDouble() {

        Collection<Position> openPositions = this.positionDao.findOpenTradeablePositionsAggregated();

        return getSecuritiesCurrentValueDoubleInternal(openPositions);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getSecuritiesCurrentValueDouble(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        List<Position> openPositions = this.positionDao.findOpenTradeablePositionsByStrategy(strategyName);

        return getSecuritiesCurrentValueDoubleInternal(openPositions);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getSecuritiesCurrentValueDouble(final Date date) {

        Validate.notNull(date, "Date is null");

        Collection<Position> openPositions = this.positionDao.findOpenPositionsByMaxDateAggregated(date);

        return getSecuritiesCurrentValueDoubleInternal(openPositions, date);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getSecuritiesCurrentValueDouble(final String strategyName, final Date date) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(date, "Date is null");

        Collection<Position> openPositions = this.positionDao.findOpenPositionsByStrategyAndMaxDate(strategyName, date);

        return getSecuritiesCurrentValueDoubleInternal(openPositions, date);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getSecuritiesCurrentValueDouble(final String filter, final Map namedParameters, final Date date) {

        Validate.notEmpty(filter, "Filter is empty");
        Validate.notNull(namedParameters, "Named parameters is null");
        Validate.notNull(date, "Date is null");

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

        List<Position> openPositions = (List<Position>) this.genericDao.find(queryString, namedParameters);

        return getSecuritiesCurrentValueDoubleInternal(openPositions, date);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getMaintenanceMargin() {

        return RoundUtil.getBigDecimal(getMaintenanceMarginDouble());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getMaintenanceMargin(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        return RoundUtil.getBigDecimal(getMaintenanceMarginDouble(strategyName));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMaintenanceMarginDouble() {

        double margin = 0.0;
        Collection<Position> positions = this.positionDao.findOpenTradeablePositions();
        for (Position position : positions) {
            margin += position.getMaintenanceMarginBaseDouble();
        }
        return margin;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMaintenanceMarginDouble(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        double margin = 0.0;
        List<Position> positions = this.positionDao.findOpenTradeablePositionsByStrategy(strategyName);
        for (Position position : positions) {
            margin += position.getMaintenanceMarginBaseDouble();
        }
        return margin;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getInitialMargin() {

        return RoundUtil.getBigDecimal(getInitialMarginDouble());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getInitialMargin(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        return RoundUtil.getBigDecimal(getInitialMarginDouble(strategyName));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getInitialMarginDouble() {

        return this.commonConfig.getInitialMarginMarkup().doubleValue() * getMaintenanceMarginDouble();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getInitialMarginDouble(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        return this.commonConfig.getInitialMarginMarkup().doubleValue() * getMaintenanceMarginDouble(strategyName);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getNetLiqValue() {

        return RoundUtil.getBigDecimal(getNetLiqValueDouble());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getNetLiqValue(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        return RoundUtil.getBigDecimal(getNetLiqValueDouble(strategyName));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getNetLiqValueDouble() {

        return getCashBalanceDouble() + getSecuritiesCurrentValueDouble();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getNetLiqValueDouble(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        return getCashBalanceDouble(strategyName) + getSecuritiesCurrentValueDouble(strategyName);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getAvailableFunds() {

        return RoundUtil.getBigDecimal(getAvailableFundsDouble());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getAvailableFunds(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        return RoundUtil.getBigDecimal(getAvailableFundsDouble(strategyName));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getAvailableFundsDouble() {

        return getNetLiqValueDouble() - getInitialMarginDouble();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getAvailableFundsDouble(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        return getNetLiqValueDouble(strategyName) - getInitialMarginDouble(strategyName);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getLeverage() {

        double exposure = 0.0;
        Collection<Position> positions = this.positionDao.findOpenTradeablePositions();
        for (Position position : positions) {
            exposure += position.getExposure();
        }
        return exposure / getNetLiqValueDouble();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getLeverage(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        double exposure = 0.0;
        List<Position> positions = this.positionDao.findOpenTradeablePositionsByStrategy(strategyName);
        for (Position position : positions) {
            exposure += position.getExposure();
        }

        return exposure / getNetLiqValueDouble(strategyName);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getPerformance() {

        return getPerformance(StrategyImpl.SERVER);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getPerformance(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        Date date = DateUtils.truncate(new Date(), Calendar.MONTH);
        List<PortfolioValueVO> portfolioValues = (List<PortfolioValueVO>) getPortfolioValuesInclPerformanceSinceDate(strategyName, date);

        // the performance of the last portfolioValue represents the performance of the entire timeperiod
        if (portfolioValues.size() > 0) {
            return portfolioValues.get(portfolioValues.size() - 1).getPerformance();
        } else {
            return Double.NaN;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PortfolioValue getPortfolioValue() {

        return getPortfolioValue(StrategyImpl.SERVER);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PortfolioValue getPortfolioValue(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        Strategy strategy = this.strategyDao.findByName(strategyName);

        BigDecimal cashBalance;
        BigDecimal securitiesCurrentValue;
        BigDecimal maintenanceMargin;
        double leverage;
        if (strategy.isServer()) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public PortfolioValue getPortfolioValue(final String strategyName, final Date date) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(date, "Date is null");

        Strategy strategy = this.strategyDao.findByName(strategyName);

        BigDecimal cashBalance;
        BigDecimal securitiesCurrentValue;
        if (strategy.isServer()) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<PortfolioValueVO> getPortfolioValuesInclPerformanceSinceDate(final String strategyName, final Date date) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(date, "Date is null");

        Collection<PortfolioValueVO> portfolioValues = (Collection<PortfolioValueVO>) this.portfolioValueDao.findByStrategyAndMinDate(PortfolioValueDao.TRANSFORM_PORTFOLIOVALUEVO, strategyName, date);

        // calculate the performance
        double lastNetLiqValue = 0;
        double lastDayNetLiqValue = 0;
        double performance = 1.0;
        double dailyPerformance = 1.0;
        for (PortfolioValueVO portfolioValue : portfolioValues) {

            // for AlgoTrader Server reset performance at the 24:00 based on NetLiqValue of prior day
            if (StrategyImpl.SERVER.equals(strategyName) && DateUtils.getFragmentInHours(portfolioValue.getDateTime(), Calendar.DAY_OF_YEAR) == 0) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<BalanceVO> getBalances() {

        Collection<CashBalance> cashBalances = this.cashBalanceDao.loadAll();
        Collection<Position> positions = this.positionDao.findOpenTradeablePositionsAggregated();

        return getBalances(cashBalances, positions);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<BalanceVO> getBalances(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        Collection<CashBalance> cashBalances = this.cashBalanceDao.findCashBalancesByStrategy(strategyName);
        Collection<Position> positions = this.positionDao.findOpenTradeablePositionsByStrategy(strategyName);

        return getBalances(cashBalances, positions);

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
                int intervalDays = this.coreConfig.getIntervalDays();
                List<Tick> ticks = this.tickDao.findTicksBySecurityAndMaxDate(1, 1, security.getId(), date, intervalDays);
                if (ticks.isEmpty()) {
                    ticks = this.tickDao.findTicksBySecurityAndMinDate(1, 1, security.getId(), date, intervalDays);
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
            double fxRate = this.forexDao.getRateDoubleByDate(entry.getKey(), this.commonConfig.getPortfolioBaseCurrency(), date);
            amount += entry.getValue() * fxRate;
        }
        return amount;
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

    private double getSecuritiesCurrentValueDoubleInternal(Collection<Position> openPositions, Date date) {

        // sum of all non-FX positions (FX counts as cash)
        DoubleMap<Currency> map = new DoubleMap<Currency>();

        for (Position openPosition : openPositions) {

            Security security = openPosition.getSecurityInitialized();
            if (!(security instanceof Forex)) {
                int intervalDays = this.coreConfig.getIntervalDays();
                List<Tick> ticks = this.tickDao.findTicksBySecurityAndMaxDate(1, 1, security.getId(), date, intervalDays);
                if (ticks.isEmpty()) {
                    ticks = this.tickDao.findTicksBySecurityAndMinDate(1, 1, security.getId(), date, intervalDays);
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
            double fxRate = this.forexDao.getRateDoubleByDate(entry.getKey(), this.commonConfig.getPortfolioBaseCurrency(), date);
            amount += entry.getValue() * fxRate;
        }
        return amount;
    }

    private List<BalanceVO> getBalances(Collection<CashBalance> cashBalances, Collection<Position> positions) {

        DoubleMap<Currency> cashMap = new DoubleMap<Currency>();
        DoubleMap<Currency> securitiesMap = new DoubleMap<Currency>();

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

        Set<Currency> currencies = new HashSet<Currency>(cashMap.keySet());
        currencies.addAll(securitiesMap.keySet());

        List<BalanceVO> balances = new ArrayList<BalanceVO>();
        for (Currency currency : currencies) {

            double cash = cashMap.containsKey(currency) ? cashMap.get(currency) : 0.0;
            double securities = securitiesMap.containsKey(currency) ? securitiesMap.get(currency) : 0.0;
            double netLiqValue = cash + securities;
            double exchangeRate = this.lookupService.getForexRateDouble(currency, this.commonConfig.getPortfolioBaseCurrency());
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

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void savePortfolioValue(final Transaction transaction) {

        // do not save PortfolioValue in simulation
        if (this.commonConfig.isSimulation()) {
            return;
        }

        // only process performanceRelevant transactions
        if (transaction.isPerformanceRelevant()) {

            // check if there is an existing portfolio value
            Collection<PortfolioValue> portfolioValues = this.portfolioValueDao.findByStrategyAndMinDate(transaction.getStrategy().getName(), transaction.getDateTime());

            if (portfolioValues.size() > 0) {

                logger.warn("transaction date is in the past, please restore portfolio values");

            } else {

                // create and save the portfolio value
                PortfolioValue portfolioValue = getPortfolioValue(transaction.getStrategy().getName());

                portfolioValue.setCashFlow(transaction.getGrossValue());

                this.portfolioValueDao.create(portfolioValue);
            }
        }

    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void savePortfolioValues() {

        for (Strategy strategy : this.strategyDao.findAutoActivateStrategies()) {

            PortfolioValue portfolioValue = getPortfolioValue(strategy.getName());

            // truncate Date to hour
            portfolioValue.setDateTime(DateUtils.truncate(portfolioValue.getDateTime(), Calendar.HOUR));

            this.portfolioValueDao.create(portfolioValue);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void restorePortfolioValues(final Strategy strategy, final Date fromDate, final Date toDate) {

        Validate.notNull(strategy, "Strategy is null");
        Validate.notNull(fromDate, "From date is null");
        Validate.notNull(toDate, "To date is null");

        // delete existing portfolio values;
        List<PortfolioValue> portfolioValues = this.portfolioValueDao.findByStrategyAndMinDate(strategy.getName(), fromDate);

        if (portfolioValues.size() > 0) {

            this.portfolioValueDao.remove(portfolioValues);

            // need to flush since new portfoliovalues will be created with same date and strategy
            this.sessionFactory.getCurrentSession().flush();
        }

        // init cron
        CronSequenceGenerator cron = new CronSequenceGenerator("0 0 * * * 1-5", TimeZone.getDefault());

        // group PortfolioValues by strategyId and date
        Map<MultiKey<Long>, PortfolioValue> portfolioValueMap = new HashMap<MultiKey<Long>, PortfolioValue>();

        // create portfolioValues for all cron time slots
        Date date = cron.next(DateUtils.addHours(fromDate, -1));
        while (date.compareTo(toDate) <= 0) {

            PortfolioValue portfolioValue = getPortfolioValue(strategy.getName(), date);
            if (portfolioValue.getNetLiqValueDouble() == 0) {
                date = cron.next(date);
                continue;
            } else {
                MultiKey<Long> key = new MultiKey<Long>((long) strategy.getId(), date.getTime());
                portfolioValueMap.put(key, portfolioValue);

                logger.info("processed portfolioValue for " + strategy.getName() + " " + date);

                date = cron.next(date);
            }
        }

        // save values for all cashFlows
        List<Transaction> transactions = this.transactionDao.findCashflowsByStrategyAndMinDate(strategy.getName(), fromDate);
        for (Transaction transaction : transactions) {

            // only process performanceRelevant transactions
            if (!transaction.isPerformanceRelevant()) {
                continue;
            }

            // do not save before fromDate
            if (transaction.getDateTime().compareTo(fromDate) < 0) {
                continue;
            }

            // if there is an existing PortfolioValue, add the cashFlow
            MultiKey<Long> key = new MultiKey<Long>((long) transaction.getStrategy().getId(), transaction.getDateTime().getTime());
            if (portfolioValueMap.containsKey(key)) {
                PortfolioValue portfolioValue = portfolioValueMap.get(key);
                if (portfolioValue.getCashFlow() != null) {
                    portfolioValue.setCashFlow(portfolioValue.getCashFlow().add(transaction.getGrossValue()));
                } else {
                    portfolioValue.setCashFlow(transaction.getGrossValue());
                }
            } else {
                PortfolioValue portfolioValue = getPortfolioValue(transaction.getStrategy().getName(), transaction.getDateTime());
                portfolioValue.setCashFlow(transaction.getGrossValue());
                portfolioValueMap.put(key, portfolioValue);
            }

            logger.info("processed portfolioValue for " + transaction.getStrategy().getName() + " " + transaction.getDateTime() + " cashflow " + transaction.getGrossValue());
        }

        // perisist the PortfolioValues
        this.portfolioValueDao.create(portfolioValueMap.values());
    }

}
