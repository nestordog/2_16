/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.entity.trade;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

import ch.algotrader.entity.marketData.MarketDataEvent;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.enumeration.Side;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class TickwiseIncrementalOrderImpl extends TickwiseIncrementalOrder {

    private static final long serialVersionUID = 6631564632498034454L;

    private BigDecimal startLimit;
    private BigDecimal endLimit;
    private BigDecimal currentLimit;

    private LimitOrder limitOrder = LimitOrder.Factory.newInstance();

    @Override
    public String getExtDescription() {

        //@formatter:off
        return "startOffsetTicks=" + getStartOffsetTicks() +
            ",endOffsetTicks=" + getEndOffsetTicks() +
            (this.startLimit != null ? ",startLimit=" + this.startLimit : "") +
            (this.endLimit != null ? ",endLimit=" + this.endLimit : "") +
            (this.currentLimit != null ? ",currentLimit=" + this.currentLimit : "");
        //@formatter:on
    }

    @Override
    public void validate() throws OrderValidationException {

        MarketDataEvent marketDataEvent = getSecurity().getCurrentMarketDataEvent();

        if (marketDataEvent == null) {
            throw new OrderValidationException("no marketDataEvent available to initialize SlicingOrder");
        } else if (!(marketDataEvent instanceof Tick)) {
            throw new OrderValidationException("only ticks are supported, " + marketDataEvent.getClass() + " are not supported");
        }

        // check spead and adjust offsetTicks if spead is too narrow
        Tick tick = (Tick) marketDataEvent;
        SecurityFamily family = getSecurity().getSecurityFamily();
        int spreadTicks = family.getSpreadTicks(tick.getBid(), tick.getAsk());
        if (spreadTicks < 0) {
            throw new OrderValidationException("markets are crossed: bid " + tick.getBid() + " ask " + tick.getAsk());
        }
    }

    @Override
    public List<Order> getInitialOrders() {

        Tick tick = (Tick) getSecurity().getCurrentMarketDataEvent();

        SecurityFamily family = getSecurity().getSecurityFamily();

        // check spead and adjust offsetTicks if spead is too narrow
        int spreadTicks = family.getSpreadTicks(tick.getBid(), tick.getAsk());
        int adjustedStartOffsetTicks = getStartOffsetTicks();
        int adjustedEndOffsetTicks = getEndOffsetTicks();

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
        this.limitOrder.setAccount(this.getAccount());

        // associate the childOrder with the parentOrder(this)
        this.limitOrder.setParentOrder(this);

        return Collections.singletonList((Order) this.limitOrder);
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
}
