package com.algoTrader.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.algoTrader.ServiceLocator;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.vo.BalanceVO;

public class StrategyImpl extends Strategy {

    private static final long serialVersionUID = -2271735085273721632L;

    private static double initialMarginMarkup = ConfigurationUtil.getBaseConfig().getDouble("initialMarginMarkup");
    private static Currency portfolioBaseCurrency = Currency.fromString(ConfigurationUtil.getBaseConfig().getString("portfolioBaseCurrency"));

    public final static String BASE = "BASE";

    public boolean isBase() {
        return (BASE.equals(getName()));
    }

    public BigDecimal getCashBalance() {
        return RoundUtil.getBigDecimal(getCashBalanceDouble());
    }

    @SuppressWarnings("unchecked")
    public double getCashBalanceDouble() {

        List<Currency> currencies = ServiceLocator.commonInstance().getLookupService().getHeldCurrencies(getName());
        Map<Currency, Double> cashMap = new HashMap<Currency, Double>();
        Map<Currency, Double> cashFlowMap = new HashMap<Currency, Double>();

        for (Currency currency : currencies) {
            cashMap.put(currency, 0.0);
            cashFlowMap.put(currency, 0.0);
        }

        // sum of all transactions that belongs to this strategy
        Collection<Transaction> transactions = getTransactions();
        for (Transaction transaction : transactions) {
            Currency currency = transaction.getCurrency();
            double cash = cashMap.get(currency) + transaction.getValueDouble();
            cashMap.put(currency, cash);
        }

        // plus part of all cashFlows
        Transaction[] cashFlowTransactions = ServiceLocator.commonInstance().getLookupService().getAllCashFlows();
        for (Transaction transaction : cashFlowTransactions) {
            Currency currency = transaction.getCurrency();
            double cashFlow = cashFlowMap.get(currency) + transaction.getValueDouble();
            cashFlowMap.put(currency, cashFlow);
        }

        double balance = 0.0;
        for (Currency currency : currencies) {

            double cash = cashMap.get(currency);
            double cashFlow = cashFlowMap.get(currency);
            double totalCash = cash + cashFlow * getAllocation();
            double exchangeRate = ServiceLocator.commonInstance().getLookupService().getForexRateDouble(currency, portfolioBaseCurrency);
            balance += (totalCash * exchangeRate);
        }

        return balance;
    }

    public BigDecimal getSecuritiesCurrentValue() {

        return RoundUtil.getBigDecimal(getSecuritiesCurrentValueDouble());
    }

    @SuppressWarnings("unchecked")
    public double getSecuritiesCurrentValueDouble() {

        List<Currency> currencies = ServiceLocator.commonInstance().getLookupService().getHeldCurrencies(getName());
        Map<Currency, Double> securitiesMap = new HashMap<Currency, Double>();

        for (Currency currency : currencies) {
            securitiesMap.put(currency, 0.0);
        }

        // get the total value of all securites
        Collection<Position> positions = getPositions();
        for (Position position : positions) {

            if (!position.isOpen())
                continue;

            Currency currency = position.getSecurity().getSecurityFamily().getCurrency();
            double securities = securitiesMap.get(currency) + position.getMarketValueDouble();
            securitiesMap.put(currency, securities);
        }

        double securitiesValue = 0.0;
        for (Currency currency : currencies) {
            double securities = securitiesMap.get(currency);
            double exchangeRate = ServiceLocator.commonInstance().getLookupService().getForexRateDouble(currency, portfolioBaseCurrency);
            securitiesValue += securities * exchangeRate;
        }

        return securitiesValue;
    }

    public BigDecimal getMaintenanceMargin() {
        return RoundUtil.getBigDecimal(getMaintenanceMarginDouble());
    }

    public double getMaintenanceMarginDouble() {

        double margin = 0.0;
        Collection<Position> positions = getPositions();
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

    public BigDecimal getNetLiqValue() {

        return RoundUtil.getBigDecimal(getNetLiqValueDouble());
    }

    public double getNetLiqValueDouble() {

        return getCashBalanceDouble() + getSecuritiesCurrentValueDouble();
    }

    public BigDecimal getAvailableFunds() {

        return RoundUtil.getBigDecimal(getAvailableFundsDouble());
    }

    public double getAvailableFundsDouble() {

        return getNetLiqValueDouble() - getInitialMarginDouble();
    }

    @SuppressWarnings("unchecked")
    public List<BalanceVO> getBalances() {

        List<Currency> currencies = ServiceLocator.commonInstance().getLookupService().getHeldCurrencies(getName());
        Map<Currency, Double> cashMap = new HashMap<Currency, Double>();
        Map<Currency, Double> cashFlowMap = new HashMap<Currency, Double>();
        Map<Currency, Double> securitiesMap = new HashMap<Currency, Double>();

        for (Currency currency : currencies) {
            cashMap.put(currency, 0.0);
            cashFlowMap.put(currency, 0.0);
            securitiesMap.put(currency, 0.0);
        }

        // sum of all transactions that belongs to this strategy
        Collection<Transaction> transactions = getTransactions();
        for (Transaction transaction : transactions) {
            Currency currency = transaction.getCurrency();
            double cash = cashMap.get(currency) + transaction.getValueDouble();
            cashMap.put(currency, cash);
        }

        // plus part of all cashFlows
        Transaction[] cashFlowTransactions = ServiceLocator.commonInstance().getLookupService().getAllCashFlows();
        for (Transaction transaction : cashFlowTransactions) {
            Currency currency = transaction.getCurrency();
            double cashFlow = cashFlowMap.get(currency) + transaction.getValueDouble();
            cashFlowMap.put(currency, cashFlow);
        }

        // get the total value of all securites
        Collection<Position> positions = getPositions();
        for (Position position : positions) {

            if (!position.isOpen())
                continue;

            Currency currency = position.getSecurity().getSecurityFamily().getCurrency();
            double securities = securitiesMap.get(currency) + position.getMarketValueDouble();
            securitiesMap.put(currency, securities);
        }

        List<BalanceVO> balances = new ArrayList<BalanceVO>();
        for (Currency currency : currencies) {

            double cash = cashMap.get(currency);
            double cashFlow = cashFlowMap.get(currency);
            double totalCash = cash + cashFlow * getAllocation();
            double securities = securitiesMap.get(currency);
            double exchangeRate = ServiceLocator.commonInstance().getLookupService().getForexRateDouble(currency, portfolioBaseCurrency);

            BalanceVO balance = new BalanceVO();
            balance.setCurrency(currency);
            balance.setCash(RoundUtil.getBigDecimal(totalCash));
            balance.setSecurities(RoundUtil.getBigDecimal(securities));
            balance.setNetLiqValue(RoundUtil.getBigDecimal(totalCash + securities));
            balance.setExchangeRate(exchangeRate);

            balances.add(balance);
        }

        return balances;
    }

    public double getRedemptionValueDouble() {

        double redemptionValue = 0.0;
        Collection<Position> positions = getPositions();
        for (Position position : positions) {
            redemptionValue += position.getRedemptionValueBaseDouble();
        }
        return redemptionValue;
    }

    public double getMaxLossDouble() {

        double maxLoss = 0.0;
        Collection<Position> positions = getPositions();
        for (Position position : positions) {
            maxLoss += position.getMaxLossBaseDouble();
        }
        return maxLoss;
    }

    public double getLeverage() {

        double exposure = 0.0;
        Collection<Position> positions = getPositions();
        for (Position position : positions) {
            if (position.isOpen()) {
                exposure += position.getExposure();
            }
        }

        return exposure / getNetLiqValueDouble();
    }
}
