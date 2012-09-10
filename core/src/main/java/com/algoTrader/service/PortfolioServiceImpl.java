package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.security.Forex;
import com.algoTrader.entity.security.ForexFuture;
import com.algoTrader.entity.security.ForexI;
import com.algoTrader.entity.strategy.CashBalance;
import com.algoTrader.entity.strategy.PortfolioValue;
import com.algoTrader.entity.strategy.PortfolioValueDao;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.DoubleMap;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.vo.BalanceVO;
import com.algoTrader.vo.PortfolioValueVO;

public class PortfolioServiceImpl extends PortfolioServiceBase {

    private @Value("${order.initialMarginMarkup}") double initialMarginMarkup;
    private @Value("#{T(com.algoTrader.enumeration.Currency).fromString('${misc.portfolioBaseCurrency}')}") Currency portfolioBaseCurrency;

    @Override
    protected BigDecimal handleGetCashBalance() throws Exception {
        return RoundUtil.getBigDecimal(getCashBalanceDouble());
    }

    @Override
    protected BigDecimal handleGetCashBalance(String strategyName) {
        return RoundUtil.getBigDecimal(getCashBalanceDouble(strategyName));
    }

    @Override
    protected double handleGetCashBalanceDouble() throws Exception {

        double amount = 0.0;

        // sum of all cashBalances
        Collection<CashBalance> cashBalances = getCashBalanceDao().loadAll();
        for (CashBalance cashBalance : cashBalances) {
            amount += cashBalance.getAmountBaseDouble();
        }

        // sum of all FX positions
        List<Position> positions = getPositionDao().findOpenFXPositions();
        for (Position position : positions) {
            amount += position.getMarketValueBaseDouble();
        }

        return amount;
    }

    @Override
    protected double handleGetCashBalanceDouble(String strategyName) {

        // sum of all cashBalances of this strategy
        Collection<CashBalance> cashBalances = getCashBalanceDao().findCashBalancesByStrategy(strategyName);
        double amount = 0.0;
        for (CashBalance cashBalance : cashBalances) {
            amount += cashBalance.getAmountBaseDouble();
        }

        // sum of all FX positions
        List<Position> positions = getPositionDao().findOpenFXPositionsByStrategy(strategyName);
        for (Position position : positions) {
            amount += position.getMarketValueBaseDouble();
        }

        return amount;
    }

    @Override
    protected BigDecimal handleGetSecuritiesCurrentValue() throws Exception {
        return RoundUtil.getBigDecimal(getSecuritiesCurrentValueDouble());
    }

    @Override
    protected BigDecimal handleGetSecuritiesCurrentValue(String strategyName) {

        return RoundUtil.getBigDecimal(getSecuritiesCurrentValueDouble(strategyName));
    }

    @Override
    protected double handleGetSecuritiesCurrentValueDouble() throws Exception {

        // sum of all non-FX positions (FX counts as cash)
        double amount = 0.0;
        Collection<Position> positions = getPositionDao().findOpenTradeablePositions();
        for (Position position : positions) {

            if (!(position.getSecurity() instanceof Forex)) {
                amount += position.getMarketValueBaseDouble();
            }
        }
        return amount;
    }

    @Override
    protected double handleGetSecuritiesCurrentValueDouble(String strategyName) {

        // sum of all non-FX positions (FX counts as cash)
        double amount = 0.0;
        List<Position> positions = getPositionDao().findOpenTradeablePositionsByStrategy(strategyName);
        for (Position position : positions) {

            if (!(position.getSecurity() instanceof Forex)) {
                amount += position.getMarketValueBaseDouble();
            }
        }
        return amount;
    }

    @Override
    protected BigDecimal handleGetMaintenanceMargin() throws Exception {
        return RoundUtil.getBigDecimal(getMaintenanceMarginDouble());
    }

    @Override
    protected BigDecimal handleGetMaintenanceMargin(String strategyName) {
        return RoundUtil.getBigDecimal(getMaintenanceMarginDouble(strategyName));
    }

