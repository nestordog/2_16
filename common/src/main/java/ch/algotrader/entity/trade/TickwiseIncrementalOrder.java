/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.entity.trade;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

import ch.algotrader.entity.marketData.TickI;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.enumeration.Side;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class TickwiseIncrementalOrder extends IncrementalOrder {

    private static final long serialVersionUID = 6631564632498034454L;

    private BigDecimal startLimit;

    private BigDecimal endLimit;

    private BigDecimal currentLimit;

    private final LimitOrder limitOrder = LimitOrder.Factory.newInstance();

    private int startOffsetTicks;

    private int endOffsetTicks;

    /**
     * The number of ticks above/below the market the first {@link LimitOrder} will be placed.
     * <ul>
     * <li>BUY: number of ticks above bid</li>
     * <li>SELL: number of ticks below ask</li>
     * </ul>
     * @return this.startOffsetTicks int
     */
    public int getStartOffsetTicks() {
        return this.startOffsetTicks;
    }

    /**
     * The number of ticks above/below the market the first {@link LimitOrder} will be placed.
     * <ul>
     * <li>BUY: number of ticks above bid</li>
     * <li>SELL: number of ticks below ask</li>
     * </ul>
     * @param startOffsetTicksIn int
     */
    public void setStartOffsetTicks(int startOffsetTicksIn) {
        this.startOffsetTicks = startOffsetTicksIn;
    }

    /**
     * The number of ticks above/below the market the last {@link LimitOrder} will be placed.
     * <ul>
     * <li>BUY: number of ticks above ask </li>
     * <li>SELL: number of ticks below bid</li>
     * </ul>
     * @return this.endOffsetTicks int
     */
    public int getEndOffsetTicks() {
        return this.endOffsetTicks;
    }

    /**
     * The number of ticks above/below the market the last {@link LimitOrder} will be placed.
     * <ul>
     * <li>BUY: number of ticks above ask </li>
     * <li>SELL: number of ticks below bid</li>
     * </ul>
     * @param endOffsetTicksIn int
     */
    public void setEndOffsetTicks(int endOffsetTicksIn) {
        this.endOffsetTicks = endOffsetTicksIn;
    }

    @Override
    public String getExtDescription() {

        //@formatter:off
        return "startOffsetTicks=" + getStartOffsetTicks() +
            ",endOffsetTicks=" + getEndOffsetTicks() +
            (this.startLimit != null ? ",startLimit=" + this.startLimit : "") +
            (this.endLimit != null ? ",endLimit=" + this.endLimit : "") +
            (this.currentLimit != null ? ",currentLimit=" + this.currentLimit : "") +
            " " + getOrderProperties();
        //@formatter:on
    }

    @Override
    public void validate() throws OrderValidationException {
        // nothing to validate
    }

    @Override
    public List<SimpleOrder> getInitialOrders(TickI tick) {

        SecurityFamily family = getSecurity().getSecurityFamily();

        // check spread and adjust offsetTicks if spread is too narrow
        int spreadTicks = family.getSpreadTicks(null, tick.getBid(), tick.getAsk());
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
            this.startLimit = family.adjustPrice(null, tick.getBid(), adjustedStartOffsetTicks);
            this.endLimit = family.adjustPrice(null, tick.getAsk(), adjustedEndOffsetTicks);

            if (this.startLimit.doubleValue() <= 0.0) {
                this.startLimit = family.adjustPrice(null, new BigDecimal(0), 1);
            }

        } else {

            this.startLimit = family.adjustPrice(null, tick.getAsk(), -adjustedStartOffsetTicks);
            this.endLimit = family.adjustPrice(null, tick.getBid(), -adjustedEndOffsetTicks);

            if (this.startLimit.doubleValue() <= 0.0) {
                this.startLimit = family.adjustPrice(null, new BigDecimal(0), 1);
            }

            if (this.endLimit.doubleValue() <= 0.0) {
                this.endLimit = family.adjustPrice(null, new BigDecimal(0), 1);
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

        return Collections.singletonList((SimpleOrder) this.limitOrder);
    }

    @Override
    public SimpleOrder modifyOrder(TickI tick) {

        SecurityFamily family = getSecurity().getSecurityFamily();

        if (getSide().equals(Side.BUY)) {
            this.currentLimit = family.adjustPrice(null, this.currentLimit, 1);
        } else {
            this.currentLimit = family.adjustPrice(null, this.currentLimit, -1);
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
            return family.adjustPrice(null, this.currentLimit, 1).compareTo(this.endLimit) <= 0;
        } else {
            return family.adjustPrice(null, this.currentLimit, -1).compareTo(this.endLimit) >= 0;
        }
    }

    @Override
    public OrderVO convertToVO() {
        throw new UnsupportedOperationException();
    }
}
