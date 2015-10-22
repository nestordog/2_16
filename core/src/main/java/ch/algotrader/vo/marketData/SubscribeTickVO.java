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

/**
 * A ValueObject used add a new {@link ch.algotrader.entity.Subscription Subscription} to the
 * TickWindow
 */
public class SubscribeTickVO implements Serializable {

    private static final long serialVersionUID = 7876356657006589699L;

    /**
     * The {@code tickerId} as assigned by the external Market Data Provider
     */
    private final String tickerId;

    /**
     * The base class of all Securities in the system
     */
    private final long securityId;

    /**
     * The market data feed that provided this  MarketDataEvent
     */
    private final String feedType;

    /**
     * Constructor with all properties
     */
    public SubscribeTickVO(final String tickerId, final long securityId, final String feedType) {

        this.tickerId = tickerId;
        this.securityId = securityId;
        this.feedType = feedType;
    }

    /**
     * Copies constructor from other SubscribeTickVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public SubscribeTickVO(final SubscribeTickVO otherBean) {

        this.tickerId = otherBean.getTickerId();
        this.securityId = otherBean.getSecurityId();
        this.feedType = otherBean.getFeedType();
    }

    /**
     * The {@code tickerId} as assigned by the external Market Data Provider
     * @return tickerId String
     */
    public String getTickerId() {

        return this.tickerId;
    }

    /**
     * The base class of all Securities in the system
     */
    public long getSecurityId() {
        return this.securityId;
    }

    /**
     * The market data feed that provided this  MarketDataEvent
     */
    public String getFeedType() {
        return this.feedType;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("tickerId=");
        builder.append(this.tickerId);
        builder.append(",securityId=");
        builder.append(this.securityId);
        builder.append(",feedType=");
        builder.append(this.feedType);

        return builder.toString();
    }

}
