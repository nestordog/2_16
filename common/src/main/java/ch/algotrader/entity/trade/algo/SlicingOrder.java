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
package ch.algotrader.entity.trade.algo;

import ch.algotrader.entity.trade.OrderValidationException;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class SlicingOrder extends AlgoOrder {

    private static final long serialVersionUID = -9017761050542085585L;

    private double minVolPct;

    private double maxVolPct;

    private long minQuantity;

    private long maxQuantity;

    private double minDuration;

    private double maxDuration;

    private double minDelay;

    private double maxDelay;

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
            return "vol=" + this.minVolPct + "-" + this.maxVolPct +
            ",qty=" + this.minQuantity + "-" + this.maxQuantity +
            ",duration=" + this.minDuration + "-" + this.maxDuration +
            ",delay=" + this.minDelay + "-" + this.maxDelay +
            "," + getOrderProperties();
        //@formatter:on
    }

    @Override
    public void validate() throws OrderValidationException {

        // check greater than
        if (this.minVolPct > this.maxVolPct) {
            throw new OrderValidationException("minVolPct cannot be greater than maxVolPct for " + getDescription());
        } else if (this.minQuantity > this.maxQuantity) {
            throw new OrderValidationException("minQuantity cannot be greater than maxQuantity for " + getDescription());
        } else if (this.maxQuantity < 2 * this.minQuantity) {
            throw new OrderValidationException("maxQuantity must be greater than 3 x minQuantity " + getDescription());
        } else if (this.minDuration > this.maxDuration) {
            throw new OrderValidationException("minDuration cannot be greater than maxDuration for " + getDescription());
        } else if (this.minDelay > this.maxDelay) {
            throw new OrderValidationException("minDelay cannot be greater than maxDelay for " + getDescription());
        }

        // check zero
        if (this.maxVolPct == 0 && this.maxQuantity == 0) {
            throw new OrderValidationException("either maxVolPct or maxQuantity have to be defined for " + getDescription());
        } else if (this.maxDuration == 0) {
            throw new OrderValidationException("maxDuration cannot be zero for " + getDescription());
        } else if (this.maxDelay == 0) {
            throw new OrderValidationException("maxDelay cannot be zero for " + getDescription());
        }
    }

}
