package com.algoTrader.entity.trade;

import org.apache.commons.lang.ClassUtils;

import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.RoundUtil;

public class SteppingLimitOrderImpl extends SteppingLimitOrder {

    private static final long serialVersionUID = 6631564632498034454L;

    private static double minSpreadPosition = ConfigurationUtil.getBaseConfig().getDouble("minSpreadPosition");
    private static double maxSpreadPosition = ConfigurationUtil.getBaseConfig().getDouble("maxSpreadPosition");
    private static double spreadPositionIncrement = ConfigurationUtil.getBaseConfig().getDouble("spreadPositionIncrement");

    public String toString() {

        return getSide() + " " + getQuantity() + " " + ClassUtils.getShortClassName(this.getClass()) + " " + getSecurity().getSymbol() + " limit " + getLimit()
                + " maxLimit " + getMaxLimit() + " increment " + getIncrement();
    }

    @Override
    public void setDefaultBuyLimits(double bid, double ask, double tickSize) {

        double spread = ask - bid;
        double limit = bid + minSpreadPosition * spread;
        double maxLimit = bid + maxSpreadPosition * spread;
        double increment = spreadPositionIncrement * spread;
        int scale = RoundUtil.getDigits(tickSize);

        double roundedLimit = RoundUtil.roundToNextN(limit, tickSize);

        setLimit(RoundUtil.getBigDecimal(roundedLimit, scale));
        setMaxLimit(RoundUtil.getBigDecimal(maxLimit, scale));
        setIncrement(RoundUtil.getBigDecimal(increment, scale));
    }

    @Override
    public void setDefaultSellLimits(double bid, double ask, double tickSize) {

        double spread = ask - bid;
        double limit = ask - minSpreadPosition * spread;
        double maxLimit = ask - maxSpreadPosition * spread;
        double increment = spreadPositionIncrement * spread;
        int scale = RoundUtil.getDigits(tickSize);

        double roundedLimit = RoundUtil.roundToNextN(limit, tickSize);

        setLimit(RoundUtil.getBigDecimal(roundedLimit, scale));
        setMaxLimit(RoundUtil.getBigDecimal(maxLimit, scale));
        setIncrement(RoundUtil.getBigDecimal(increment, scale));
    }
}
