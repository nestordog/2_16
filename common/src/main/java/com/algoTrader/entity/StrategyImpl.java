package com.algoTrader.entity;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.security.Forex;
import com.algoTrader.entity.strategy.CashBalance;
import com.algoTrader.util.RoundUtil;

public class StrategyImpl extends Strategy {

    public static final String BASE = "BASE";

    private static final long serialVersionUID = -2271735085273721632L;

    private static @Value("${order.initialMarginMarkup}") double initialMarginMarkup;


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

        // sum of all cashBalances of this strategy
        Collection<CashBalance> cashBalances = getCashBalances();
        for (CashBalance cashBalance : cashBalances) {
            amount += cashBalance.getAmountBaseDouble();
        }

        // sum of all FX positions
        List<Position> positions = getOpenFXPositions();
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
        List<Position> positions = getOpenTradeablePositions();
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
        List<Position> positions = getOpenTradeablePositions();
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
    public double getRedemptionValueDouble() {

        double redemptionValue = 0.0;
        List<Position> positions = getOpenTradeablePositions();
        for (Position position : positions) {
            redemptionValue += position.getRedemptionValueBaseDouble();
        }
        return redemptionValue;
    }

    @Override
    public double getMaxLossDouble() {

        double maxLoss = 0.0;
        List<Position> positions = getOpenTradeablePositions();
        for (Position position : positions) {
            maxLoss += position.getMaxLossBaseDouble();
        }
        return maxLoss;
    }

    @Override
    public double getLeverage() {

        double exposure = 0.0;
        List<Position> positions = getOpenTradeablePositions();
        for (Position position : positions) {
            exposure += position.getExposure();
        }

        return exposure / getNetLiqValueDouble();
    }

    @Override
    public double getPerformance() {

        if (getBenchmark() != null) {
            return getNetLiqValueDouble() / getBenchmark().doubleValue() - 1.0;
        } else {
            return Double.NaN;
        }
    }

    @Override
    public String toString() {

        return getName();
    }

    private List<Position> getOpenTradeablePositions() {
        return ServiceLocator.instance().getLookupService().getOpenTradeablePositionsByStrategy(getName());
    }

    private List<Position> getOpenFXPositions() {
        return ServiceLocator.instance().getLookupService().getOpenFXPositionsByStrategy(getName());
    }
}
