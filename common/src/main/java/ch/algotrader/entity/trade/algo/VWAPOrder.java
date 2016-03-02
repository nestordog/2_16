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

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import ch.algotrader.entity.trade.OrderValidationException;
import ch.algotrader.enumeration.Duration;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class VWAPOrder extends AlgoOrder {

    private static final DateTimeFormatter TIME_PATTERN = DateTimeFormatter.ofPattern("HH:mm");

    private static final long serialVersionUID = -9017761050542085585L;

    private Duration bucketSize;

    private int lookbackPeriod;

    private double minInterval;

    private double maxInterval;

    private LocalTime startTime;

    private LocalTime endTime;

    private double qtyRandomFactor;

    /**
     * historical data bucket size (in minutes)
     */
    public Duration getBucketSize() {
        return this.bucketSize;
    }

    /**
     * historical data bucket size (in minutes)
     */
    public void setBucketSize(Duration bucketSize) {
        this.bucketSize = bucketSize;
    }

    /**
     * historical data lookback period (in days)
     */
    public int getLookbackPeriod() {
        return this.lookbackPeriod;
    }

    /**
     * historical data lookback period (in days)
     */
    public void setLookbackPeriod(int lookbackDays) {
        this.lookbackPeriod = lookbackDays;
    }

    /**
     * minimum time interval between child orders (in seconds)
     */
    public double getMinInterval() {
        return this.minInterval;
    }

    /**
     * minimum time interval between child orders (in seconds)
     */
    public void setMinInterval(double minInterval) {
        this.minInterval = minInterval;
    }

    /**
     * maximum time interval between child orders (in seconds)
     */
    public double getMaxInterval() {
        return this.maxInterval;
    }

    /**
     * maximum time interval between child orders (in seconds)
     */
    public void setMaxInterval(double maxInterval) {
        this.maxInterval = maxInterval;
    }

    /**
     * algo start time (optional)
     */
    public LocalTime getStartTime() {
        return this.startTime;
    }

    /**
     * algo start time (optional)
     */
    public String getStart() {
        return this.startTime.format(TIME_PATTERN);
    }

    /**
     * algo start time (optional)
     */
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    /**
     * algo start time (optional)
     */
    public void setStart(String startTime) {
        this.startTime = LocalTime.parse(startTime);
    }

    /**
     * algo end time (optional)
     */
    public LocalTime getEndTime() {
        return this.endTime;
    }

    /**
     * algo end time (optional)
     */
    public String getEnd() {
        return this.endTime.format(TIME_PATTERN);
    }

    /**
     * algo end time (optional)
     */
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    /**
     * algo end time (optional)
     */
    public void setEnd(String endTime) {
        this.endTime = LocalTime.parse(endTime);
    }

    /**
     * quantity randomization factor
     * e.g. 30% will randomize an order for 10 contracts by +/- 30% (i.e. between 7 and 13 contracts)
     */
    public double getQtyRandomFactor() {
        return this.qtyRandomFactor;
    }

    /**
     * quantity randomization factor
     * e.g. 30% will randomize an order for 10 contracts by +/- 30% (i.e. between 7 and 13 contracts)
     */
    public void setQtyRandomFactor(double qtyRandomFactor) {
        this.qtyRandomFactor = qtyRandomFactor;
    }

    /**
     * duration of the algo (in minutes)
     */
    public int getDuration() {
        if (this.startTime == null || this.endTime == null) {
            return 0;
        } else {
            return (int) ChronoUnit.MINUTES.between(this.startTime, this.endTime);
        }
    }

    @Override
    public String getExtDescription() {

        //@formatter:off
        return "buckets=" + this.bucketSize + "/" + this.lookbackPeriod + "days" +
        ",interval=" + this.minInterval + "-" + this.maxInterval +
        (this.startTime != null && this.endTime != null? (",timeWindow=" + getStart() + "-" + getEnd()) : "") +
        ",qtyRandomFactor=" + this.qtyRandomFactor +
        " " + getOrderProperties();
        //@formatter:on
    }

    @Override
    public void validate() throws OrderValidationException {

        // check zero
        if (this.bucketSize == null) {
            throw new OrderValidationException("bucketSize cannot be null for " + getDescription());
        } else if (this.lookbackPeriod == 0) {
            throw new OrderValidationException("lookbackPeriod cannot be zero for " + getDescription());
        } else if (this.minInterval == 0) {
            throw new OrderValidationException("minInterval cannot be zero for " + getDescription());
        } else if (this.maxInterval == 0) {
            throw new OrderValidationException("maxInterval cannot be zero for " + getDescription());
        }

        // check conditions
        if (this.minInterval > this.maxInterval) {
            throw new OrderValidationException("minInterval cannot be greater than maxInterval for " + getDescription());
        }

        if (this.maxInterval * 1000 > this.bucketSize.getValue()) {
            throw new OrderValidationException("maxInterval cannot be greater than bucketSize for " + getDescription());
        }

        if (this.startTime != null && this.endTime != null && this.endTime.compareTo(this.startTime) <= 0) {
            throw new OrderValidationException("endTime needs to be greater than startTime for " + getDescription());
        }

        if (this.qtyRandomFactor < 0 || this.qtyRandomFactor >= 1.0) {
            throw new OrderValidationException("qtyRandomFactor needs to be >=0.0 and <1.0 " + getDescription());
        }

    }

}
