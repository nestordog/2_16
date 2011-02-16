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

public class StrategyDaoImpl extends StrategyDaoBase {

    private static double initialMarginMarkup = ConfigurationUtil.getBaseConfig().getDouble("initialMarginMarkup");
    private static Currency portfolioBaseCurrency = Currency.fromString(ConfigurationUtil.getBaseConfig().getString("portfolioBaseCurrency"));

    protected BigDecimal handleGetPortfolioCashBalance() throws Exception {
        return RoundUtil.getBigDecimal(getPortfolioCashBalanceDouble());
    }

    @SuppressWarnings("unchecked")
    protected double handleGetPortfolioCashBalanceDouble() throws Exception {

        List<Currency> currencies = ServiceLocator.commonInstance().getLookupService().getHeldCurrencies();
        Map<Currency, Double> cashMap = new HashMap<Currency, Double>();

        for (Currency currency : currencies) {
            cashMap.put(currency, 0.0);
        }

        Collection<Transaction> transactions = getTransactionDao().loadAll();
        for (Transaction transaction : transactions) {
            Currency currency = transaction.getCurrency();
            double cash = cashMap.get(currency) + transaction.getValueDouble();
            cashMap.put(currency, cash);
        }

        double balance = 0.0;
        for (Currency currency : currencies) {

            double cash = cashMap.get(currency);
            double exchangeRate = ServiceLocator.commonInstance().getLookupService().getForexRateDouble(currency, portfolioBaseCurrency);
            balance += (cash * exchangeRate);
        }

        return balance;
    }

    protected BigDecimal handleGetPortfolioSecuritiesCurrentValue() throws Exception {
        return RoundUtil.getBigDecimal(getPortfolioSecuritiesCurrentValueDouble());
    }

    @SuppressWarnings("unchecked")
    protected double handleGetPortfolioSecuritiesCurrentValueDouble() throws Exception {

        List<Currency> currencies = ServiceLocator.commonInstance().getLookupService().getHeldCurrencies();
        Map<Currency, Double> securitiesMap = new HashMap<Currency, Double>();

        for (Currency currency : currencies) {
            securitiesMap.put(currency, 0.0);
        }

        // get the total value of all securites
        Collection<Position> positions = getPositionDao().findOpenPositions();
        for (Position position : positions) {
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

    protected BigDecimal handleGetPortfolioMaintenanceMargin() throws Exception {
        return RoundUtil.getBigDecimal(getPortfolioMaintenanceMarginDouble());
    }

    @SuppressWarnings("unchecked")
    protected double handleGetPortfolioMaintenanceMarginDouble() throws Exception {

        double margin = 0.0;
        Collection<Position> positions = getPositionDao().findOpenPositions();
        for (Position position : positions) {
            margin += position.getMaintenanceMarginBaseDouble();
        }
        return margin;
    }

    protected BigDecimal handleGetPortfolioInitialMargin() {

        return RoundUtil.getBigDecimal(getPortfolioInitialMarginDouble());
    }

    protected double handleGetPortfolioInitialMarginDouble() {

        return initialMarginMarkup * getPortfolioMaintenanceMarginDouble();
    }

    protected BigDecimal handleGetPortfolioNetLiqValue() throws Exception {
        return RoundUtil.getBigDecimal(getPortfolioNetLiqValueDouble());
    }

    protected double handleGetPortfolioNetLiqValueDouble() throws Exception {

        return getPortfolioCashBalanceDouble() + getPortfolioSecuritiesCurrentValueDouble();
    }

    protected BigDecimal handleGetPortfolioAvailableFunds() throws Exception {
        return RoundUtil.getBigDecimal(getPortfolioAvailableFundsDouble());
    }

    protected double handleGetPortfolioAvailableFundsDouble() throws Exception {

        return getPortfolioNetLiqValueDouble() - getPortfolioInitialMarginDouble();
    }

    @SuppressWarnings("unchecked")
    protected List<BalanceVO> handleGetPortfolioBalances() throws Exception {

        List<Currency> currencies = ServiceLocator.commonInstance().getLookupService().getHeldCurrencies();
        Map<Currency, Double> cashMap = new HashMap<Currency, Double>();
        Map<Currency, Double> securitiesMap = new HashMap<Currency, Double>();

        for (Currency currency : currencies) {
            cashMap.put(currency, 0.0);
            securitiesMap.put(currency, 0.0);
        }

        // sum of all transactions
        Collection<Transaction> transactions = getTransactionDao().loadAll();
        for (Transaction transaction : transactions) {
            Currency currency = transaction.getCurrency();
            double cash = cashMap.get(currency) + transaction.getValueDouble();
            cashMap.put(currency, cash);
        }

        // get the total value of all securites
        Collection<Position> positions = getPositionDao().findOpenPositions();
        for (Position position : positions) {
            Currency currency = position.getSecurity().getSecurityFamily().getCurrency();
            double securities = securitiesMap.get(currency) + position.getMarketValueDouble();
            securitiesMap.put(currency, securities);
        }

        List<BalanceVO> balances = new ArrayList<BalanceVO>();
        for (Currency currency : currencies) {

            double cash = cashMap.get(currency);
            double securities = securitiesMap.get(currency);
            double exchangeRate = ServiceLocator.commonInstance().getLookupService().getForexRateDouble(currency, portfolioBaseCurrency);

            BalanceVO balance = new BalanceVO();
            balance.setCurrency(currency);
            balance.setCash(RoundUtil.getBigDecimal(cash));
            balance.setSecurities(RoundUtil.getBigDecimal(securities));
            balance.setNetLiqValue(RoundUtil.getBigDecimal(cash + securities));
            balance.setExchangeRate(exchangeRate);

            balances.add(balance);
        }

        return balances;
    }

    @SuppressWarnings("unchecked")
    protected double handleGetPortfolioLeverageDouble() throws Exception {

        double deltaRisk = 0.0;
        Collection<Position> positions = getPositionDao().findOpenPositions();
        for (Position position : positions) {
            deltaRisk += position.getDeltaRisk();
        }
        return deltaRisk / getPortfolioNetLiqValueDouble();
    }
}
