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

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

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

        try {
            return RoundUtil.getBigDecimal(getCashBalanceDouble());
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getCashBalance(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is nempty");

        try {
            return RoundUtil.getBigDecimal(getCashBalanceDouble(strategyName));
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getCashBalance(final Date date) {

        Validate.notNull(date, "Date is null");

        try {
            return RoundUtil.getBigDecimal(getCashBalanceDouble(date));
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getCashBalance(final String strategyName, final Date date) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(date, "Date is null");

        try {
            return RoundUtil.getBigDecimal(getCashBalanceDouble(strategyName, date));
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getCashBalance(final String filter, final Map namedParameters, final Date date) {

        Validate.notEmpty(filter, "Filter is empty");
        Validate.notNull(namedParameters, "Named parameters is null");
        Validate.notNull(date, "Date is null");

        try {
            return RoundUtil.getBigDecimal(getCashBalanceDouble(filter, namedParameters, date));
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getCashBalanceDouble() {

        try {
            Collection<CashBalance> cashBalances = this.cashBalanceDao.loadAll();

            List<Position> positions = this.positionDao.findOpenFXPositionsAggregated();

            return getCashBalanceDoubleInternal(cashBalances, positions);
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getCashBalanceDouble(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is null");

        try {
            Collection<CashBalance> cashBalances = this.cashBalanceDao.findCashBalancesByStrategy(strategyName);

            List<Position> positions = this.positionDao.findOpenFXPositionsByStrategy(strategyName);

            return getCashBalanceDoubleInternal(cashBalances, positions);
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getCashBalanceDouble(final Date date) {

        Validate.notNull(date, "Date is null");
        try {
            Collection<Transaction> transactions = this.transactionDao.findByMaxDate(date);

            Collection<Position> openPositions = this.positionDao.findOpenPositionsByMaxDateAggregated(date);

            return getCashBalanceDoubleInternal(transactions, openPositions, date);
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getCashBalanceDouble(final String strategyName, final Date date) {

        Validate.notEmpty(strategyName, "Strategy name is null");
        Validate.notNull(date, "Date is null");

        try {
            Collection<Transaction> transactions = this.transactionDao.findByStrategyAndMaxDate(strategyName, date);

            Collection<Position> openPositions = this.positionDao.findOpenPositionsByStrategyAndMaxDate(strategyName, date);

            return getCashBalanceDoubleInternal(transactions, openPositions, date);
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getCashBalanceDouble(final String filter, final Map namedParameters, final Date date) {

        Validate.notEmpty(filter, "Filter is empty");
        Validate.notNull(namedParameters, "Named parameters is null");
        Validate.notNull(date, "Date is null");

        try {
            namedParameters.put("maxDate", date);

            //@formatter:off
            String query =
                    "from TransactionImpl as t "
                    + "where " + filter + " "
                    + "and t.dateTime <= :maxDate";

            Collection<Transaction> transactions = (Collection<Transaction>) this.genericDao.find(query, namedParameters);

            return getCashBalanceDoubleInternal(transactions, new ArrayList<Position>(), date);        }
        catch (Exception ex)
        {
            throw new PortfolioServiceException(ex.getMessage(),ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getSecuritiesCurrentValue() {
        try {
            return RoundUtil.getBigDecimal(getSecuritiesCurrentValueDouble());
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(),ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getSecuritiesCurrentValue(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        try {
            return RoundUtil.getBigDecimal(getSecuritiesCurrentValueDouble(strategyName));
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(),ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getSecuritiesCurrentValue(final Date date) {

        Validate.notNull(date, "Date is null");

        try {
            return RoundUtil.getBigDecimal(getSecuritiesCurrentValueDouble(date));
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(),ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getSecuritiesCurrentValue(final String strategyName, final Date date) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(date, "Date is null");

        try {
            return RoundUtil.getBigDecimal(getSecuritiesCurrentValueDouble(strategyName, date));
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(),ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getSecuritiesCurrentValue(final String filter, final Map namedParameters, final Date date) {

        Validate.notEmpty(filter, "Filter is empty");
        Validate.notNull(namedParameters, "Named parameters is null");
        Validate.notNull(date, "Date is null");

        try {
            return RoundUtil.getBigDecimal(getSecuritiesCurrentValueDouble(filter, namedParameters, date));
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(),ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getSecuritiesCurrentValueDouble() {

        try {
            Collection<Position> openPositions = this.positionDao.findOpenTradeablePositionsAggregated();

            return getSecuritiesCurrentValueDoubleInternal(openPositions);
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(),ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getSecuritiesCurrentValueDouble(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        try {
            List<Position> openPositions = this.positionDao.findOpenTradeablePositionsByStrategy(strategyName);

            return getSecuritiesCurrentValueDoubleInternal(openPositions);
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(),ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getSecuritiesCurrentValueDouble(final Date date) {

        Validate.notNull(date, "Date is null");

        try {
            Collection<Position> openPositions = this.positionDao.findOpenPositionsByMaxDateAggregated(date);

            return getSecuritiesCurrentValueDoubleInternal(openPositions, date);
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(),ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getSecuritiesCurrentValueDouble(final String strategyName, final Date date) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(date, "Date is null");

        try {
            Collection<Position> openPositions = this.positionDao.findOpenPositionsByStrategyAndMaxDate(strategyName, date);

            return getSecuritiesCurrentValueDoubleInternal(openPositions, date);
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(),ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getSecuritiesCurrentValueDouble(final String filter, final Map namedParameters, final Date date) {

        Validate.notEmpty(filter, "Filter is empty");
        Validate.notNull(namedParameters, "Named parameters is null");
        Validate.notNull(date, "Date is null");

        try {
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
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getMaintenanceMargin() {

        try {
            return RoundUtil.getBigDecimal(getMaintenanceMarginDouble());
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getMaintenanceMargin(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        try {
            return RoundUtil.getBigDecimal(getMaintenanceMarginDouble(strategyName));
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMaintenanceMarginDouble() {

        try {
            double margin = 0.0;
            Collection<Position> positions = this.positionDao.findOpenTradeablePositions();
            for (Position position : positions) {
                margin += position.getMaintenanceMarginBaseDouble();
            }
            return margin;
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMaintenanceMarginDouble(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        try {
            double margin = 0.0;
            List<Position> positions = this.positionDao.findOpenTradeablePositionsByStrategy(strategyName);
            for (Position position : positions) {
                margin += position.getMaintenanceMarginBaseDouble();
            }
            return margin;
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getInitialMargin() {

        try {
            return RoundUtil.getBigDecimal(getInitialMarginDouble());
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getInitialMargin(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        try {
            return RoundUtil.getBigDecimal(getInitialMarginDouble(strategyName));
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getInitialMarginDouble() {

        try {
            return this.commonConfig.getInitialMarginMarkup().doubleValue() * getMaintenanceMarginDouble();
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getInitialMarginDouble(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        try {
            return this.commonConfig.getInitialMarginMarkup().doubleValue() * getMaintenanceMarginDouble(strategyName);
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getNetLiqValue() {

        try {
            return RoundUtil.getBigDecimal(getNetLiqValueDouble());
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getNetLiqValue(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        try {
            return RoundUtil.getBigDecimal(getNetLiqValueDouble(strategyName));
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getNetLiqValueDouble() {

        try {
            return getCashBalanceDouble() + getSecuritiesCurrentValueDouble();
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getNetLiqValueDouble(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        try {
            return getCashBalanceDouble(strategyName) + getSecuritiesCurrentValueDouble(strategyName);
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getAvailableFunds() {

        try {
            return RoundUtil.getBigDecimal(getAvailableFundsDouble());
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getAvailableFunds(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        try {
            return RoundUtil.getBigDecimal(getAvailableFundsDouble(strategyName));
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getAvailableFundsDouble() {

        try {
            return getNetLiqValueDouble() - getInitialMarginDouble();
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getAvailableFundsDouble(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        try {
            return getNetLiqValueDouble(strategyName) - getInitialMarginDouble(strategyName);
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getLeverage() {

        try {
            double exposure = 0.0;
            Collection<Position> positions = this.positionDao.findOpenTradeablePositions();
            for (Position position : positions) {
                exposure += position.getExposure();
            }
            return exposure / getNetLiqValueDouble();
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getLeverage(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        try {
            double exposure = 0.0;
            List<Position> positions = this.positionDao.findOpenTradeablePositionsByStrategy(strategyName);
            for (Position position : positions) {
                exposure += position.getExposure();
            }

            return exposure / getNetLiqValueDouble(strategyName);
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getPerformance() {

        try {
            return getPerformance(StrategyImpl.BASE);
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getPerformance(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        try {
            Date date = DateUtils.truncate(new Date(), Calendar.MONTH);
            List<PortfolioValueVO> portfolioValues = (List<PortfolioValueVO>) getPortfolioValuesInclPerformanceSinceDate(strategyName, date);

            // the performance of the last portfolioValue represents the performance of the entire timeperiod
            if (portfolioValues.size() > 0) {
                return portfolioValues.get(portfolioValues.size() - 1).getPerformance();
            } else {
                return Double.NaN;
            }
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PortfolioValue getPortfolioValue() {

        try {
            return getPortfolioValue(StrategyImpl.BASE);
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PortfolioValue getPortfolioValue(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        try {
            Strategy strategy = this.strategyDao.findByName(strategyName);

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
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PortfolioValue getPortfolioValue(final String strategyName, final Date date) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(date, "Date is null");

        try {
            Strategy strategy = this.strategyDao.findByName(strategyName);

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
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<PortfolioValueVO> getPortfolioValuesInclPerformanceSinceDate(final String strategyName, final Date date) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(date, "Date is null");

        try {
            Collection<PortfolioValueVO> portfolioValues = (Collection<PortfolioValueVO>) this.portfolioValueDao.findByStrategyAndMinDate(PortfolioValueDao.TRANSFORM_PORTFOLIOVALUEVO, strategyName,
                    date);

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
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<BalanceVO> getBalances() {

        try {
            Collection<Currency> currencies = this.cashBalanceDao.findHeldCurrencies();
            Collection<CashBalance> cashBalances = this.cashBalanceDao.loadAll();
            Collection<Position> positions = this.positionDao.findOpenTradeablePositionsAggregated();

            return getBalances(currencies, cashBalances, positions);
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<BalanceVO> getBalances(final String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        try {
            Collection<Currency> currencies = this.cashBalanceDao.findHeldCurrenciesByStrategy(strategyName);
            Collection<CashBalance> cashBalances = this.cashBalanceDao.findCashBalancesByStrategy(strategyName);
            Collection<Position> positions = this.positionDao.findOpenTradeablePositionsByStrategy(strategyName);

            return getBalances(currencies, cashBalances, positions);
        } catch (Exception ex) {
            throw new PortfolioServiceException(ex.getMessage(), ex);
        }
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
}
