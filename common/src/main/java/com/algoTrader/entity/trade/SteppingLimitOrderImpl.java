package com.algoTrader.entity.trade;

import java.math.BigDecimal;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.ClassUtils;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.entity.security.SecurityFamily;
import com.algoTrader.enumeration.Side;
import com.algoTrader.util.RoundUtil;

public class SteppingLimitOrderImpl extends SteppingLimitOrder {

    private static final long serialVersionUID = 6631564632498034454L;

    private static @Value("${order.minSpreadPosition}") double minSpreadPosition;
    private static @Value("${order.maxSpreadPosition}") double maxSpreadPosition;
    private static @Value("${order.spreadPositionIncrement}") double spreadPositionIncrement;

    @Override
    public String toString() {

        //@formatter:off
        return getSide() + " " + getQuantity() + " "
            + ClassUtils.getShortClassName(this.getClass()) + " "
            + getSecurity().getSymbol() +
            " limit " + getLimit() +
            " maxLimit " + getMaxLimit() +
            " increment " + RoundUtil.getBigDecimal(getIncrement(), getSecurity().getSecurityFamily().getScale() + 1);
        //@formatter:on
    }

    @Override
    public void setDefaultLimits(double bid, double ask) {

        SecurityFamily family = getSecurity().getSecurityFamily();

        double spread = ask - bid;
        double increment = spreadPositionIncrement * spread;

        // the increment is not rounded as it will depend on the currentLimit
        setIncrement(increment);

        double limit;
        double maxLimit;
        if (Side.BUY.equals(getSide())) {
            double limitRaw = bid + minSpreadPosition * spread;
            double maxLimitRaw = bid + maxSpreadPosition * spread;
            limit = RoundUtil.roundToNextN(limitRaw, family.getTickSize(limitRaw, true), BigDecimal.ROUND_FLOOR);
            maxLimit = RoundUtil.roundToNextN(maxLimitRaw, family.getTickSize(maxLimitRaw, true), BigDecimal.ROUND_CEILING);
        } else {
            double limitRaw = ask - minSpreadPosition * spread;
            double maxLimitRaw = ask - maxSpreadPosition * spread;
            limit = RoundUtil.roundToNextN(limitRaw, family.getTickSize(limitRaw, true), BigDecimal.ROUND_CEILING);
            maxLimit = RoundUtil.roundToNextN(maxLimitRaw, family.getTickSize(maxLimitRaw, true), BigDecimal.ROUND_FLOOR);
        }

        // limit and maxLimit are correctly rounded according to tickSizePattern
        setLimit(RoundUtil.getBigDecimal(limit, family.getScale()));
        setMaxLimit(RoundUtil.getBigDecimal(maxLimit, family.getScale()));
    }

    @Override
    public SteppingLimitOrder adjustLimit() {

        SecurityFamily family = getSecurity().getSecurityFamily();

        BigDecimal newLimit;
        if (getSide().equals(Side.BUY)) {

            double tickSize = family.getTickSize(getLimit().doubleValue(), true);
            double increment = RoundUtil.roundToNextN(getIncrement(), tickSize, BigDecimal.ROUND_CEILING);
            BigDecimal roundedIncrement = RoundUtil.getBigDecimal(increment, family.getScale());
            newLimit = getLimit().add(roundedIncrement);
        } else {

            double tickSize = family.getTickSize(getLimit().doubleValue(), false);
            double increment = RoundUtil.roundToNextN(getIncrement(), tickSize, BigDecimal.ROUND_CEILING);
            BigDecimal roundedIncrement = RoundUtil.getBigDecimal(increment, family.getScale());
            newLimit = getLimit().subtract(roundedIncrement);
        }

        try {
            SteppingLimitOrder newOrder = (SteppingLimitOrder) BeanUtils.cloneBean(this);
            newOrder.setLimit(newLimit);
            return newOrder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean checkLimit() {

        SecurityFamily family = getSecurity().getSecurityFamily();

        if (getSide().equals(Side.BUY)) {

            double tickSize = family.getTickSize(getLimit().doubleValue(), true);
            double increment = RoundUtil.roundToNextN(getIncrement(), tickSize, BigDecimal.ROUND_CEILING);
            BigDecimal roundedIncrement = RoundUtil.getBigDecimal(increment, family.getScale());

            return getLimit().add(roundedIncrement).compareTo(getMaxLimit()) <= 0;
        } else {
            double tickSize = family.getTickSize(getLimit().doubleValue(), false);
            double increment = RoundUtil.roundToNextN(getIncrement(), tickSize, BigDecimal.ROUND_CEILING);
            BigDecimal roundedIncrement = RoundUtil.getBigDecimal(increment, family.getScale());

            return getLimit().subtract(roundedIncrement).compareTo(getMaxLimit()) >= 0;
        }
    }
}
