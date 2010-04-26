package com.algoTrader.entity;

import java.math.BigDecimal;

import com.algoTrader.util.RoundUtil;

public class PositionImpl extends com.algoTrader.entity.Position {

    private static final long serialVersionUID = -2679980079043322328L;

    public BigDecimal getValue() {

        return RoundUtil.getBigDecimal(getValueDouble());
    }

    public double getValueDouble() {

        if (getQuantity() != 0) {
            return (double)getQuantity() * getSecurity().getCurrentValuePerContractDouble();
        } else {
            return 0.0;
        }
    }

    public double getMarginDouble() {

        if (getQuantity() != 0 && getMargin() != null) {
                return getMargin().doubleValue();
        } else {
            return 0.0;
        }
    }

    public double getRedemptionValue() {

        if (getQuantity() != 0 && getExitValue() != null) {

            return -(double)getQuantity() * getSecurity().getContractSize() * getExitValue().doubleValue();
        } else {
            return 0.0;
        }
    }
}
