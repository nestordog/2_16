package com.algoTrader.entity.trade;

import java.math.BigDecimal;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.security.SecurityFamily;
import com.algoTrader.enumeration.Side;
import com.algoTrader.util.RoundUtil;

public class VariableIncrementalLimitOrderImpl extends VariableIncrementalLimitOrder {

    private static final long serialVersionUID = 6631564632498034454L;

    private static @Value("${order.variableIncrementalLimitOrder.minSpreadPosition}") double minSpreadPosition;
    private static @Value("${order.variableIncrementalLimitOrder.maxSpreadPosition}") double maxSpreadPosition;
    private static @Value("${order.variableIncrementalLimitOrder.spreadPositionIncrement}") double spreadPositionIncrement;

    @Override
    public String toString() {

        //@formatter:off
        return super.toString() +
            " startLimit " + getStartLimit() +
            " endLimit " + getEndLimit() +
            " currentLimit " + getLimit() +
            " increment " + RoundUtil.getBigDecimal(getIncrement(), getSecurity().getSecurityFamily().getScale() + 1);
        //@formatter:on
    }

    @Override
    public void init(Tick tick) {

        // make sure there is a tick
        if (tick == null) {
            tick = getSecurity().getLastTick();
        }

        SecurityFamily family = getSecurity().getSecurityFamily();

        double bidDouble = tick.getBid().doubleValue();
        double askDouble = tick.getAsk().doubleValue();
        double spread = askDouble - bidDouble;
        double increment = spreadPositionIncrement * spread;

        // the increment is not rounded as it will depend on the currentLimit
        setIncrement(increment);

        double limit;
        double maxLimit;
        if (Side.BUY.equals(getSide())) {
            double limitRaw = bidDouble + minSpreadPosition * spread;
            double maxLimitRaw = bidDouble + maxSpreadPosition * spread;
            limit = RoundUtil.roundToNextN(limitRaw, family.getTickSize(limitRaw, true), BigDecimal.ROUND_FLOOR);
            maxLimit = RoundUtil.roundToNextN(maxLimitRaw, family.getTickSize(maxLimitRaw, true), BigDecimal.ROUND_CEILING);
        } else {
            double limitRaw = askDouble - minSpreadPosition * spread;
            double maxLimitRaw = askDouble - maxSpreadPosition * spread;
            limit = RoundUtil.roundToNextN(limitRaw, family.getTickSize(limitRaw, true), BigDecimal.ROUND_CEILING);
            maxLimit = RoundUtil.roundToNextN(maxLimitRaw, family.getTickSize(maxLimitRaw, true), BigDecimal.ROUND_FLOOR);
        }

        // limit and maxLimit are correctly rounded according to tickSizePattern
        setStartLimit(RoundUtil.getBigDecimal(limit, family.getScale()));
        setEndLimit(RoundUtil.getBigDecimal(maxLimit, family.getScale()));
        setLimit(getStartLimit());
    }

    @Override
    public IncrementalLimitOrder adjustLimit() {

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
            VariableIncrementalLimitOrderImpl newOrder = (VariableIncrementalLimitOrderImpl) BeanUtils.cloneBean(this);
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

            return getLimit().add(roundedIncrement).compareTo(getEndLimit()) <= 0;
        } else {
            double tickSize = family.getTickSize(getLimit().doubleValue(), false);
            double increment = RoundUtil.roundToNextN(getIncrement(), tickSize, BigDecimal.ROUND_CEILING);
            BigDecimal roundedIncrement = RoundUtil.getBigDecimal(increment, family.getScale());

            return getLimit().subtract(roundedIncrement).compareTo(getEndLimit()) >= 0;
        }
    }
}
