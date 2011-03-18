package com.algoTrader.entity;

import org.apache.commons.math.MathException;
import org.apache.log4j.Logger;

import com.algoTrader.service.TickServiceException;
import com.algoTrader.stockOption.StockOptionUtil;
import com.algoTrader.util.MyLogger;

public class StockOptionImpl extends StockOption {

    private static final long serialVersionUID = -3168298592370987085L;
    private static Logger logger = MyLogger.getLogger(StockOptionImpl.class.getName());

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

    public double getMargin() {

        Tick stockOptionTick = getLastTick();
        Tick underlayingTick = getUnderlaying().getLastTick();

        double marginPerContract = 0;
        if (stockOptionTick != null && underlayingTick != null && stockOptionTick.getCurrentValueDouble() > 0.0) {

            double stockOptionSettlement = stockOptionTick.getSettlement().doubleValue();
            double underlayingSettlement = underlayingTick.getSettlement().doubleValue();
            int contractSize = getSecurityFamily().getContractSize();
            try {
                marginPerContract = StockOptionUtil.getMaintenanceMargin(this, stockOptionSettlement, underlayingSettlement) * contractSize;
            } catch (MathException e) {
                logger.warn("could not calculate margin for " + getSymbol(), e);
            }
        } else {
            logger.warn("no last tick available or currentValue to low to set margin on " + getSymbol());
        }
        return marginPerContract;
    }
}
