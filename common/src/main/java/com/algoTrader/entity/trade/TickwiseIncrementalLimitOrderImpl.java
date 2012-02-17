package com.algoTrader.entity.trade;

import java.math.BigDecimal;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.ClassUtils;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.entity.security.SecurityFamily;
import com.algoTrader.enumeration.Side;

public class TickwiseIncrementalLimitOrderImpl extends TickwiseIncrementalLimitOrder {

    private static final long serialVersionUID = 6631564632498034454L;

    private static @Value("${order.tickwiseIncrementalLimitOrder.startOffsetTicks}") int startOffsetTicks;
    private static @Value("${order.tickwiseIncrementalLimitOrder.endOffsetTicks}") int endOffsetTicks;

    @Override
    public String toString() {

        //@formatter:off
        return getSide() + " " + getQuantity() + " "
            + ClassUtils.getShortClassName(this.getClass()) + " "
            + getSecurity().getSymbol() +
            " startLimit " + getStartLimit() +
            " endLimit " + getEndLimit() +
            " currentLimit " + getLimit();
        //@formatter:on
    }

    @Override
    public void setDefaultLimits(BigDecimal bid, BigDecimal ask) {

        SecurityFamily family = getSecurity().getSecurityFamily();

        if (Side.BUY.equals(getSide())) {
            setStartLimit(family.adjustPrice(bid, startOffsetTicks));
            setEndLimit(family.adjustPrice(ask, endOffsetTicks));

            if (getStartLimit().doubleValue() <= 0.0) {
                setStartLimit(family.adjustPrice(new BigDecimal(0), 1));
            }

            if (getStartLimit().doubleValue() > getEndLimit().doubleValue()) {
                throw new RuntimeException("startLimit " + getStartLimit() + " cannot be greater than endLimit " + getEndLimit());
            }

            setLimit(getStartLimit());

        } else {

            setStartLimit(family.adjustPrice(ask, -startOffsetTicks));
            setEndLimit(family.adjustPrice(bid, -endOffsetTicks));

            if (getStartLimit().doubleValue() <= 0.0) {
                setStartLimit(family.adjustPrice(new BigDecimal(0), 1));
            }

            if (getEndLimit().doubleValue() <= 0.0) {
                setEndLimit(family.adjustPrice(new BigDecimal(0), 1));
            }

            if (getStartLimit().doubleValue() < getEndLimit().doubleValue()) {
                throw new RuntimeException("startLimit " + getStartLimit() + " cannot be smaller than endLimit " + getEndLimit());
            }

            setLimit(getStartLimit());
        }
    }

    @Override
    public TickwiseIncrementalLimitOrderImpl adjustLimit() {

        SecurityFamily family = getSecurity().getSecurityFamily();

        BigDecimal newLimit;
        if (getSide().equals(Side.BUY)) {
            newLimit = family.adjustPrice(getLimit(), 1);
        } else {
            newLimit = family.adjustPrice(getLimit(), -1);
        }

        try {
            TickwiseIncrementalLimitOrderImpl newOrder = (TickwiseIncrementalLimitOrderImpl) BeanUtils.cloneBean(this);
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
            return family.adjustPrice(getLimit(), 1).compareTo(getEndLimit()) <= 0;
        } else {
            return family.adjustPrice(getLimit(), -1).compareTo(getEndLimit()) >= 0;
        }
    }
}
