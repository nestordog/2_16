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
 * A ValueObject representing a {@link ch.algotrader.entity.marketData.Bar Bar}.
 */
public class BarVO extends MarketDataEventVO {

    private static final long serialVersionUID = 7781274232307224743L;

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
     * Default Constructor
     */
    public BarVO() {

        super();
    }

    /**
     * Constructor taking only required properties
     * @param securityIdIn int The Id of the Security associated with this MarketDataEventVO
     * @param nameIn String The Symbol of the associated Security
     * @param dateTimeIn Date The dateTime of this MarketDataEventVO
     * @param volIn int The current volume
     * @param feedTypeIn String The market data feed that provided this  MarketDataEventVO
     * @param openIn BigDecimal The opening price of this Bar
     * @param highIn BigDecimal The highest price during this Bar
     * @param lowIn BigDecimal The lowest price during this Bar
     * @param closeIn BigDecimal The closing price of this Bar
     */
    public BarVO(final int securityIdIn, final String nameIn, final Date dateTimeIn, final int volIn, final String feedTypeIn, final BigDecimal openIn,
            final BigDecimal highIn, final BigDecimal lowIn, final BigDecimal closeIn) {

        super(securityIdIn, nameIn, dateTimeIn, volIn, feedTypeIn);
        this.open = openIn;
        this.high = highIn;
        this.low = lowIn;
        this.close = closeIn;
    }

    /**
     * Constructor with all properties
     * @param securityIdIn int
     * @param nameIn String
     * @param dateTimeIn Date
     * @param volIn int
     * @param currentValueIn BigDecimal
     * @param feedTypeIn String
     * @param openIn BigDecimal
     * @param highIn BigDecimal
     * @param lowIn BigDecimal
     * @param closeIn BigDecimal
     */
    public BarVO(final int securityIdIn, final String nameIn, final Date dateTimeIn, final int volIn, final BigDecimal currentValueIn, final String feedTypeIn, final BigDecimal openIn,
            final BigDecimal highIn, final BigDecimal lowIn, final BigDecimal closeIn) {

        super(securityIdIn, nameIn, dateTimeIn, volIn, currentValueIn, feedTypeIn);

        this.open = openIn;
        this.high = highIn;
        this.low = lowIn;
        this.close = closeIn;
    }

    /**
     * Copies constructor from other BarVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public BarVO(final BarVO otherBean) {

        super(otherBean);

        this.open = otherBean.getOpen();
        this.high = otherBean.getHigh();
        this.low = otherBean.getLow();
        this.close = otherBean.getClose();
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

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("BarVO [open=");
        builder.append(this.open);
        builder.append(", high=");
        builder.append(this.high);
        builder.append(", low=");
        builder.append(this.low);
        builder.append(", close=");
        builder.append(this.close);
        builder.append(", ");
        builder.append(super.toString());
        builder.append("]");

        return builder.toString();
    }

}
