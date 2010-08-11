package com.algoTrader.entity;

import java.math.BigDecimal;

import com.algoTrader.util.RoundUtil;

public class PositionImpl extends com.algoTrader.entity.Position {

    private static final long serialVersionUID = -2679980079043322328L;

    public boolean isOpen() {

        return (getQuantity() != 0);
    }

    public BigDecimal getValue() {

        return RoundUtil.getBigDecimal(getValueDouble());
    }

    public double getValueDouble() {

        if (isOpen()) {
            return getQuantity() * getSecurity().getCurrentValuePerContractDouble();
        } else {
            return 0.0;
        }
    }

    public double getMarginDouble() {

        if (isOpen() && getMargin() != null) {
                return getMargin().doubleValue();
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
}
