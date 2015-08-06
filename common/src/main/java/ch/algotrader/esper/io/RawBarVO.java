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

import ch.algotrader.enumeration.Duration;

/**
 * A ValueObject representing a {@link ch.algotrader.entity.marketData.Bar Bar}. Used for mapping of CSV Files
 */
public class RawBarVO implements Serializable {

    private static final long serialVersionUID = -497455836293005332L;

    /**
     * The size of this Bar (e.g. 1Min, 15Min, 1Hour, etc..)
     */
    private Duration barSize;

    /**
     * The dateTime of this RawBarVO
     */
    private Date dateTime;

    /**
     * The opening price of this Bar
     */
    private BigDecimal open;
    /**
     * The highest price during this Bar
     */
    private BigDecimal high;

    /**
     * The lowest price during this Bar
     */
    private BigDecimal low;

    /**
     * The closing price of this Bar
     */
    private BigDecimal close;

    /**
     * The current volume
     */
    private int vol;

    private String security;

    /**
     * Default Constructor
     */
    public RawBarVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param barSizeIn Duration
     * @param dateTimeIn Date
     * @param openIn BigDecimal
     * @param highIn BigDecimal
     * @param lowIn BigDecimal
     * @param closeIn BigDecimal
     * @param volIn int
     * @param securityIn String
     */
    public RawBarVO(final Duration barSizeIn, final Date dateTimeIn, final BigDecimal openIn, final BigDecimal highIn, final BigDecimal lowIn, final BigDecimal closeIn, final int volIn,
            final String securityIn) {

        this.barSize = barSizeIn;
        this.dateTime = dateTimeIn;
        this.open = openIn;
        this.high = highIn;
        this.low = lowIn;
        this.close = closeIn;
        this.vol = volIn;
        this.security = securityIn;
    }

    /**
     * Copies constructor from other RawBarVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public RawBarVO(final RawBarVO otherBean) {

        this.barSize = otherBean.getBarSize();
        this.dateTime = otherBean.getDateTime();
        this.open = otherBean.getOpen();
        this.high = otherBean.getHigh();
        this.low = otherBean.getLow();
        this.close = otherBean.getClose();
        this.vol = otherBean.getVol();
        this.security = otherBean.getSecurity();
    }

    /**
     * The size of this Bar (e.g. 1Min, 15Min, 1Hour, etc..)
     * @return barSize Duration
     */
    public Duration getBarSize() {

        return this.barSize;
    }

    /**
     * The size of this Bar (e.g. 1Min, 15Min, 1Hour, etc..)
     * @param value Duration
     */
    public void setBarSize(final Duration value) {

        this.barSize = value;
    }

    /**
     * The dateTime of this RawBarVO
     * @return dateTime Date
     */
    public Date getDateTime() {

        return this.dateTime;
    }

    /**
     * The dateTime of this RawBarVO
     * @param value Date
     */
    public void setDateTime(final Date value) {

        this.dateTime = value;
    }

    /**
     * The opening price of this Bar
     * @return open BigDecimal
     */
    public BigDecimal getOpen() {

        return this.open;
    }

    /**
     * The opening price of this Bar
     * @param value BigDecimal
     */
    public void setOpen(final BigDecimal value) {

        this.open = value;
    }

    /**
     * The highest price during this Bar
     * @return high BigDecimal
     */
    public BigDecimal getHigh() {

        return this.high;
    }

    /**
     * The highest price during this Bar
     * @param value BigDecimal
     */
    public void setHigh(final BigDecimal value) {

        this.high = value;
    }

    /**
     * The lowest price during this Bar
     * @return low BigDecimal
     */
    public BigDecimal getLow() {

        return this.low;
    }

    /**
     * The lowest price during this Bar
     * @param value BigDecimal
     */
    public void setLow(final BigDecimal value) {

        this.low = value;
    }

    /**
     * The closing price of this Bar
     * @return close BigDecimal
     */
    public BigDecimal getClose() {

        return this.close;
    }

    /**
     * The closing price of this Bar
     * @param value BigDecimal
     */
    public void setClose(final BigDecimal value) {

        this.close = value;
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

    public String getSecurity() {

        return this.security;
    }

    public void setSecurity(final String value) {

        this.security = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("RawBarVO [barSize=");
        builder.append(this.barSize);
        builder.append(", dateTime=");
        builder.append(this.dateTime);
        builder.append(", open=");
        builder.append(this.open);
        builder.append(", high=");
        builder.append(this.high);
        builder.append(", low=");
        builder.append(this.low);
        builder.append(", close=");
        builder.append(this.close);
        builder.append(", vol=");
        builder.append(this.vol);
        builder.append(", setVol=");
        builder.append(this.security);
        builder.append("]");

        return builder.toString();
    }

}
