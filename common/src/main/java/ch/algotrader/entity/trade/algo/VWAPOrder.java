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

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

import ch.algotrader.entity.trade.OrderValidationException;
import ch.algotrader.enumeration.Duration;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class VWAPOrder extends AlgoOrder {

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

    private static final long serialVersionUID = -9017761050542085585L;

    private Duration bucketSize;

    private int lookbackPeriod;

    private double minInterval;

    private double maxInterval;

    private Date startTime;

    private Date endTime;

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
    public Date getStartTime() {
        return this.startTime;
    }

    /**
     * algo start time (optional)
     */
    public String getStart() {
        return TIME_FORMAT.format(this.startTime);
    }

    /**
     * algo start time (optional)
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * algo start time (optional)
     */
    public void setStart(String startTime) {
        this.startTime = Date.from(LocalTime.parse(startTime).atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * algo end time (optional)
     */
    public Date getEndTime() {
        return this.endTime;
    }

    /**
     * algo end time (optional)
     */
    public String getEnd() {
        return TIME_FORMAT.format(this.endTime);
    }

    /**
     * algo end time (optional)
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * algo end time (optional)
     */
    public void setEnd(String endTime) {
        this.endTime = Date.from(LocalTime.parse(endTime).atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant());
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
            return (int) (this.endTime.getTime() - this.startTime.getTime()) / 60000;
        }
    }

    @Override
    public String getExtDescription() {

        //@formatter:off
        return "buckets=" + this.bucketSize + "/" + this.lookbackPeriod + "days" +
        ",interval=" + this.minInterval + "-" + this.maxInterval +
        (this.startTime != null && this.endTime != null? (",time=" + getStart() + "-" + getEnd()) : "") +
        ",rnd=" + this.qtyRandomFactor +
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
