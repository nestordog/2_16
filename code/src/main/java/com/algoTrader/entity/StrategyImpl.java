package com.algoTrader.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.security.Forex;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.vo.BalanceVO;

public class StrategyImpl extends Strategy {

    private static final long serialVersionUID = -2271735085273721632L;

    private static double initialMarginMarkup = ConfigurationUtil.getBaseConfig().getDouble("initialMarginMarkup");
    private static Currency portfolioBaseCurrency = Currency.fromString(ConfigurationUtil.getBaseConfig().getString("portfolioBaseCurrency"));

    public final static String BASE = "BASE";

    @Override
    public boolean isBase() {
        return (BASE.equals(getName()));
    }

    @Override
    public BigDecimal getCashBalance() {
        return RoundUtil.getBigDecimal(getCashBalanceDouble());
    }

    @Override
    public double getCashBalanceDouble() {

        double amount = 0.0;

        // sum of all cashBalances of this strategy (not considering BASE itself)
        if (!BASE.equals(this.getName())) {
            Collection<CashBalance> cashBalances = getCashBalances();
            for (CashBalance cashBalance : cashBalances) {
                amount += cashBalance.getAmountBaseDouble();
            }
        }

        // sum of all cashBalances of base (i.e. cashFlows)
        Collection<CashBalance> cashBalancesBase = getCashBalancesBase();
        for (CashBalance cashBalance : cashBalancesBase) {
            amount += cashBalance.getAmountBaseDouble() * getAllocation();
        }

        // sum of all FX positions
        Position[] positions = getOpenFXPositions();
        for (Position position : positions) {
            amount += position.getMarketValueBaseDouble();
        }

        return amount;
    }

    @Override
    public BigDecimal getSecuritiesCurrentValue() {

        return RoundUtil.getBigDecimal(getSecuritiesCurrentValueDouble());
    }

    @Override
    public double getSecuritiesCurrentValueDouble() {

        double amount = 0.0;

        // sum of all non-FX positions
        Position[] positions = getOpenPositions();
        for (Position position : positions) {

            if (!(position.getSecurity() instanceof Forex)) {
                amount += position.getMarketValueBaseDouble();
            }
        }

        return amount;
    }

    @Override
    public BigDecimal getMaintenanceMargin() {
        return RoundUtil.getBigDecimal(getMaintenanceMarginDouble());
    }

    @Override
    public double getMaintenanceMarginDouble() {

        double margin = 0.0;
        Position[] positions = getOpenPositions();
        for (Position position : positions) {
            margin += position.getMaintenanceMarginBaseDouble();
        }
        return margin;
    }

    @Override
    public BigDecimal getInitialMargin() {

        return RoundUtil.getBigDecimal(getInitialMarginDouble());
    }

    @Override
    public double getInitialMarginDouble() {

        return initialMarginMarkup * getMaintenanceMarginDouble();
    }

    @Override
    public BigDecimal getNetLiqValue() {

        return RoundUtil.getBigDecimal(getNetLiqValueDouble());
    }

    @Override
    public double getNetLiqValueDouble() {

        return getCashBalanceDouble() + getSecuritiesCurrentValueDouble();
    }

    @Override
    public BigDecimal getAvailableFunds() {

        return RoundUtil.getBigDecimal(getAvailableFundsDouble());
    }

    @Override
    public double getAvailableFundsDouble() {

        return getNetLiqValueDouble() - getInitialMarginDouble();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<BalanceVO> getBalances() {

        List<Currency> currencies = ServiceLocator.commonInstance().getLookupService().getHeldCurrencies(getName());
        Map<Currency, Double> cashMap = new HashMap<Currency, Double>();
        Map<Currency, Double> securitiesMap = new HashMap<Currency, Double>();

        for (Currency currency : currencies) {
            cashMap.put(currency, 0.0);
            securitiesMap.put(currency, 0.0);
        }

        // sum of all cashBalances of this strategy
        Collection<CashBalance> cashBalances = getCashBalances();
        for (CashBalance cashBalance : cashBalances) {
            Currency currency = cashBalance.getCurrency();
            double amount = cashMap.get(currency) + cashBalance.getAmountDouble();
            cashMap.put(currency, amount);
        }

        // sum of all cashBalances of base (i.e. cashFlows)
        Collection<CashBalance> cashBalancesBase = getCashBalancesBase();
        for (CashBalance cashBalance : cashBalancesBase) {
            Currency currency = cashBalance.getCurrency();
            double amount = cashMap.get(currency) + cashBalance.getAmountDouble() * getAllocation();
            cashMap.put(currency, amount);
        }

        // sum of all positions
        Position[] positions = getOpenPositions();
        for (Position position : positions) {

            // Forex positions are considered cash
            if (position.getSecurity() instanceof Forex) {
                Currency currency = ((Forex) position.getSecurity()).getBaseCurrency();
                double amount = cashMap.get(currency) + position.getQuantity();
                cashMap.put(currency, amount);
            } else {
                Currency currency = position.getSecurity().getSecurityFamily().getCurrency();
                double amount = securitiesMap.get(currency) + position.getMarketValueDouble();
                securitiesMap.put(currency, amount);
            }
        }

        List<BalanceVO> balances = new ArrayList<BalanceVO>();
        for (Currency currency : currencies) {

            double cash = cashMap.get(currency);
            double securities = securitiesMap.get(currency);
            double netLiqValue = cash + securities;
            double exchangeRate = ServiceLocator.commonInstance().getLookupService().getForexRateDouble(currency, portfolioBaseCurrency);
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
    public double getRedemptionValueDouble() {

        double redemptionValue = 0.0;
        Position[] positions = getOpenPositions();
        for (Position position : positions) {
            redemptionValue += position.getRedemptionValueBaseDouble();
        }
        return redemptionValue;
    }

    @Override
    public double getMaxLossDouble() {

        double maxLoss = 0.0;
        Position[] positions = getOpenPositions();
        for (Position position : positions) {
            maxLoss += position.getMaxLossBaseDouble();
        }
        return maxLoss;
    }

    @Override
    public double getLeverage() {

        double exposure = 0.0;
        Position[] positions = getOpenPositions();
        for (Position position : positions) {
            exposure += position.getExposure();
        }

        return exposure / getNetLiqValueDouble();
    }

    @Override
    public String toString() {

        return getName();
    }

    private Position[] getOpenPositions() {
        return ServiceLocator.commonInstance().getLookupService().getOpenPositionsByStrategy(getName());
    }

    private Position[] getOpenFXPositions() {
        return ServiceLocator.commonInstance().getLookupService().getOpenFXPositionsByStrategy(getName());
    }

    @SuppressWarnings("unchecked")
    private List<CashBalance> getCashBalancesBase() {
        return ServiceLocator.commonInstance().getLookupService().getCashBalancesBase();
    }
}
