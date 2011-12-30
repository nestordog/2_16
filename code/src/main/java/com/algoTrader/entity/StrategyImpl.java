package com.algoTrader.entity;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.security.Forex;
import com.algoTrader.util.RoundUtil;

public class StrategyImpl extends Strategy {

    private static final long serialVersionUID = -2271735085273721632L;

    public static final String BASE = "BASE";

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
        List<Position> positions = getOpenPositions();
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
        List<Position> positions = getOpenPositions();
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

        double initialMarginMarkup = ServiceLocator.instance().getConfiguration().getInitialMarginMarkup();
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
        List<Position> positions = getOpenPositions();
        for (Position position : positions) {
            redemptionValue += position.getRedemptionValueBaseDouble();
        }
        return redemptionValue;
    }

    @Override
    public double getMaxLossDouble() {

        double maxLoss = 0.0;
        List<Position> positions = getOpenPositions();
        for (Position position : positions) {
            maxLoss += position.getMaxLossBaseDouble();
        }
        return maxLoss;
    }

    @Override
    public double getLeverage() {

        double exposure = 0.0;
        List<Position> positions = getOpenPositions();
        for (Position position : positions) {
            exposure += position.getExposure();
        }

        return exposure / getNetLiqValueDouble();
    }

    @Override
    public String toString() {

        return getName();
    }

    private List<Position> getOpenPositions() {
        return ServiceLocator.instance().getLookupService().getOpenPositionsByStrategy(getName());
    }

    private List<Position> getOpenFXPositions() {
        return ServiceLocator.instance().getLookupService().getOpenFXPositionsByStrategy(getName());
    }

    @SuppressWarnings("unchecked")
    private List<CashBalance> getCashBalancesBase() {
        return ServiceLocator.instance().getLookupService().getCashBalancesBase();
    }
}
