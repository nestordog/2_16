package com.algoTrader.entity;

import java.math.BigDecimal;
import java.util.Collection;

import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.RoundUtil;

public class StrategyDaoImpl extends StrategyDaoBase {

    private static double initialMarginMarkup = ConfigurationUtil.getBaseConfig().getDouble("initialMarginMarkup");

    protected BigDecimal handleGetPortfolioCashBalance() throws Exception {
        return RoundUtil.getBigDecimal(getPortfolioCashBalanceDouble());
    }

    @SuppressWarnings("unchecked")
    protected double handleGetPortfolioCashBalanceDouble() throws Exception {

        double cashBalance = 0.0;
        Collection<Transaction> transactions = getTransactionDao().loadAll();
        for (Transaction transaction : transactions) {
            cashBalance += transaction.getValueDouble();
        }
        return cashBalance;
    }

    protected BigDecimal handleGetPortfolioSecuritiesCurrentValue() throws Exception {
        return RoundUtil.getBigDecimal(getPortfolioSecuritiesCurrentValueDouble());
    }

    @SuppressWarnings("unchecked")
    protected double handleGetPortfolioSecuritiesCurrentValueDouble() throws Exception {

        double securitiesValue = 0.0;
        Collection<Position> positions = getPositionDao().findOpenPositions();
        for (Position position : positions) {
            securitiesValue += position.getMarketValueDouble();
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
            margin += position.getMaintenanceMarginDouble();
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
    protected double handleGetPortfolioLeverageDouble() throws Exception {

        double deltaRisk = 0.0;
        Collection<Position> positions = getPositionDao().findOpenPositions();
        for (Position position : positions) {
            deltaRisk += position.getDeltaRisk();
        }
        return deltaRisk / getPortfolioNetLiqValueDouble();
    }

    @SuppressWarnings("unchecked")
    protected double handleGetPortfolioCashFlowDouble() throws Exception {

        double cashFlows = 0.0;
        Collection<Transaction> transactions = getTransactionDao().findAllCashflows();
        for (Transaction transaction : transactions) {
            cashFlows += transaction.getValueDouble();
        }
        return cashFlows;
    }
}
