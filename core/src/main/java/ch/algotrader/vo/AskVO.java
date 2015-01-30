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
public class AskVO implements Serializable {

    private static final long serialVersionUID = -3195482126593164671L;

    /**
     * The {@code tickerId} as assigned by the external Market Data Provider
     */
    private String tickerId;

    private FeedType feedType;

    private Date dateTime;

    private double ask;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setAsk = false;

    private int volAsk;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setVolAsk = false;

    /**
     * Default Constructor
     */
    public AskVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param tickerIdIn String
     * @param feedTypeIn FeedType
     * @param dateTimeIn Date
     * @param askIn double
     * @param volAskIn int
     */
    public AskVO(final String tickerIdIn, final FeedType feedTypeIn, final Date dateTimeIn, final double askIn, final int volAskIn) {

        this.tickerId = tickerIdIn;
        this.feedType = feedTypeIn;
        this.dateTime = dateTimeIn;
        this.ask = askIn;
        this.setAsk = true;
        this.volAsk = volAskIn;
        this.setVolAsk = true;
    }

    /**
     * Copies constructor from other AskVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public AskVO(final AskVO otherBean) {

        this.tickerId = otherBean.getTickerId();
        this.feedType = otherBean.getFeedType();
        this.dateTime = otherBean.getDateTime();
        this.ask = otherBean.getAsk();
        this.setAsk = true;
        this.volAsk = otherBean.getVolAsk();
        this.setVolAsk = true;
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

    public double getAsk() {

        return this.ask;
    }

    public void setAsk(final double value) {

        this.ask = value;
        this.setAsk = true;
    }

    /**
     * Return true if the primitive attribute ask is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetAsk() {

        return this.setAsk;
    }

    public int getVolAsk() {

        return this.volAsk;
    }

    public void setVolAsk(final int value) {

        this.volAsk = value;
        this.setVolAsk = true;
    }

    /**
     * Return true if the primitive attribute volAsk is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetVolAsk() {

        return this.setVolAsk;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("AskVO [tickerId=");
        builder.append(tickerId);
        builder.append(", feedType=");
        builder.append(feedType);
        builder.append(", dateTime=");
        builder.append(dateTime);
        builder.append(", ask=");
        builder.append(ask);
        builder.append(", setAsk=");
        builder.append(setAsk);
        builder.append(", volAsk=");
        builder.append(volAsk);
        builder.append(", setVolAsk=");
        builder.append(setVolAsk);
        builder.append("]");

        return builder.toString();
    }

}