    @Override
    protected double handleGetMaintenanceMarginDouble() throws Exception {

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
    protected BigDecimal handleGetNetLiqValue() throws Exception {
        return RoundUtil.getBigDecimal(getNetLiqValueDouble());
    }

    @Override
    protected BigDecimal handleGetNetLiqValue(String strategyName) {

        return RoundUtil.getBigDecimal(getNetLiqValueDouble(strategyName));
    }

    @Override
    protected double handleGetNetLiqValueDouble() throws Exception {

        return getCashBalanceDouble() + getSecuritiesCurrentValueDouble();
    }

    @Override
    protected double handleGetNetLiqValueDouble(String strategyName) {

        return getCashBalanceDouble(strategyName) + getSecuritiesCurrentValueDouble(strategyName);
    }

    @Override
    protected BigDecimal handleGetAvailableFunds() throws Exception {
        return RoundUtil.getBigDecimal(getAvailableFundsDouble());
    }

    @Override
    protected BigDecimal handleGetAvailableFunds(String strategyName) {

        return RoundUtil.getBigDecimal(getAvailableFundsDouble(strategyName));
    }

    @Override
    protected double handleGetAvailableFundsDouble() throws Exception {

        return getNetLiqValueDouble() - getInitialMarginDouble();
    }

    @Override
    protected double handleGetAvailableFundsDouble(String strategyName) {

        return getNetLiqValueDouble(strategyName) - getInitialMarginDouble(strategyName);
    }

    @Override
    protected double handleGetLeverage() throws Exception {

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
    protected double handleGetPerformance() throws Exception {

        return getPerformance(StrategyImpl.BASE);
    }

    @Override
    protected double handleGetPerformance(String strategyName) {

        Date date = DateUtils.truncate(new Date(), Calendar.MONTH);
        List<PortfolioValueVO> portfolioValues = (List<PortfolioValueVO>) getPortfolioValuesSinceDate(strategyName, date);

        // the performance of the last portfolioValue represents the performance of the entire timeperiod
        return portfolioValues.get(portfolioValues.size() - 1).getPerformance();
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
        portfolioValue.setSecuritiesCurrentValue(securitiesCurrentValue);
        portfolioValue.setMaintenanceMargin(maintenanceMargin);
        portfolioValue.setNetLiqValue(cashBalance.add(securitiesCurrentValue)); // add here to prevent another lookup
        portfolioValue.setLeverage(Double.isNaN(leverage) ? 0 : leverage);
        portfolioValue.setAllocation(strategy.getAllocation());

        return portfolioValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Collection<PortfolioValueVO> handleGetPortfolioValuesSinceDate(String strategyName, Date minDate) throws Exception {

        Collection<PortfolioValueVO> portfolioValues = (Collection<PortfolioValueVO>) getPortfolioValueDao().findByStrategyAndMinDate(PortfolioValueDao.TRANSFORM_PORTFOLIOVALUEVO, strategyName, minDate);

        // calculate the performance
        double lastNetLiqValue = 0;
        double performance = 1.0;
        for (PortfolioValueVO portfolioValue : portfolioValues) {

            if (lastNetLiqValue != 0) {
                double adjustedNetLiqValue = portfolioValue.getNetLiqValue().doubleValue();
                if (portfolioValue.getCashFlow() != null) {
                    adjustedNetLiqValue -= portfolioValue.getCashFlow().doubleValue();
                }
                performance = performance * (adjustedNetLiqValue / lastNetLiqValue);
                portfolioValue.setPerformance(performance - 1.0);
            }
            lastNetLiqValue = portfolioValue.getNetLiqValue().doubleValue();
        }

        return portfolioValues;
    }

    @Override
    protected Collection<BalanceVO> handleGetBalances() throws Exception {

        Collection<Currency> currencies = ServiceLocator.instance().getLookupService().getHeldCurrencies();
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
        Collection<Position> positions = getPositionDao().findOpenTradeablePositions();
        for (Position position : positions) {

            // Forex and ForexFutures are attributed in their baseCurrency
            if (position.getSecurity() instanceof ForexI) {
                Currency currency = ((ForexI) position.getSecurity()).getBaseCurrency();

                // Forex positions are considered cash
                if (position.getSecurity() instanceof Forex) {
                    cashMap.increment(currency, position.getQuantity());

                    // ForexFuture positions are considered securities
                } else if (position.getSecurity() instanceof ForexFuture) {
                    securitiesMap.increment(currency, position.getQuantity() * position.getSecurity().getSecurityFamily().getContractSize());
                }

            } else {
                Currency currency = position.getSecurity().getSecurityFamily().getCurrency();
                securitiesMap.increment(currency, position.getMarketValueDouble());
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
