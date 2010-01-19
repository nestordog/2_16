package com.algoTrader.entity;

import java.math.BigDecimal;

import com.algoTrader.util.EsperService;
import com.algoTrader.util.PropertiesUtil;

public class TickImpl extends com.algoTrader.entity.Tick {

    private static int lastTransactionAge = Integer.parseInt(PropertiesUtil.getProperty("lastTransactionAge"));
    private static int minVol = Integer.parseInt(PropertiesUtil.getProperty("minVol"));

    private static final long serialVersionUID = 7518020445322413106L;

    public BigDecimal getCurrentValue() {

        long currenttime = EsperService.getEPServiceInstance().getEPRuntime().getCurrentTime();

        if (currenttime - getLastDateTime().getTime() > lastTransactionAge) {

            if (getVolAsk() > minVol && getVolBid() > minVol) {
                return (getAsk().add(getBid()).divide(new BigDecimal(2)));
            } else {
                return getLast();
            }
        } else {
            return getLast();
        }
    }
}
