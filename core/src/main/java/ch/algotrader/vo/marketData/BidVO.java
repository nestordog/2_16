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
package ch.algotrader.vo.marketData;

import java.io.Serializable;
import java.util.Date;

import ch.algotrader.enumeration.FeedType;

/**
 * Represents the bid price provided by a data feed.
 * <p>
 * The bid price represents the maximum price that a buyer or buyers are willing to pay for a security.
 */
public class BidVO implements Serializable {

    private static final long serialVersionUID = 6674906339222452763L;

    /**
     * The {@code tickerId} as assigned by the external Market Data Provider
     */
    private final String tickerId;

    private final FeedType feedType;

    private final Date dateTime;

    private final double bid;

    private final int volBid;

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
        this.volBid = volBidIn;
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
        this.volBid = otherBean.getVolBid();
    }

    /**
     * The {@code tickerId} as assigned by the external Market Data Provider
     * Get the tickerId Attribute
     * @return tickerId String
     */
    public String getTickerId() {

        return this.tickerId;
    }

    public FeedType getFeedType() {

        return this.feedType;
    }

    public Date getDateTime() {

        return this.dateTime;
    }

    public double getBid() {

        return this.bid;
    }

    public int getVolBid() {

        return this.volBid;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("tickerId=");
        builder.append(this.tickerId);
        builder.append(",feedType=");
        builder.append(this.feedType);
        builder.append(",dateTime=");
        builder.append(this.dateTime);
        builder.append(",bid=");
        builder.append(this.bid);
        builder.append(",volBid=");
        builder.append(this.volBid);

        return builder.toString();
    }

}
