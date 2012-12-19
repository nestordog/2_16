package com.algoTrader.entity.trade;

import java.math.BigDecimal;

import org.apache.commons.beanutils.BeanUtils;

import com.algoTrader.entity.marketData.MarketDataEvent;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.security.SecurityFamily;
import com.algoTrader.enumeration.Side;

public class TickwiseIncrementalOrderImpl extends TickwiseIncrementalOrder {

    private static final long serialVersionUID = 6631564632498034454L;

    private BigDecimal startLimit;
    private BigDecimal endLimit;
    private BigDecimal currentLimit;

    private LimitOrder limitOrder = LimitOrder.Factory.newInstance();

    @Override
    public String getDescription() {

        //@formatter:off
        return "startOffsetTicks: " + getStartOffsetTicks() +
            " endOffsetTicks: " + getEndOffsetTicks() +
            (this.startLimit != null ? " startLimit: " + this.startLimit : "") +
            (this.endLimit != null ? " endLimit: " + this.endLimit : "") +
            (this.currentLimit != null ? " currentLimit: " + this.currentLimit : "");
        //@formatter:on
    }

    @Override
    public void validate() throws OrderValidationException {

        // do nothing
    }

    @Override
    public LimitOrder firstOrder() {

        MarketDataEvent marketDataEvent = getSecurity().getCurrentMarketDataEvent();

        if (marketDataEvent == null) {
            throw new IllegalStateException("no marketDataEvent available to initialize SlicingOrder");
        } else if (!(marketDataEvent instanceof Tick)) {
            throw new IllegalStateException("only ticks are supported, " + marketDataEvent.getClass() + " are not supported");
        }

        Tick tick = (Tick) marketDataEvent;

        SecurityFamily family = getSecurity().getSecurityFamily();

        // check spead and adjust offsetTicks if spead is too narrow
        int spreadTicks = family.getSpreadTicks(tick.getBid(), tick.getAsk());
        int adjustedStartOffsetTicks = getStartOffsetTicks();
        int adjustedEndOffsetTicks = getEndOffsetTicks();
        if (spreadTicks < 0) {

            throw new IllegalStateException("markets are crossed: bid " + tick.getBid() + " ask " + tick.getAsk());

        }

        // adjust offsetTicks if needed
        if (spreadTicks < (getStartOffsetTicks() - getEndOffsetTicks())) {

            // first reduce startOffsetTicks to min 0
            adjustedStartOffsetTicks = Math.max(spreadTicks + getEndOffsetTicks(), 0);
            if (spreadTicks < (adjustedStartOffsetTicks - getEndOffsetTicks())) {

                // if necessary also increase endOffstTicks to max 0
                adjustedEndOffsetTicks = Math.min(adjustedStartOffsetTicks - spreadTicks, 0);
            }
        }

        if (Side.BUY.equals(getSide())) {
            this.startLimit = family.adjustPrice(tick.getBid(), adjustedStartOffsetTicks);
            this.endLimit = family.adjustPrice(tick.getAsk(), adjustedEndOffsetTicks);

            if (this.startLimit.doubleValue() <= 0.0) {
                this.startLimit = family.adjustPrice(new BigDecimal(0), 1);
            }

        } else {

            this.startLimit = family.adjustPrice(tick.getAsk(), -adjustedStartOffsetTicks);
            this.endLimit = family.adjustPrice(tick.getBid(), -adjustedEndOffsetTicks);

            if (this.startLimit.doubleValue() <= 0.0) {
                this.startLimit = family.adjustPrice(new BigDecimal(0), 1);
            }

            if (this.endLimit.doubleValue() <= 0.0) {
                this.endLimit = family.adjustPrice(new BigDecimal(0), 1);
            }
        }

        this.currentLimit = this.startLimit;

        this.limitOrder.setSecurity(this.getSecurity());
        this.limitOrder.setStrategy(this.getStrategy());
        this.limitOrder.setSide(this.getSide());
        this.limitOrder.setQuantity(this.getQuantity());
        this.limitOrder.setLimit(this.currentLimit);
        this.limitOrder.setMarketChannel(this.getMarketChannel());

        // associate the childOrder with the parentOrder(this)
        this.limitOrder.setParentOrder(this);

        return this.limitOrder;
    }

    @Override
    public LimitOrder modifyOrder() {

        SecurityFamily family = getSecurity().getSecurityFamily();

        if (getSide().equals(Side.BUY)) {
            this.currentLimit = family.adjustPrice(this.currentLimit, 1);
        } else {
            this.currentLimit = family.adjustPrice(this.currentLimit, -1);
        }

        try {
            LimitOrder modifiedOrder = (LimitOrder) BeanUtils.cloneBean(this.limitOrder);
            modifiedOrder.setLimit(this.currentLimit);
            return modifiedOrder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean checkLimit() {

        SecurityFamily family = getSecurity().getSecurityFamily();

        if (getSide().equals(Side.BUY)) {
            return family.adjustPrice(this.currentLimit, 1).compareTo(this.endLimit) <= 0;
        } else {
            return family.adjustPrice(this.currentLimit, -1).compareTo(this.endLimit) >= 0;
        }
    }

    @Override
    public void done() {
        // do nothing
    }
}
