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
package ch.algotrader.vo;

import java.io.Serializable;
import java.util.Date;

import ch.algotrader.enumeration.FeedType;

/**
 * A ValueObject used add a new {@link ch.algotrader.entity.Subscription Subscription} to the
 * TickWindow
 */
public class TradeVO implements Serializable {

    private static final long serialVersionUID = 7725588965098248689L;

    /**
     * The {@code tickerId} as assigned by the external Market Data Provider
     */
    private String tickerId;

    private FeedType feedType;

    private Date lastDateTime;

    private double last;

    private int vol;

    /**
     * Default Constructor
     */
    public TradeVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param tickerIdIn String
     * @param feedTypeIn FeedType
     * @param lastDateTimeIn Date
     * @param lastIn double
     * @param volIn int
     */
    public TradeVO(final String tickerIdIn, final FeedType feedTypeIn, final Date lastDateTimeIn, final double lastIn, final int volIn) {

        this.tickerId = tickerIdIn;
        this.feedType = feedTypeIn;
        this.lastDateTime = lastDateTimeIn;
        this.last = lastIn;
        this.vol = volIn;
    }

    /**
     * Copies constructor from other TradeVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public TradeVO(final TradeVO otherBean) {

        this.tickerId = otherBean.getTickerId();
        this.feedType = otherBean.getFeedType();
        this.lastDateTime = otherBean.getLastDateTime();
        this.last = otherBean.getLast();
        this.vol = otherBean.getVol();
    }

    /**
     * The {@code tickerId} as assigned by the external Market Data Provider
     * @return tickerId String
     */
    public String getTickerId() {

        return this.tickerId;
    }

    /**
     * The {@code tickerId} as assigned by the external Market Data Provider
     * @param value String
     */
    public void setTickerId(final String value) {

        this.tickerId = value;
    }

    public FeedType getFeedType() {

        return this.feedType;
    }

    public void setFeedType(final FeedType value) {

        this.feedType = value;
    }

    public Date getLastDateTime() {

        return this.lastDateTime;
    }

    public void setLastDateTime(final Date value) {

        this.lastDateTime = value;
    }

    public double getLast() {

        return this.last;
    }

    public void setLast(final double value) {

        this.last = value;
    }

    public int getVol() {

        return this.vol;
    }

    public void setVol(final int value) {

        this.vol = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("TradeVO [tickerId=");
        builder.append(this.tickerId);
        builder.append(", feedType=");
        builder.append(this.feedType);
        builder.append(", lastDateTime=");
        builder.append(this.lastDateTime);
        builder.append(", last=");
        builder.append(this.last);
        builder.append(", vol=");
        builder.append(this.vol);
        builder.append("]");

        return builder.toString();
    }

}
