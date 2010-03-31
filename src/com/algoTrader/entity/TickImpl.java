package com.algoTrader.entity;

import java.math.BigDecimal;

import com.algoTrader.util.EsperService;
import com.algoTrader.util.PropertiesUtil;
import com.algoTrader.util.RoundUtil;

public class TickImpl extends com.algoTrader.entity.Tick {

    private static int lastTransactionAge = PropertiesUtil.getIntProperty("lastTransactionAge");
    private static int minVol = PropertiesUtil.getIntProperty("minVol");
    private static double spreadSlope = PropertiesUtil.getDoubleProperty("spreadSlope");
    private static double maxSpreadConstant = PropertiesUtil.getDoubleProperty("maxSpreadConstant");

    private static final long serialVersionUID = 7518020445322413106L;

    public BigDecimal getCurrentValue() {

        if (getLastDateTime() == null ||
                EsperService.getCurrentTime() - getLastDateTime().getTime() > lastTransactionAge) {
            if (getVolAsk() > minVol && getVolBid() > minVol) {
                return RoundUtil.getBigDecimal((getAsk().doubleValue() + getBid().doubleValue()) / 2d);
            } else {
                return getLast();
            }
        } else {
            return getLast();
        }
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

    public double getCurrentValueDouble() {

        return getCurrentValue().doubleValue();
    }
}
