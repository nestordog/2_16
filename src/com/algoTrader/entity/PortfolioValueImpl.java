package com.algoTrader.entity;

import com.algoTrader.util.RoundUtil;

public class PortfolioValueImpl extends com.algoTrader.entity.PortfolioValue {

    private static final long serialVersionUID = 7571299891766944493L;

    public java.math.BigDecimal getNetLiqValue() {

        return RoundUtil.getBigDecimal(getNetLiqValueDouble());
    }

    public double getNetLiqValueDouble() {

        return getCashBalance().doubleValue() + getSecuritiesCurrentValue().doubleValue();
    }
}
