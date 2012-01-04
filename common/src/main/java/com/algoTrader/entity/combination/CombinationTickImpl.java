package com.algoTrader.entity.combination;

import java.math.BigDecimal;

import com.algoTrader.util.RoundUtil;

public class CombinationTickImpl extends CombinationTick {

    private static final long serialVersionUID = -843461058228285697L;

    @Override
    public BigDecimal getCurrentValue() {

        return RoundUtil.getBigDecimal((getAsk().doubleValue() + getBid().doubleValue()) / 2.0);
    }

    @Override
    public double getCurrentValueDouble() {

        return getCurrentValue().doubleValue();
    }
}
