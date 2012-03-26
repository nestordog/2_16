package com.algoTrader.entity.trade;

import java.math.BigDecimal;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.ClassUtils;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.entity.marketData.Tick;
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
    public void init(Tick tick) {

        // make sure there is a tick
        if (tick == null) {
            tick = getSecurity().getLastTick();
        }

        SecurityFamily family = getSecurity().getSecurityFamily();

        // check spead and adjust offsetTicks if spead is too narrow
        int spreadTicks = family.getSpreadTicks(tick.getBid(), tick.getAsk());
        int adjustedStartOffsetTicks = startOffsetTicks;
        int adjustedEndOffsetTicks = endOffsetTicks;
        if (spreadTicks < 0) {

            throw new RuntimeException("markets are crossed: bid " + tick.getBid() + " ask " + tick.getAsk());

        } else if (spreadTicks < (startOffsetTicks - endOffsetTicks)) {

            // first reduce startOffsetTicks to min 0
            adjustedStartOffsetTicks = Math.max(spreadTicks + endOffsetTicks, 0);
            if (spreadTicks < (adjustedStartOffsetTicks - endOffsetTicks)) {

                // if necessary also increase endOffstTicks to max 0
                adjustedEndOffsetTicks = Math.min(adjustedStartOffsetTicks - spreadTicks, 0);
            }
        }

        if (Side.BUY.equals(getSide())) {
            setStartLimit(family.adjustPrice(tick.getBid(), adjustedStartOffsetTicks));
            setEndLimit(family.adjustPrice(tick.getAsk(), adjustedEndOffsetTicks));

            if (getStartLimit().doubleValue() <= 0.0) {
                setStartLimit(family.adjustPrice(new BigDecimal(0), 1));
            }

            setLimit(getStartLimit());

        } else {

            setStartLimit(family.adjustPrice(tick.getAsk(), -adjustedStartOffsetTicks));
            setEndLimit(family.adjustPrice(tick.getBid(), -adjustedEndOffsetTicks));

            if (getStartLimit().doubleValue() <= 0.0) {
                setStartLimit(family.adjustPrice(new BigDecimal(0), 1));
            }

            if (getEndLimit().doubleValue() <= 0.0) {
                setEndLimit(family.adjustPrice(new BigDecimal(0), 1));
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
