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
package ch.algotrader.vo.client;

import java.math.BigDecimal;
import java.util.Date;

/**
 * A ValueObject representing a {@link ch.algotrader.entity.marketData.Tick Tick}. Used for Client display.
 */
public class TickVO extends MarketDataEventVO {

    private static final long serialVersionUID = -3053864132170718631L;

    /**
     * The last price.
     */
    private BigDecimal last;

    /**
     * The dateTime of the last trade.
     */
    private Date lastDateTime;

    /**
     * The volume on the bid side.
     */
    private int volBid;

    /**
     * The bid price.
     */
    private BigDecimal bid;

    /**
     * The ask price.
     */
    private BigDecimal ask;

    /**
     * The volume on the ask side.
     */
    private int volAsk;

    /**
     * Default Constructor
     */
    public TickVO() {

        super();
    }

    /**
     * Constructor taking only required properties
     * @param securityIdIn int The Id of the Security associated with this MarketDataEventVO
     * @param nameIn String The Symbol of the associated Security
     * @param dateTimeIn Date The dateTime of this MarketDataEventVO
     * @param volIn int The current volume
     * @param feedTypeIn FeedType The market data feed that provided this  MarketDataEventVO
     * @param lastIn BigDecimal The last price.
     * @param lastDateTimeIn Date The dateTime of the last trade.
     * @param volBidIn int The volume on the bid side.
     * @param volAskIn int The volume on the ask side.
     */
    public TickVO(final int securityIdIn, final String nameIn, final Date dateTimeIn, final int volIn, final String feedTypeIn, final BigDecimal lastIn,
            final Date lastDateTimeIn, final int volBidIn, final int volAskIn) {

        super(securityIdIn, nameIn, dateTimeIn, volIn, feedTypeIn);
        this.last = lastIn;
        this.lastDateTime = lastDateTimeIn;
        this.volBid = volBidIn;
        this.volAsk = volAskIn;
    }

    /**
     * Constructor with all properties
     * @param securityIdIn int
     * @param nameIn String
     * @param dateTimeIn Date
     * @param volIn int
     * @param currentValueIn BigDecimal
     * @param feedTypeIn FeedType
     * @param lastIn BigDecimal
     * @param lastDateTimeIn Date
     * @param volBidIn int
     * @param bidIn BigDecimal
     * @param askIn BigDecimal
     * @param volAskIn int
     */
    public TickVO(final int securityIdIn, final String nameIn, final Date dateTimeIn, final int volIn, final BigDecimal currentValueIn, final String feedTypeIn,
            final BigDecimal lastIn, final Date lastDateTimeIn, final int volBidIn, final BigDecimal bidIn, final BigDecimal askIn, final int volAskIn) {

        super(securityIdIn, nameIn, dateTimeIn, volIn, currentValueIn, feedTypeIn);
        this.last = lastIn;
        this.lastDateTime = lastDateTimeIn;
        this.volBid = volBidIn;
        this.bid = bidIn;
        this.ask = askIn;
        this.volAsk = volAskIn;
    }

    /**
     * Copies constructor from other TickVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public TickVO(final TickVO otherBean) {

        super(otherBean);
        this.last = otherBean.getLast();
        this.lastDateTime = otherBean.getLastDateTime();
        this.volBid = otherBean.getVolBid();
        this.bid = otherBean.getBid();
        this.ask = otherBean.getAsk();
        this.volAsk = otherBean.getVolAsk();
    }

    /**
     * The last price.
     * @return last BigDecimal
     */
    public BigDecimal getLast() {

        return this.last;
    }

    /**
     * The last price.
     * @param value BigDecimal
     */
    public void setLast(final BigDecimal value) {

        this.last = value;
    }

    /**
     * The dateTime of the last trade.
     * @return lastDateTime Date
     */
    public Date getLastDateTime() {

        return this.lastDateTime;
    }

    /**
     * The dateTime of the last trade.
     * @param value Date
     */
    public void setLastDateTime(final Date value) {

        this.lastDateTime = value;
    }

    /**
     * The volume on the bid side.
     * @return volBid int
     */
    public int getVolBid() {

        return this.volBid;
    }

    /**
     * The volume on the bid side.
     * @param value int
     */
    public void setVolBid(final int value) {

        this.volBid = value;
    }

    /**
     * The bid price.
     * @return bid BigDecimal
     */
    public BigDecimal getBid() {

        return this.bid;
    }

    /**
     * The bid price.
     * @param value BigDecimal
     */
    public void setBid(final BigDecimal value) {

        this.bid = value;
    }

    /**
     * The ask price.
     * @return ask BigDecimal
     */
    public BigDecimal getAsk() {

        return this.ask;
    }

    /**
     * The ask price.
     * @param value BigDecimal
     */
    public void setAsk(final BigDecimal value) {

        this.ask = value;
    }

    /**
     * The volume on the ask side.
     * @return volAsk int
     */
    public int getVolAsk() {

        return this.volAsk;
    }

    /**
     * The volume on the ask side.
     * @param value int
     */
    public void setVolAsk(final int value) {

        this.volAsk = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("TickVO [last=");
        builder.append(this.last);
        builder.append(", lastDateTime=");
        builder.append(this.lastDateTime);
        builder.append(", volBid=");
        builder.append(this.volBid);
        builder.append(", bid=");
        builder.append(this.bid);
        builder.append(", ask=");
        builder.append(this.ask);
        builder.append(", volAsk=");
        builder.append(this.volAsk);
        builder.append(", ");
        builder.append(super.toString());
        builder.append("]");

        return builder.toString();
    }

}
