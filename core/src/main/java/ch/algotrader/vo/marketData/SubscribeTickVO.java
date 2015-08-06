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

import ch.algotrader.entity.marketData.Tick;

/**
 * A ValueObject used add a new {@link ch.algotrader.entity.Subscription Subscription} to the
 * TickWindow
 */
public class SubscribeTickVO implements Serializable {

    private static final long serialVersionUID = 7876356657006589699L;

    /**
     * And "empty" Tick with an associated Security to be populated to the TickWindow.
     */
    private Tick tick;

    /**
     * The {@code tickerId} as assigned by the external Market Data Provider
     */
    private String tickerId;

    /**
     * Default Constructor
     */
    public SubscribeTickVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param tickIn Tick
     * @param tickerIdIn String
     */
    public SubscribeTickVO(final Tick tickIn, final String tickerIdIn) {

        this.tick = tickIn;
        this.tickerId = tickerIdIn;
    }

    /**
     * Copies constructor from other SubscribeTickVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public SubscribeTickVO(final SubscribeTickVO otherBean) {

        this.tick = otherBean.getTick();
        this.tickerId = otherBean.getTickerId();
    }

    /**
     * And "empty" Tick with an associated Security to be populated to the TickWindow.
     * @return tick Tick
     */
    public Tick getTick() {

        return this.tick;
    }

    /**
     * And "empty" Tick with an associated Security to be populated to the TickWindow.
     * @param value Tick
     */
    public void setTick(final Tick value) {

        this.tick = value;
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

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("SubscribeTickVO [tick=");
        builder.append(this.tick);
        builder.append(", tickerId=");
        builder.append(this.tickerId);
        builder.append("]");

        return builder.toString();
    }

}
