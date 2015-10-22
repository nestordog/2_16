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

public class TradingHaltVO implements Serializable  {

    private static final long serialVersionUID = 3268296036239451388L;

    private final String tickerId;
    private final String feedType;
    private final Date dateTime;

    public TradingHaltVO(final String tickerId, final String feedType, final Date dateTime) {
        this.tickerId = tickerId;
        this.feedType = feedType;
        this.dateTime = dateTime;
    }

    public String getTickerId() {
        return this.tickerId;
    }

    public String getFeedType() {
        return this.feedType;
    }

    public Date getDateTime() {
        return this.dateTime;
    }

    @Override
    public String toString() {
        return "{" +
                "tickerId='" + this.tickerId + '\'' +
                ", feedType=" + this.feedType +
                ", dateTime=" + this.dateTime +
                '}';
    }

}