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
public class BidVO implements Serializable {

    private static final long serialVersionUID = 6674906339222452763L;

    /**
     * The {@code tickerId} as assigned by the external Market Data Provider
     */
    private String tickerId;

    private FeedType feedType;

    private Date dateTime;

    private double bid;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setBid = false;

    private int volBid;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setVolBid = false;

    /**
     * Default Constructor
     */
    public BidVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param tickerIdIn String
     * @param feedTypeIn FeedType
     * @param dateTimeIn Date
     * @param bidIn double
     * @param volBidIn int
     */
    public BidVO(final String tickerIdIn, final FeedType feedTypeIn, final Date dateTimeIn, final double bidIn, final int volBidIn) {

        this.tickerId = tickerIdIn;
        this.feedType = feedTypeIn;
        this.dateTime = dateTimeIn;
        this.bid = bidIn;
        this.setBid = true;
        this.volBid = volBidIn;
        this.setVolBid = true;
    }

    /**
     * Copies constructor from other BidVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public BidVO(final BidVO otherBean) {

        this.tickerId = otherBean.getTickerId();
        this.feedType = otherBean.getFeedType();
        this.dateTime = otherBean.getDateTime();
        this.bid = otherBean.getBid();
        this.setBid = true;
        this.volBid = otherBean.getVolBid();
        this.setVolBid = true;
    }

    /**
     * The {@code tickerId} as assigned by the external Market Data Provider
     * Get the tickerId Attribute
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

    public Date getDateTime() {

        return this.dateTime;
    }

    public void setDateTime(final Date value) {

        this.dateTime = value;
    }

    public double getBid() {

        return this.bid;
    }

    public void setBid(final double value) {

        this.bid = value;
        this.setBid = true;
    }

    /**
     * Return true if the primitive attribute bid is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetBid() {

        return this.setBid;
    }

    public int getVolBid() {

        return this.volBid;
    }

    public void setVolBid(final int value) {

        this.volBid = value;
        this.setVolBid = true;
    }

    /**
     * Return true if the primitive attribute volBid is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetVolBid() {

        return this.setVolBid;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("BidVO [tickerId=");
        builder.append(this.tickerId);
        builder.append(", feedType=");
        builder.append(this.feedType);
        builder.append(", dateTime=");
        builder.append(this.dateTime);
        builder.append(", bid=");
        builder.append(this.bid);
        builder.append(", setBid=");
        builder.append(this.setBid);
        builder.append(", volBid=");
        builder.append(this.volBid);
        builder.append(", setVolBid=");
        builder.append(this.setVolBid);
        builder.append("]");

        return builder.toString();
    }

}
