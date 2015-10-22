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
package ch.algotrader.vo.client;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * A ValueObject representing a {@link ch.algotrader.entity.marketData.MarketDataEvent
 * MarketDataEvent}. Used for Client display.
 */
public class MarketDataEventVO implements Serializable {

    private static final long serialVersionUID = -4662489084911960645L;

    /**
     * The Id of the Security associated with this MarketDataEventVO
     */
    private long securityId;

    /**
     * The Symbol of the associated Security
     */
    private String name;

    /**
     * The dateTime of this MarketDataEventVO
     */
    private Date dateTime;

    /**
     * The current volume
     */
    private int vol;

    /**
     * The most recent price of this market data event.
     */
    private BigDecimal currentValue;

    /**
     * The market data feed that provided this  MarketDataEventVO
     */
    private String feedType;

    /**
     * Default Constructor
     */
    public MarketDataEventVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor taking only required properties
     * @param securityIdIn int The Id of the Security associated with this MarketDataEventVO
     * @param nameIn String The Symbol of the associated Security
     * @param dateTimeIn Date The dateTime of this MarketDataEventVO
     * @param volIn int The current volume
     * @param feedTypeIn FeedType The market data feed that provided this  MarketDataEventVO
     */
    public MarketDataEventVO(final int securityIdIn, final String nameIn, final Date dateTimeIn, final int volIn, final String feedTypeIn) {

        this.securityId = securityIdIn;
        this.name = nameIn;
        this.dateTime = dateTimeIn;
        this.vol = volIn;
        this.feedType = feedTypeIn;
    }

    /**
     * Constructor with all properties
     * @param securityIdIn int
     * @param nameIn String
     * @param dateTimeIn Date
     * @param volIn int
     * @param currentValueIn BigDecimal
     * @param feedTypeIn String
     */
    public MarketDataEventVO(final int securityIdIn, final String nameIn, final Date dateTimeIn, final int volIn, final BigDecimal currentValueIn, final String feedTypeIn) {

        this.securityId = securityIdIn;
        this.name = nameIn;
        this.dateTime = dateTimeIn;
        this.vol = volIn;
        this.currentValue = currentValueIn;
        this.feedType = feedTypeIn;
    }

    /**
     * Copies constructor from other MarketDataEventVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public MarketDataEventVO(final MarketDataEventVO otherBean) {

        this.securityId = otherBean.getSecurityId();
        this.name = otherBean.getName();
        this.dateTime = otherBean.getDateTime();
        this.vol = otherBean.getVol();
        this.currentValue = otherBean.getCurrentValue();
        this.feedType = otherBean.getFeedType();
    }

    /**
     * The Id of the Security associated with this MarketDataEventVO
     * @return securityId int
     */
    public long getSecurityId() {

        return this.securityId;
    }

    /**
     * The Id of the Security associated with this MarketDataEventVO
     * @param value int
     */
    public void setSecurityId(final long value) {

        this.securityId = value;
    }

    /**
     * The Symbol of the associated Security
     * @return name String
     */
    public String getName() {

        return this.name;
    }

    /**
     * The Symbol of the associated Security
     * @param value String
     */
    public void setName(final String value) {

        this.name = value;
    }

    /**
     * The dateTime of this MarketDataEventVO
     * @return dateTime Date
     */
    public Date getDateTime() {

        return this.dateTime;
    }

    /**
     * The dateTime of this MarketDataEventVO
     * @param value Date
     */
    public void setDateTime(final Date value) {

        this.dateTime = value;
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
     * The most recent price of this market data event.
     * @return currentValue BigDecimal
     */
    public BigDecimal getCurrentValue() {

        return this.currentValue;
    }

    /**
     * The most recent price of this market data event.
     * @param value BigDecimal
     */
    public void setCurrentValue(final BigDecimal value) {

        this.currentValue = value;
    }

    /**
     * The market data feed that provided this  MarketDataEventVO
     * @return feedType FeedType
     */
    public String getFeedType() {

        return this.feedType;
    }

    /**
     * The market data feed that provided this  MarketDataEventVO
     * @param value String
     */
    public void setFeedType(final String value) {

        this.feedType = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("MarketDataEventVO [securityId=");
        builder.append(this.securityId);
        builder.append(", name=");
        builder.append(this.name);
        builder.append(", dateTime=");
        builder.append(this.dateTime);
        builder.append(", vol=");
        builder.append(this.vol);
        builder.append(", currentValue=");
        builder.append(this.currentValue);
        builder.append(", feedType=");
        builder.append(this.feedType);
        builder.append("]");

        return builder.toString();
    }

}
