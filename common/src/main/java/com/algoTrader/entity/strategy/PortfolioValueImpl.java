package com.algoTrader.entity.strategy;

import java.text.SimpleDateFormat;

import com.algoTrader.util.RoundUtil;

public class PortfolioValueImpl extends PortfolioValue {

    private static final long serialVersionUID = -3646704287725745092L;
    private static SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy hh:mm:ss");

    @Override
    public String toString() {

        //@formatter:off
        return format.format(getDateTime()) + " " + getStrategy() +
                " netLiqValue: " + getNetLiqValue() +
                " securitiesCurrentValue: " + getSecuritiesCurrentValue() +
                " cashBalance: " + getCashBalance() +
                " maintenanceMargin: " +getMaintenanceMargin() +
                " leverage: " + RoundUtil.getBigDecimal(getLeverage()) +
                " allocation: " + RoundUtil.getBigDecimal(getAllocation()) +
                ((getCashFlow() != null) ? " cashFlow: " + getCashFlow() : "");
        //@formatter:on
    }

    @Override
    public double getNetLiqValueDouble() {

        return getNetLiqValue().doubleValue();
    }

    @Override
    public double getSecuritiesCurrentValueDouble() {

        return getSecuritiesCurrentValue().doubleValue();
    }

    @Override
    public double getCashBalanceDouble() {

        return getCashBalance().doubleValue();
    }

    @Override
    public double getMaintenanceMarginDouble() {

        return getMaintenanceMargin().doubleValue();
    }
}
