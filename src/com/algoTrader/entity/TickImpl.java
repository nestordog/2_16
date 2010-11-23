package com.algoTrader.entity;

import java.math.BigDecimal;

import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.RoundUtil;

public class TickImpl extends com.algoTrader.entity.Tick {

    private static boolean simulation = ConfigurationUtil.getBaseConfig().getBoolean("simulation");

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

    public BigDecimal getBid() {

        if (simulation) {
            return getLast();
        } else {
            return super.getBid();
        }
    }

    public BigDecimal getAsk() {

        if (simulation) {
            return getLast();
        } else {
            return super.getAsk();
        }
    }

    public BigDecimal getSettlement() {

        if (simulation) {
            return getLast();
        } else {
            return super.getSettlement();
        }
    }

    public void validate() {

        getSecurity().validateTick(this);
    }
}
