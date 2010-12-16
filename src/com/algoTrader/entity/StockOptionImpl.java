package com.algoTrader.entity;

import com.algoTrader.service.TickServiceException;
import com.algoTrader.stockOption.StockOptionUtil;
import com.algoTrader.util.ConfigurationUtil;

public class StockOptionImpl extends StockOption {

    private static final long serialVersionUID = -3168298592370987085L;

    private static int minVol = ConfigurationUtil.getBaseConfig().getInt("minVol");

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

        SecurityFamily family = tick.getSecurity().getSecurityFamily();
        int contractSize = family.getContractSize();
        double maxSpreadSlope = family.getMaxSpreadSlope();
        double maxSpreadConstant = family.getMaxSpreadConstant();

        double mean = contractSize * (tick.getAsk().doubleValue() + tick.getBid().doubleValue()) / 2.0;
        double spread = contractSize * (tick.getAsk().doubleValue() - tick.getBid().doubleValue());
        double maxSpread = mean * maxSpreadSlope + maxSpreadConstant;

        if (spread > maxSpread) {
            throw new TickServiceException("spread (" + spread + ") is higher than maxSpread (" + maxSpread + ") for security " + getSymbol());
        }
    }
}
