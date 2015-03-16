/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
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
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.entity.trade;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.enumeration.Side;
import ch.algotrader.util.collection.Pair;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class SlicingOrder extends AlgoOrder {

    private static final long serialVersionUID = -9017761050542085585L;

    private static Logger logger = LogManager.getLogger(SlicingOrder.class);

    private double minVolPct;

    private double maxVolPct;

    private long minQuantity;

    private long maxQuantity;

    private double minDuration;

    private double maxDuration;

    private double minDelay;

    private double maxDelay;

    private int currentOffsetTicks = 1;

    private List<Pair<LimitOrder, Tick>> pairs = new ArrayList<Pair<LimitOrder, Tick>>();

    /**
     * minimum part of the market volume (bidVol or askVol) that should be ordered (i.e. 50% of
     * askVol for a BUY order).
     * @return this.minVolPct double
     */
    public double getMinVolPct() {
        return this.minVolPct;
    }

    /**
     * minimum part of the market volume (bidVol or askVol) that should be ordered (i.e. 50% of
     * askVol for a BUY order).
     * @param minVolPctIn double
     */
    public void setMinVolPct(double minVolPctIn) {
        this.minVolPct = minVolPctIn;
    }

    /**
     * maximum part of the market volume (bidVol or askVol) that should be ordered (i.e. 100% of
     * askVol for a BUY order). If {@code maxVolPct} is zero, then the current market volume will
     * not be considered when sizing the order.
     * @return this.maxVolPct double
     */
    public double getMaxVolPct() {
        return this.maxVolPct;
    }

    /**
     * maximum part of the market volume (bidVol or askVol) that should be ordered (i.e. 100% of
     * askVol for a BUY order). If {@code maxVolPct} is zero, then the current market volume will
     * not be considered when sizing the order.
     * @param maxVolPctIn double
     */
    public void setMaxVolPct(double maxVolPctIn) {
        this.maxVolPct = maxVolPctIn;
    }

    /**
     * minimum quantity that should be ordered (i.e. 10 contracts). if the outcome of {@code
     * minVolPct} is lower than {@code minQuantity} then {@code minQuantity} will be enforced.
     * @return this.minQuantity long
     */
    public long getMinQuantity() {
        return this.minQuantity;
    }

    /**
     * minimum quantity that should be ordered (i.e. 10 contracts). if the outcome of {@code
     * minVolPct} is lower than {@code minQuantity} then {@code minQuantity} will be enforced.
     * @param minQuantityIn long
     */
    public void setMinQuantity(long minQuantityIn) {
        this.minQuantity = minQuantityIn;
    }

    /**
     * maximum quantity that should be ordered (i.e. 100 contracts). if the outcome of {@code
     * maxVolPct} is higher than {@code maxQuantity} then {@code maxQuantity} will be enforced. If
     * {@code maxQuantity} is zero, then no maximum quantity will be enforced on top of the market
     * volume restriction.
     * @return this.maxQuantity long
     */
    public long getMaxQuantity() {
        return this.maxQuantity;
    }

    /**
     * maximum quantity that should be ordered (i.e. 100 contracts). if the outcome of {@code
     * maxVolPct} is higher than {@code maxQuantity} then {@code maxQuantity} will be enforced. If
     * {@code maxQuantity} is zero, then no maximum quantity will be enforced on top of the market
     * volume restriction.
     * @param maxQuantityIn long
     */
    public void setMaxQuantity(long maxQuantityIn) {
        this.maxQuantity = maxQuantityIn;
    }

    /**
     * minimum duration in seconds the order will be left in the market (i.e. 1.5 seconds)
     * @return this.minDuration double
     */
    public double getMinDuration() {
        return this.minDuration;
    }

    /**
     * minimum duration in seconds the order will be left in the market (i.e. 1.5 seconds)
     * @param minDurationIn double
     */
    public void setMinDuration(double minDurationIn) {
        this.minDuration = minDurationIn;
    }

    /**
     * maximum duration in seconds the order will be left in the market (i.e. 2.5 seconds)
     * @return this.maxDuration double
     */
    public double getMaxDuration() {
        return this.maxDuration;
    }

    /**
     * maximum duration in seconds the order will be left in the market (i.e. 2.5 seconds)
     * @param maxDurationIn double
     */
    public void setMaxDuration(double maxDurationIn) {
        this.maxDuration = maxDurationIn;
    }

    /**
     * minimum delay in seconds between orders (i.e. 1.5 seconds)
     * @return this.minDelay double
     */
    public double getMinDelay() {
        return this.minDelay;
    }

    /**
     * minimum delay in seconds between orders (i.e. 1.5 seconds)
     * @param minDelayIn double
     */
    public void setMinDelay(double minDelayIn) {
        this.minDelay = minDelayIn;
    }

    /**
     * maximum delay in seconds between orders (i.e. 2.5 seconds)
     * @return this.maxDelay double
     */
    public double getMaxDelay() {
        return this.maxDelay;
    }

    /**
     * maximum delay in seconds between orders (i.e. 2.5 seconds)
     * @param maxDelayIn double
     */
    public void setMaxDelay(double maxDelayIn) {
        this.maxDelay = maxDelayIn;
    }

    @Override
    public String getExtDescription() {

        //@formatter:off
            return "vol=" + getMinVolPct() + "-" + getMaxVolPct() +
            ",qty=" + getMinQuantity() + "-" + getMaxQuantity() +
            ",duration=" + getMinDuration() + "-" + getMaxDuration() +
            ",delay=" + getMinDelay() + "-" + getMaxDelay() +
            ",currentOffsetTicks=" + this.currentOffsetTicks +
            " " + getOrderProperties();
        //@formatter:on
    }

    @Override
    public void validate() throws OrderValidationException {

        // check greater than
        if (getMinVolPct() > getMaxVolPct()) {
            throw new OrderValidationException("minVolPct cannot be greater than maxVolPct for " + getDescription());
        } else if (getMinQuantity() > getMaxQuantity()) {
            throw new OrderValidationException("minQuantity cannot be greater than maxQuantity for " + getDescription());
        } else if (getMaxQuantity() < 2 * getMinQuantity()) {
            throw new OrderValidationException("maxQuantity must be greater than 3 x minQuantity " + getDescription());
        } else if (getMinDuration() > getMaxDuration()) {
            throw new OrderValidationException("minDuration cannot be greater than maxDuration for " + getDescription());
        } else if (getMinDelay() > getMaxDelay()) {
            throw new OrderValidationException("minDelay cannot be greater than maxDelay for " + getDescription());
        }

        // check zero
        if (getMaxVolPct() == 0 && getMaxQuantity() == 0) {
            throw new OrderValidationException("either maxVolPct or maxQuantity have to be defined for " + getDescription());
        } else if (getMaxDuration() == 0) {
            throw new OrderValidationException("maxDuration cannot be zero for " + getDescription());
        } else if (getMaxDelay() == 0) {
            throw new OrderValidationException("maxDelay cannot be zero for " + getDescription());
        }
    }

    @Override
    public List<SimpleOrder> getInitialOrders(Tick tick) {

        return Collections.singletonList((SimpleOrder) nextOrder(getQuantity(), tick));
    }

    public void increaseOffsetTicks() {
        this.currentOffsetTicks = this.currentOffsetTicks + 1;
        logger.debug("increaseOffsetTicks of " + getDescription() + " to " + this.currentOffsetTicks);
    }

    public void decreaseOffsetTicks() {
        this.currentOffsetTicks = Math.max(this.currentOffsetTicks - 1, 0);
        logger.debug("decreaseOffsetTicks of " + getDescription() + " to " + this.currentOffsetTicks);
    }

    @Override
    public SimpleOrder nextOrder(long remainingQuantity, Tick tick) {

        SecurityFamily family = getSecurity().getSecurityFamily();

        // limit (at least one tick above market but do not exceed the market)
        BigDecimal limit;
        long marketVolume;
        if (Side.BUY.equals(getSide())) {

            marketVolume = tick.getVolAsk();
            limit = family.adjustPrice(null, tick.getAsk(), -this.currentOffsetTicks);

            if (limit.compareTo(tick.getBid()) <= 0.0) {
                limit = family.adjustPrice(null, tick.getBid(), 1);
                this.currentOffsetTicks = family.getSpreadTicks(null, tick.getBid(), tick.getAsk()) - 1;
            }

            if (limit.compareTo(tick.getAsk()) > 0.0) {
                limit = tick.getAsk();
                this.currentOffsetTicks = 0;
            }

        } else {

            marketVolume = tick.getVolBid();
            limit = family.adjustPrice(null, tick.getBid(), this.currentOffsetTicks);

            if (limit.compareTo(tick.getAsk()) >= 0.0) {
                limit = family.adjustPrice(null, tick.getAsk(), -1);
                this.currentOffsetTicks = family.getSpreadTicks(null, tick.getBid(), tick.getAsk()) - 1;
            }

            if (limit.compareTo(tick.getBid()) < 0.0) {
                limit = tick.getBid();
                this.currentOffsetTicks = 0;
            }
        }

        // ignore maxVolPct / maxQuantity if they are zero
        double maxVolPct = getMaxVolPct() == 0.0 ? Double.MAX_VALUE : getMaxVolPct();
        long maxQuantity = getMaxQuantity() == 0 ? Long.MAX_VALUE : getMaxQuantity();

        // evaluate the order minimum and maximum qty
        long orderMinQty = Math.max(Math.round(marketVolume * getMinVolPct()), getMinQuantity());
        long orderMaxQty = Math.min(Math.round(marketVolume * maxVolPct), maxQuantity);

        // orderMinQty cannot be greater than orderMaxQty
        if (orderMinQty > orderMaxQty) {
            orderMinQty = orderMaxQty;
        }

        // randomize the quantity between orderMinQty and orderMaxQty
        long quantity = Math.round(orderMinQty + Math.random() * (orderMaxQty - orderMinQty));

        // make sure that the remainingQty after the next slice is greater than minQuantity
        long remainingQuantityAfterSlice = remainingQuantity - quantity;
        if (getMinQuantity() > 0 && getMaxQuantity() > 0 && remainingQuantityAfterSlice > 0 && remainingQuantityAfterSlice < getMinQuantity()) {

            // if quantity is below half between minQuantity and maxQuantity
            if (quantity < (getMinQuantity() + getMaxQuantity()) / 2.0) {

                // take full remaining quantity but not more than orderMaxQty
                quantity = Math.min(remainingQuantity, orderMaxQty);
            } else {

                // make sure remaining after slice quantity is greater than minQuantity
                quantity = remainingQuantity - getMinQuantity();
            }
        }

        // qty should be at least one
        quantity = Math.max(quantity, 1);

        // qty should be maximium remainingQuantity
        quantity = Math.min(quantity, remainingQuantity);

        // create the limit order
        LimitOrder order = LimitOrder.Factory.newInstance();
        order.setSecurity(this.getSecurity());
        order.setStrategy(this.getStrategy());
        order.setSide(this.getSide());
        order.setQuantity(quantity);
        order.setLimit(limit);
        order.setAccount(this.getAccount());

        // associate the childOrder with the parentOrder(this)
        order.setParentOrder(this);

        // store the current order and tick
        this.pairs.add(new Pair<LimitOrder, Tick>(order, tick));

        //@formatter:off
        logger.info(
                "next slice for " + getDescription() +
                ",currentOffsetTicks=" + this.currentOffsetTicks +
                ",qty=" + order.getQuantity() +
                ",vol="+ (Side.BUY.equals(order.getSide()) ? tick.getVolAsk() : tick.getVolBid()) +
                ",limit=" + limit +
                ",bid=" + tick.getBid() +
                ",ask=" + tick.getAsk());
        //@formatter:on

        return order;
    }
}
