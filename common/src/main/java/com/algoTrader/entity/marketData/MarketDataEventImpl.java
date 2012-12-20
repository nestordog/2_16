package com.algoTrader.entity.marketData;

import java.math.BigDecimal;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.enumeration.Direction;
import com.algoTrader.util.CustomToStringStyle;

public abstract class MarketDataEventImpl extends MarketDataEvent {

    private static final long serialVersionUID = 8758212212560594623L;

    private static @Value("${simulation}") boolean simulation;

    @Override
    public BigDecimal getSettlement() {

        if (simulation && (super.getSettlement() == null || super.getSettlement().compareTo(new BigDecimal(0)) == 0)) {
            return getCurrentValue();
        } else {
            return super.getSettlement();
        }
    }

    @Override
    public double getCurrentValueDouble() {

        return getCurrentValue().doubleValue();
    }

    @Override
    public double getMarketValueDouble(Direction direction) {

        return getMarketValue(direction).doubleValue();
    }

    @Override
    public String toString() {

        return ToStringBuilder.reflectionToString(this, CustomToStringStyle.getInstance());
    }
}
