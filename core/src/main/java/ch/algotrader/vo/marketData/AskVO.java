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

/**
 * Represents the ask price provided by a data feed.
 * <p>
 * The ask price represents the minimum price that a seller or sellers are willing to receive for the security
 */
public class AskVO implements Serializable {

    private static final long serialVersionUID = -3195482126593164671L;

    /**
     * The {@code tickerId} as assigned by the external Market Data Provider
     */
    private final String tickerId;

    private final String feedType;

    private final Date dateTime;

    private final double ask;

    private final int volAsk;

    /**
     * Constructor with all properties
     */
    public AskVO(final String tickerIdIn, final String feedTypeIn, final Date dateTimeIn, final double askIn, final int volAskIn) {

        this.tickerId = tickerIdIn;
        this.feedType = feedTypeIn;
        this.dateTime = dateTimeIn;
        this.ask = askIn;
        this.volAsk = volAskIn;
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
        this.volAsk = otherBean.getVolAsk();
    }

    /**
     * The {@code tickerId} as assigned by the external Market Data Provider
     * Get the tickerId Attribute
     * @return tickerId String
     */
    public String getTickerId() {

        return this.tickerId;
    }

    public String getFeedType() {

        return this.feedType;
    }

    public Date getDateTime() {

        return this.dateTime;
    }

    public double getAsk() {

        return this.ask;
    }

    public int getVolAsk() {

        return this.volAsk;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("tickerId=");
        builder.append(tickerId);
        builder.append(",feedType=");
        builder.append(feedType);
        builder.append(",dateTime=");
        builder.append(dateTime);
        builder.append(",ask=");
        builder.append(ask);
        builder.append(",volAsk=");
        builder.append(volAsk);

        return builder.toString();
    }

}
