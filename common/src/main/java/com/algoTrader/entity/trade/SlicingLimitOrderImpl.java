package com.algoTrader.entity.trade;

import java.math.BigDecimal;

import org.apache.commons.beanutils.BeanUtils;

import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.security.SecurityFamily;
import com.algoTrader.enumeration.Side;

public class SlicingLimitOrderImpl extends SlicingLimitOrder {

    private static final long serialVersionUID = -9017761050542085585L;

    @Override
    public SlicingLimitOrder nextSlice() {

        SecurityFamily family = getSecurity().getSecurityFamily();
        Tick tick = getSecurity().getLastTick();

        // quantity
        long quantity = Math.min(getLotQuantity(), getTotalQuantity() - getFilledQuantity());

        // limit (at least one tick above market but do not exceed the spread)
        BigDecimal limit;
        if (Side.BUY.equals(getSide())) {

            limit = family.adjustPrice(tick.getBid(), getCurrentOffsetTicks());

            if (limit.compareTo(tick.getBid()) <= 0) {
                limit = family.adjustPrice(tick.getBid(), 1);
                setCurrentOffsetTicks(1);
            } else if (limit.compareTo(tick.getAsk()) > 0) {
                limit = tick.getAsk();
                setCurrentOffsetTicks(family.getSpreadTicks(tick.getBid(), tick.getAsk()));
            }
        } else {

            limit = family.adjustPrice(tick.getAsk(), -getCurrentOffsetTicks());

            if (limit.compareTo(tick.getAsk()) >= 0) {
                limit = family.adjustPrice(tick.getAsk(), -1);
                setCurrentOffsetTicks(1);
            } else if (limit.compareTo(tick.getBid()) < 0) {
                limit = tick.getBid();
                setCurrentOffsetTicks(family.getSpreadTicks(tick.getBid(), tick.getAsk()));
            }
        }

        try {

            SlicingLimitOrderImpl newOrder = (SlicingLimitOrderImpl) BeanUtils.cloneBean(this);
            newOrder.setQuantity(quantity);
            newOrder.setLimit(limit);

            return newOrder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {

        //@formatter:off
        return super.toString() +
            " totalQuantity " + getTotalQuantity() +
            " lotQuantity " + getLotQuantity() +
            " filledQuantity " + getFilledQuantity() +
            " currentOffsetTicks " + getCurrentOffsetTicks() +
            " currentLimit " + getLimit();
        //@formatter:on
    }
}
