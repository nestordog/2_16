package com.algoTrader.entity.trade;

import java.math.BigDecimal;

import org.apache.commons.lang.ClassUtils;

import com.algoTrader.ServiceLocator;
import com.algoTrader.enumeration.Side;
import com.algoTrader.util.Configuration;
import com.algoTrader.util.RoundUtil;

public class SteppingLimitOrderImpl extends SteppingLimitOrder {

    private static final long serialVersionUID = 6631564632498034454L;

    @Override
    public String toString() {

        return getSide() + " " + getQuantity() + " " + ClassUtils.getShortClassName(this.getClass()) + " " + getSecurity().getSymbol() + " limit " + getLimit()
                + " maxLimit " + getMaxLimit() + " increment " + getIncrement();
    }

    @Override
    public void setDefaultLimits(double bid, double ask) {

        Configuration configuration = ServiceLocator.instance().getConfiguration();
        double minSpreadPosition = configuration.getDouble("minSpreadPosition");
        double maxSpreadPosition = configuration.getDouble("maxSpreadPosition");
        double spreadPositionIncrement = configuration.getDouble("spreadPositionIncrement");

        double tickSize = getSecurity().getSecurityFamily().getTickSize();
        int scale = RoundUtil.getDigits(tickSize);

        double spread = ask - bid;
        double increment = spreadPositionIncrement * spread;
        double roundedIncrement = RoundUtil.roundToNextN(increment, tickSize, BigDecimal.ROUND_CEILING);

        double roundedLimit;
        double roundedMaxLimit;
        if (Side.BUY.equals(getSide())) {
            double limit = bid + minSpreadPosition * spread;
            double maxLimit = bid + maxSpreadPosition * spread;
            roundedLimit = RoundUtil.roundToNextN(limit, tickSize, BigDecimal.ROUND_FLOOR);
            roundedMaxLimit = RoundUtil.roundToNextN(maxLimit, tickSize, BigDecimal.ROUND_CEILING);
        } else {
            double limit = ask - minSpreadPosition * spread;
            double maxLimit = ask - maxSpreadPosition * spread;
            roundedLimit = RoundUtil.roundToNextN(limit, tickSize, BigDecimal.ROUND_CEILING);
            roundedMaxLimit = RoundUtil.roundToNextN(maxLimit, tickSize, BigDecimal.ROUND_FLOOR);
        }

        setLimit(RoundUtil.getBigDecimal(roundedLimit, scale));
        setMaxLimit(RoundUtil.getBigDecimal(roundedMaxLimit, scale));
        setIncrement(RoundUtil.getBigDecimal(roundedIncrement, scale));
    }
}
