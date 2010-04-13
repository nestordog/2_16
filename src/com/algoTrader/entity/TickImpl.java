package com.algoTrader.entity;

import java.math.BigDecimal;

import com.algoTrader.util.PropertiesUtil;
import com.algoTrader.util.RoundUtil;

public class TickImpl extends com.algoTrader.entity.Tick {

    private static int minVol = PropertiesUtil.getIntProperty("minVol");
    private static double spreadSlope = PropertiesUtil.getDoubleProperty("spreadSlope");
    private static double maxSpreadConstant = PropertiesUtil.getDoubleProperty("maxSpreadConstant");
    private static boolean simulation = PropertiesUtil.getBooleanProperty("simulation");

    private static final long serialVersionUID = 7518020445322413106L;

    /**
     * 1. in simulation only "last" is used
     * 2. on indexes (smi & vsmi) there is no bid and ask (only last)
     * Note: ticks that are not valid (i.e. low volume) are not fed into esper, so we don't need to check
     */
    public BigDecimal getCurrentValue() {

        if (simulation) {
            return getLast();
        } else {
            if (this.getSecurity() instanceof StockOption) {
                return RoundUtil.getBigDecimal((getAsk().doubleValue() + getBid().doubleValue()) / 2.0);
            } else {
                return getLast();
            }
        }
    }

    public double getCurrentValueDouble() {

        return getCurrentValue().doubleValue();
    }

    public BigDecimal getSettlement() {

        if (super.getSettlement().doubleValue() == 0) {
            return getCurrentValue();
        } else {
            return super.getSettlement();
        }
    }

    public double getSettlementDouble() {

        return getSettlement().doubleValue();
    }

    public boolean isValid() {

        if (getSecurity() instanceof StockOption) {
            if (getVolAsk() <= minVol || getVolBid() <= minVol) return false;

            double average = getAsk().doubleValue() * getBid().doubleValue() / 2.0;
            double spread = getAsk().doubleValue() - getBid().doubleValue();
            double maxSpread = average * spreadSlope + maxSpreadConstant;

            if (spread > maxSpread) return false;
        }

        return true;
    }
}
