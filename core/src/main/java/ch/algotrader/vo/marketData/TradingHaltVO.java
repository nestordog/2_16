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

public class TradingHaltVO implements Serializable  {

    private final String tickerId;
    private final FeedType feedType;
    private final Date dateTime;

    public TradingHaltVO(final String tickerId, final FeedType feedType, final Date dateTime) {
        this.tickerId = tickerId;
        this.feedType = feedType;
        this.dateTime = dateTime;
    }

    public String getTickerId() {
        return this.tickerId;
    }

    public FeedType getFeedType() {
        return this.feedType;
    }

    public Date getDateTime() {
        return this.dateTime;
    }

    @Override
    public String toString() {
        return "{" +
                "tickerId='" + tickerId + '\'' +
                ", feedType=" + feedType +
                ", dateTime=" + dateTime +
                '}';
    }

}