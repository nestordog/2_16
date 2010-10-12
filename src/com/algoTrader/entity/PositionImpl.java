package com.algoTrader.entity;

import java.math.BigDecimal;

import com.algoTrader.util.RoundUtil;

public class PositionImpl extends com.algoTrader.entity.Position {

    private static final long serialVersionUID = -2679980079043322328L;

    public boolean isOpen() {

        return (getQuantity() != 0);
    }

    public BigDecimal getCurrentValue() {

        return RoundUtil.getBigDecimal(getCurrentValueDouble());
    }

    public double getCurrentValueDouble() {

        if (isOpen()) {
            return getQuantity() * getSecurity().getCurrentValuePerContractDouble();
        } else {
            return 0.0;
        }
    }

    public double getMaintenanceMarginDouble() {

        if (isOpen() && getMaintenanceMargin() != null) {
                return getMaintenanceMargin().doubleValue();
        } else {
            return 0.0;
        }
    }

    public double getRedemptionValue() {

        if (isOpen() && getExitValue() != null) {

            return -(double)getQuantity() * getSecurity().getContractSize() * getExitValue().doubleValue();
        } else {
            return 0.0;
        }
    }

    public double getDeltaRisk() {

        if (getSecurity() instanceof StockOption) {

            StockOption stockOption = (StockOption) getSecurity();

            return getCurrentValueDouble() * stockOption.getLeverage();

        } else {
            return 0;
        }
    }
}
