package com.algoTrader.entity;

import java.math.BigDecimal;

import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.stockOption.StockOptionUtil;
import com.algoTrader.util.PropertiesUtil;
import com.algoTrader.util.RoundUtil;

public class StockOptionImpl extends com.algoTrader.entity.StockOption {

    private static final long serialVersionUID = -3168298592370987085L;

    private static final double commission = PropertiesUtil.getDoubleProperty("strategie.commission");

    public BigDecimal getCommission(long quantity, TransactionType transactionType) {

        if (TransactionType.SELL.equals(transactionType) || TransactionType.BUY.equals(transactionType)) {
            return RoundUtil.getBigDecimal(quantity * commission);
        } else {
            return new BigDecimal(0);
        }
    }

    public BigDecimal getCurrentValuePerContract() {

        return RoundUtil.getBigDecimal(getCurrentValuePerContractDouble());
    }

    public double getCurrentValuePerContractDouble() {

        Tick tick = getLastTick();
        if (tick != null) {
            return getContractSize() * getLastTick().getCurrentValueDouble();
        } else {
            return 0.0;
        }
    }

    public double getLeverage() {

        try {
            double underlyingSpot = getUnderlaying().getLastTick().getCurrentValueDouble();
            double currentValue = getLastTick().getCurrentValueDouble();
            double delta = StockOptionUtil.getDelta(this, currentValue, underlyingSpot);

            return underlyingSpot / currentValue * delta;

        } catch (Exception e) {

            return Double.NaN;
        }
    }
}
