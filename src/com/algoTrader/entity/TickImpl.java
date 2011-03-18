package com.algoTrader.entity;

import java.math.BigDecimal;

import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.RoundUtil;

public class TickImpl extends Tick {

    private static boolean simulation = ConfigurationUtil.getBaseConfig().getBoolean("simulation");

    private static final long serialVersionUID = 7518020445322413106L;

    public BigDecimal getCurrentValue() {

        return RoundUtil.getBigDecimal((getAsk().doubleValue() + getBid().doubleValue()) / 2.0);

    }

    public double getCurrentValueDouble() {

        return getCurrentValue().doubleValue();
    }

    public BigDecimal getBid() {

        if (simulation && super.getBid().equals(new BigDecimal(0))) {
            return getLast();
        } else {
            return super.getBid();
        }
    }

    public BigDecimal getAsk() {

        if (simulation && super.getAsk().equals(new BigDecimal(0))) {
            return getLast();
        } else {
            return super.getAsk();
        }
    }

    public BigDecimal getSettlement() {

        if (simulation && super.getSettlement().equals(new BigDecimal(0))) {
            return getLast();
        } else {
            return super.getSettlement();
        }
    }

    public void validate() {

        getSecurity().validateTick(this);
    }
}
