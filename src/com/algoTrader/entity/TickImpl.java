package com.algoTrader.entity;

import java.math.BigDecimal;

import com.algoTrader.util.EsperService;
import com.algoTrader.util.PropertiesUtil;

public class TickImpl extends com.algoTrader.entity.Tick {

    private static int lastTransactionAge = Integer.parseInt(PropertiesUtil.getProperty("lastTransactionAge"));
    private static int minVol = Integer.parseInt(PropertiesUtil.getProperty("minVol"));
    private static double spreadSlope = Double.parseDouble(PropertiesUtil.getProperty("spreadSlope"));
    private static double maxSpreadConstant = Double.parseDouble(PropertiesUtil.getProperty("maxSpreadConstant"));

    private static final long serialVersionUID = 7518020445322413106L;

    public BigDecimal getCurrentValue() {

        if (getLastDateTime() == null ||
                EsperService.getEPServiceInstance().getEPRuntime().getCurrentTime() - getLastDateTime().getTime() > lastTransactionAge) {
            if (getVolAsk() > minVol && getVolBid() > minVol) {
                return (getAsk().add(getBid()).divide(new BigDecimal(2)));
            } else {
                return getLast();
            }
        } else {
            return getLast();
        }
    }

    public BigDecimal getSettlement() {

        if ((new BigDecimal(0)).equals(super.getSettlement())) {
            return getCurrentValue();
        } else {
            return super.getSettlement();
        }
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
