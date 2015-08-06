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
package ch.algotrader.esper.io;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * A ValueObject representing a {@link ch.algotrader.entity.marketData.Tick Tick}. Used for mapping
 * of CSV Files
 */
public class RawTickVO implements Serializable {

    private static final long serialVersionUID = -1199148491741538415L;

    /**
     * The dateTime of this RawTickVO
     */
    private Date dateTime;

    /**
     * The last price.
     */
    private BigDecimal last;

    /**
     * The dateTime of the last trade.
     */
    private Date lastDateTime;

    /**
     * The current volume
     */
    private int vol;

    /**
     * The volume on the bid side.
     */
    private int volBid;

    /**
     * The volume on the ask side.
     */
    private int volAsk;

    /**
     * The bid price.
     */
    private BigDecimal bid;

    /**
     * The ask price.
     */
    private BigDecimal ask;

    private String security;

    /**
     * Default Constructor
     */
    public RawTickVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor taking only required properties
     * @param dateTimeIn Date The dateTime of this RawTickVO
     * @param volIn int The current volume
     * @param volBidIn int The volume on the bid side.
     * @param volAskIn int The volume on the ask side.
     * @param securityIn String
     */
    public RawTickVO(final Date dateTimeIn, final int volIn, final int volBidIn, final int volAskIn, final String securityIn) {

        this.dateTime = dateTimeIn;
        this.vol = volIn;
        this.volBid = volBidIn;
        this.volAsk = volAskIn;
        this.security = securityIn;
    }

    /**
     * Constructor with all properties
     * @param dateTimeIn Date
     * @param lastIn BigDecimal
     * @param lastDateTimeIn Date
     * @param volIn int
     * @param volBidIn int
     * @param volAskIn int
     * @param bidIn BigDecimal
     * @param askIn BigDecimal
     * @param securityIn String
     */
    public RawTickVO(final Date dateTimeIn, final BigDecimal lastIn, final Date lastDateTimeIn, final int volIn, final int volBidIn, final int volAskIn, final BigDecimal bidIn,
            final BigDecimal askIn, final String securityIn) {

        this.dateTime = dateTimeIn;
        this.last = lastIn;
        this.lastDateTime = lastDateTimeIn;
        this.vol = volIn;
        this.volBid = volBidIn;
        this.volAsk = volAskIn;
        this.bid = bidIn;
        this.ask = askIn;
        this.security = securityIn;
    }

    /**
     * Copies constructor from other RawTickVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public RawTickVO(final RawTickVO otherBean) {

        this.dateTime = otherBean.getDateTime();
        this.last = otherBean.getLast();
        this.lastDateTime = otherBean.getLastDateTime();
        this.vol = otherBean.getVol();
        this.volBid = otherBean.getVolBid();
        this.volAsk = otherBean.getVolAsk();
        this.bid = otherBean.getBid();
        this.ask = otherBean.getAsk();
        this.security = otherBean.getSecurity();
    }

    /**
     * The dateTime of this RawTickVO
     * @return dateTime Date
     */
    public Date getDateTime() {

        return this.dateTime;
    }

    /**
     * The dateTime of this RawTickVO
     * @param value Date
     */
    public void setDateTime(final Date value) {

        this.dateTime = value;
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
     * The current volume
     * @return vol int
     */
    public int getVol() {

        return this.vol;
    }

    /**
     * The current volume
     * @param value int
     */
    public void setVol(final int value) {

        this.vol = value;
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

    public String getSecurity() {

        return this.security;
    }

    public void setSecurity(final String value) {

        this.security = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("RawTickVO [dateTime=");
        builder.append(this.dateTime);
        builder.append(", last=");
        builder.append(this.last);
        builder.append(", lastDateTime=");
        builder.append(this.lastDateTime);
        builder.append(", vol=");
        builder.append(this.vol);
        builder.append(", volBid=");
        builder.append(this.volBid);
        builder.append(", volAsk=");
        builder.append(this.volAsk);
        builder.append(", bid=");
        builder.append(this.bid);
        builder.append(", ask=");
        builder.append(this.ask);
        builder.append(", security=");
        builder.append(this.security);
        builder.append("]");

        return builder.toString();
    }

}
