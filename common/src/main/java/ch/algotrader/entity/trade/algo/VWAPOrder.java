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

    private int lookbackDays;

    private double minInterval;

    private double maxInterval;

    private LocalTime startTime;

    private LocalTime endTime;

    private double qtyRandomFactor;

    public Duration getBucketSize() {
        return this.bucketSize;
    }

    public void setBucketSize(Duration bucketSize) {
        this.bucketSize = bucketSize;
    }

    public int getLookbackDays() {
        return this.lookbackDays;
    }

    public void setLookbackDays(int lookbackDays) {
        this.lookbackDays = lookbackDays;
    }

    public double getMinInterval() {
        return this.minInterval;
    }

    public void setMinInterval(double minInterval) {
        this.minInterval = minInterval;
    }

    public double getMaxInterval() {
        return this.maxInterval;
    }

    public void setMaxInterval(double maxInterval) {
        this.maxInterval = maxInterval;
    }

    public LocalTime getStartTime() {
        return this.startTime;
    }

    public String getStart() {
        return this.startTime.format(TIME_PATTERN);
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setStart(String startTime) {
        this.startTime = LocalTime.parse(startTime);
    }
    public LocalTime getEndTime() {
        return this.endTime;
    }

    public String getEnd() {
        return this.endTime.format(TIME_PATTERN);
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public void setEnd(String endTime) {
        this.endTime = LocalTime.parse(endTime);
    }

    public double getQtyRandomFactor() {
        return this.qtyRandomFactor;
    }

    public void setQtyRandomFactor(double qtyRandomFactor) {
        this.qtyRandomFactor = qtyRandomFactor;
    }

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
        return "buckets=" + this.bucketSize + "/" + this.lookbackDays + "days" +
        ",interval=" + this.minInterval + "-" + this.maxInterval +
        (this.startTime != null ? (",timeWindow=" + getStart() + "-" + getEnd()) : "") +
        ",qtyRandomFactor=" + this.qtyRandomFactor +
        " " + getOrderProperties();
        //@formatter:on
    }

    @Override
    public void validate() throws OrderValidationException {

        // check greater than
        if (this.minInterval > this.maxInterval) {
            throw new OrderValidationException("minInterval cannot be greater than maxInterval for " + getDescription());
        }

        // check zero
        if (this.bucketSize == null) {
            throw new OrderValidationException("bucketSize cannot be null for " + getDescription());
        } else if (this.minInterval == 0) {
            throw new OrderValidationException("minInterval cannot be zero for " + getDescription());
        } else if (this.lookbackDays == 0) {
            throw new OrderValidationException("lookbackDays cannot be zero for " + getDescription());
        } else if (this.maxInterval == 0) {
            throw new OrderValidationException("maxInterval cannot be zero for " + getDescription());
        }

    }

}
