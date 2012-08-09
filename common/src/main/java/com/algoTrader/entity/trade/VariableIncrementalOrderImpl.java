package com.algoTrader.entity.trade;

import java.math.BigDecimal;

import org.apache.commons.beanutils.BeanUtils;

import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.security.SecurityFamily;
import com.algoTrader.enumeration.Side;
import com.algoTrader.util.RoundUtil;

public class VariableIncrementalOrderImpl extends VariableIncrementalOrder {

    private static final long serialVersionUID = 6631564632498034454L;

    private BigDecimal startLimit;
    private BigDecimal endLimit;
    private BigDecimal currentLimit;
    private double increment;

    @Override
    public String getDescription() {
        //@formatter:off
            return "startOffsetPct: " + getStartOffsetPct() +
            " endOffsetPct: " + getEndOffsetPct() +
            " spreadPositionIncrement: " + getIncrement() +
            " startLimit: " + this.startLimit +
            " endLimit: " + this.endLimit +
            " currentLimit: " + this.currentLimit +
            " increment: " + RoundUtil.getBigDecimal(this.increment, getSecurity().getSecurityFamily().getScale() + 1);
        //@formatter:on
    }

    @Override
    public void validate() throws OrderValidationException {

        if (getIncrement() == 0.0) {
            throw new OrderValidationException("increment cannot be 0 for " + this);
        }
    }

    @Override
    public LimitOrder firstOrder() {

        // make sure there is a tick
        Tick tick = getSecurity().getLastTick();

        SecurityFamily family = getSecurity().getSecurityFamily();

        double bidDouble = tick.getBid().doubleValue();
        double askDouble = tick.getAsk().doubleValue();
        double spread = askDouble - bidDouble;
        this.increment = getIncrement() * spread;

        double limit;
        double maxLimit;
        if (Side.BUY.equals(getSide())) {
            double limitRaw = bidDouble + getStartOffsetPct() * spread;
            double maxLimitRaw = bidDouble + getEndOffsetPct() * spread;
            limit = RoundUtil.roundToNextN(limitRaw, family.getTickSize(limitRaw, true), BigDecimal.ROUND_FLOOR);
            maxLimit = RoundUtil.roundToNextN(maxLimitRaw, family.getTickSize(maxLimitRaw, true), BigDecimal.ROUND_CEILING);
        } else {
            double limitRaw = askDouble - getStartOffsetPct() * spread;
            double maxLimitRaw = askDouble - getEndOffsetPct() * spread;
            limit = RoundUtil.roundToNextN(limitRaw, family.getTickSize(limitRaw, true), BigDecimal.ROUND_CEILING);
            maxLimit = RoundUtil.roundToNextN(maxLimitRaw, family.getTickSize(maxLimitRaw, true), BigDecimal.ROUND_FLOOR);
        }

        // limit and maxLimit are correctly rounded according to tickSizePattern
        this.startLimit = RoundUtil.getBigDecimal(limit, family.getScale());
        this.endLimit = RoundUtil.getBigDecimal(maxLimit, family.getScale());
        this.currentLimit = this.startLimit;

        LimitOrder order = LimitOrder.Factory.newInstance();
        order.setSecurity(this.getSecurity());
        order.setStrategy(this.getStrategy());
        order.setSide(this.getSide());
        order.setQuantity(this.getQuantity());
        order.setLimit(this.startLimit);

        return order;
    }

    @Override
    public LimitOrder modifyOrder() {

        SecurityFamily family = getSecurity().getSecurityFamily();

        BigDecimal newLimit;
        if (getSide().equals(Side.BUY)) {

            double tickSize = family.getTickSize(this.currentLimit.doubleValue(), true);
            double increment = RoundUtil.roundToNextN(this.increment, tickSize, BigDecimal.ROUND_CEILING);
            BigDecimal roundedIncrement = RoundUtil.getBigDecimal(increment, family.getScale());
            newLimit = this.currentLimit.add(roundedIncrement);
        } else {

            double tickSize = family.getTickSize(this.currentLimit.doubleValue(), false);
            double increment = RoundUtil.roundToNextN(this.increment, tickSize, BigDecimal.ROUND_CEILING);
            BigDecimal roundedIncrement = RoundUtil.getBigDecimal(increment, family.getScale());
            newLimit = this.currentLimit.subtract(roundedIncrement);
        }

        try {
            LimitOrder newOrder = (LimitOrder) BeanUtils.cloneBean(this);
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

            double tickSize = family.getTickSize(this.currentLimit.doubleValue(), true);
            double increment = RoundUtil.roundToNextN(this.increment, tickSize, BigDecimal.ROUND_CEILING);
            BigDecimal roundedIncrement = RoundUtil.getBigDecimal(increment, family.getScale());

            return this.currentLimit.add(roundedIncrement).compareTo(this.endLimit) <= 0;
        } else {
            double tickSize = family.getTickSize(this.currentLimit.doubleValue(), false);
            double increment = RoundUtil.roundToNextN(this.increment, tickSize, BigDecimal.ROUND_CEILING);
            BigDecimal roundedIncrement = RoundUtil.getBigDecimal(increment, family.getScale());

            return this.currentLimit.subtract(roundedIncrement).compareTo(this.endLimit) >= 0;
        }
    }

    @Override
    public void done() {
        // do nothing
    }
}
