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
package ch.algotrader.vo.marketData;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents the last trade provided by a data feed.
 */
public class TradeVO implements Serializable {

    private static final long serialVersionUID = 7725588965098248689L;

    /**
     * The {@code tickerId} as assigned by the external Market Data Provider
     */
    private final String tickerId;

    private final String feedType;

    private final Date lastDateTime;

    private final double last;

    private final int vol;

    /**
     * Constructor with all properties
     */
    public TradeVO(final String tickerIdIn, final String feedTypeIn, final Date lastDateTimeIn, final double lastIn, final int volIn) {

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

    public String getFeedType() {

        return this.feedType;
    }

    public Date getLastDateTime() {

        return this.lastDateTime;
    }

    public double getLast() {

        return this.last;
    }

    public int getVol() {

        return this.vol;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("tickerId=");
        builder.append(this.tickerId);
        builder.append(",feedType=");
        builder.append(this.feedType);
        builder.append(",lastDateTime=");
        builder.append(this.lastDateTime);
        builder.append(",last=");
        builder.append(this.last);
        builder.append(",vol=");
        builder.append(this.vol);

        return builder.toString();
    }

}
