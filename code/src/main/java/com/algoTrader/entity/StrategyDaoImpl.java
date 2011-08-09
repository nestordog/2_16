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

public class StrategyDaoImpl extends StrategyDaoBase {

    private static double initialMarginMarkup = ConfigurationUtil.getBaseConfig().getDouble("initialMarginMarkup");
    private static Currency portfolioBaseCurrency = Currency.fromString(ConfigurationUtil.getBaseConfig().getString("portfolioBaseCurrency"));

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

        return initialMarginMarkup * getPortfolioMaintenanceMarginDouble();
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

        List<Currency> currencies = ServiceLocator.commonInstance().getLookupService().getHeldCurrencies();
        Map<Currency, Double> cashMap = new HashMap<Currency, Double>();
        Map<Currency, Double> securitiesMap = new HashMap<Currency, Double>();

        for (Currency currency : currencies) {
            cashMap.put(currency, 0.0);
            securitiesMap.put(currency, 0.0);
        }

        // sum of all cashBalances
        Collection<CashBalance> cashBalances = getCashBalanceDao().loadAll();
        for (CashBalance cashBalance : cashBalances) {
            Currency currency = cashBalance.getCurrency();
            double amount = cashMap.get(currency) + cashBalance.getAmountDouble();
            cashMap.put(currency, amount);
        }

        // sum of all positions
        Collection<Position> positions = getPositionDao().findOpenPositions();
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
    protected double handleGetPortfolioLeverageDouble() throws Exception {

        double exposure = 0.0;
        Collection<Position> positions = getPositionDao().findOpenPositions();
        for (Position position : positions) {
            exposure += position.getExposure();
        }
        return exposure / getPortfolioNetLiqValueDouble();
    }
}
