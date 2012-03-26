package com.algoTrader.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.security.Forex;
import com.algoTrader.entity.strategy.CashBalance;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.util.DoubleMap;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.vo.BalanceVO;

public class StrategyDaoImpl extends StrategyDaoBase {

    private @Value("${order.initialMarginMarkup}") double initialMarginMarkup;
    private @Value("#{T(com.algoTrader.enumeration.Currency).fromString('${misc.portfolioBaseCurrency}')}") Currency portfolioBaseCurrency;

    @Override
    protected BigDecimal handleGetPortfolioCashBalance() throws Exception {
        return RoundUtil.getBigDecimal(getPortfolioCashBalanceDouble());
    }

    @Override
    protected double handleGetPortfolioCashBalanceDouble() throws Exception {

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
    protected BigDecimal handleGetPortfolioSecuritiesCurrentValue() throws Exception {
        return RoundUtil.getBigDecimal(getPortfolioSecuritiesCurrentValueDouble());
    }

    @Override
    protected double handleGetPortfolioSecuritiesCurrentValueDouble() throws Exception {

        double amount = 0.0;

        // sum of all non-FX positions
        Collection<Position> positions = getPositionDao().findOpenPositions();
        for (Position position : positions) {

            if (!(position.getSecurity() instanceof Forex)) {
                amount += position.getMarketValueBaseDouble();
            }
        }

        return amount;
    }

    @Override
    protected BigDecimal handleGetPortfolioMaintenanceMargin() throws Exception {
        return RoundUtil.getBigDecimal(getPortfolioMaintenanceMarginDouble());
    }

    @Override
    protected double handleGetPortfolioMaintenanceMarginDouble() throws Exception {

        double margin = 0.0;
        Collection<Position> positions = getPositionDao().findOpenPositions();
        for (Position position : positions) {
            margin += position.getMaintenanceMarginBaseDouble();
        }
        return margin;
    }

    @Override
    protected BigDecimal handleGetPortfolioInitialMargin() {

        return RoundUtil.getBigDecimal(getPortfolioInitialMarginDouble());
    }

    @Override
    protected double handleGetPortfolioInitialMarginDouble() {

        return this.initialMarginMarkup * getPortfolioMaintenanceMarginDouble();
    }

    @Override
    protected BigDecimal handleGetPortfolioNetLiqValue() throws Exception {
        return RoundUtil.getBigDecimal(getPortfolioNetLiqValueDouble());
    }

    @Override
    protected double handleGetPortfolioNetLiqValueDouble() throws Exception {

        return getPortfolioCashBalanceDouble() + getPortfolioSecuritiesCurrentValueDouble();
    }

    @Override
    protected BigDecimal handleGetPortfolioAvailableFunds() throws Exception {
        return RoundUtil.getBigDecimal(getPortfolioAvailableFundsDouble());
    }

    @Override
    protected double handleGetPortfolioAvailableFundsDouble() throws Exception {

        return getPortfolioNetLiqValueDouble() - getPortfolioInitialMarginDouble();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<BalanceVO> handleGetPortfolioBalances() throws Exception {

        List<Currency> currencies = ServiceLocator.instance().getLookupService().getHeldCurrencies();
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
        Collection<Position> positions = getPositionDao().findOpenPositions();
        for (Position position : positions) {

            // Forex positions are considered cash
            if (position.getSecurity() instanceof Forex) {
                Currency currency = ((Forex) position.getSecurity()).getBaseCurrency();
                cashMap.increment(currency, position.getQuantity());
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

    @Override
    protected double handleGetPortfolioLeverage() throws Exception {

        double exposure = 0.0;
        Collection<Position> positions = getPositionDao().findOpenPositions();
        for (Position position : positions) {
            exposure += position.getExposure();
        }
        return exposure / getPortfolioNetLiqValueDouble();
    }

    @Override
    protected double handleGetPortfolioPerformance() throws Exception {

        Strategy base = findByName(StrategyImpl.BASE);
        if (base.getBenchmark() != null) {
            return getPortfolioNetLiqValueDouble() / base.getBenchmark().doubleValue() - 1.0;
        } else {
            return Double.NaN;
        }
    }
}
