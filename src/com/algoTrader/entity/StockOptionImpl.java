package com.algoTrader.entity;

import java.math.BigDecimal;

import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.service.TickServiceException;
import com.algoTrader.stockOption.StockOptionUtil;
import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.RoundUtil;

public class StockOptionImpl extends com.algoTrader.entity.StockOption {

    private static final long serialVersionUID = -3168298592370987085L;

    private static final double commission = ConfigurationUtil.getBaseConfig().getDouble("strategie.commission");
    private static int minVol = ConfigurationUtil.getBaseConfig().getInt("minVol");
    private static double maxSpreadSlope = ConfigurationUtil.getBaseConfig().getDouble("strategie.maxSpreadSlope");
    private static double maxSpreadConstant = ConfigurationUtil.getBaseConfig().getDouble("strategie.maxSpreadConstant");

    public BigDecimal getCommission(long quantity, TransactionType transactionType) {

        if (TransactionType.SELL.equals(transactionType) || TransactionType.BUY.equals(transactionType)) {
            return RoundUtil.getBigDecimal(quantity * commission);
        } else {
            return new BigDecimal(0);
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

    public void validateTick(Tick tick) {

        validateTickVol(tick);
        validateTickSpread(tick);
    }

    public void validateTickVol(Tick tick) {

        if (tick.getVolAsk() <= minVol) {
            throw new TickServiceException("vol ask (" + tick.getVolAsk() + ") ist too low for security " + getSymbol());
        }

        if (tick.getVolBid() <= minVol) {
            throw new TickServiceException("vol bid (" + tick.getVolBid() + ") ist too low for security " + getSymbol());
        }
    }

    public void validateTickSpread(Tick tick) {

        double mean = getContractSize() * (tick.getAsk().doubleValue() + tick.getBid().doubleValue()) / 2.0;
        double spread = getContractSize() * (tick.getAsk().doubleValue() - tick.getBid().doubleValue());
        double maxSpread = mean * maxSpreadSlope + maxSpreadConstant;

        if (spread > maxSpread) {
            throw new TickServiceException("spread (" + spread + ") is higher than maxSpread (" + maxSpread + ") for security " + getSymbol());
        }
    }
}
