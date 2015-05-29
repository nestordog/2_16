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
package ch.algotrader.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * A ValueObject used to import data from <a href="http://www.iVolatility.com">iVolatility.com</a>
 */
public class IVolVO implements Serializable {

    private static final long serialVersionUID = -1232522448366174418L;

    /**
     * They Symbol of the Security
     */
    private String symbol;

    private String exchange;

    private Date date;

    /**
     * The adjusted closing price of the underlying
     */
    private BigDecimal adjustedStockClosePrice;

    /**
     * The Symbol of the Option
     */
    private String optionSymbol;

    /**
     * The expiration Date
     */
    private Date expiration;

    /**
     * The strike price
     */
    private BigDecimal strike;

    /**
     * C or O
     */
    private String type;

    /**
     * The ask price
     */
    private BigDecimal ask;

    /**
     * The bid price
     */
    private BigDecimal bid;

    private int volume;

    /**
     * The open Intrest
     */
    private int openIntrest;

    /**
     * The un-adjusted price of the underlying
     */
    private BigDecimal unadjustedStockPrice;

    /**
     * Default Constructor
     */
    public IVolVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param symbolIn String
     * @param exchangeIn String
     * @param dateIn Date
     * @param adjustedStockClosePriceIn BigDecimal
     * @param optionSymbolIn String
     * @param expirationIn Date
     * @param strikeIn BigDecimal
     * @param typeIn String
     * @param askIn BigDecimal
     * @param bidIn BigDecimal
     * @param volumeIn int
     * @param openIntrestIn int
     * @param unadjustedStockPriceIn BigDecimal
     */
    public IVolVO(final String symbolIn, final String exchangeIn, final Date dateIn, final BigDecimal adjustedStockClosePriceIn, final String optionSymbolIn, final Date expirationIn,
            final BigDecimal strikeIn, final String typeIn, final BigDecimal askIn, final BigDecimal bidIn, final int volumeIn, final int openIntrestIn, final BigDecimal unadjustedStockPriceIn) {

        this.symbol = symbolIn;
        this.exchange = exchangeIn;
        this.date = dateIn;
        this.adjustedStockClosePrice = adjustedStockClosePriceIn;
        this.optionSymbol = optionSymbolIn;
        this.expiration = expirationIn;
        this.strike = strikeIn;
        this.type = typeIn;
        this.ask = askIn;
        this.bid = bidIn;
        this.volume = volumeIn;
        this.openIntrest = openIntrestIn;
        this.unadjustedStockPrice = unadjustedStockPriceIn;
    }

    /**
     * Copies constructor from other IVolVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public IVolVO(final IVolVO otherBean) {

        this.symbol = otherBean.getSymbol();
        this.exchange = otherBean.getExchange();
        this.date = otherBean.getDate();
        this.adjustedStockClosePrice = otherBean.getAdjustedStockClosePrice();
        this.optionSymbol = otherBean.getOptionSymbol();
        this.expiration = otherBean.getExpiration();
        this.strike = otherBean.getStrike();
        this.type = otherBean.getType();
        this.ask = otherBean.getAsk();
        this.bid = otherBean.getBid();
        this.volume = otherBean.getVolume();
        this.openIntrest = otherBean.getOpenIntrest();
        this.unadjustedStockPrice = otherBean.getUnadjustedStockPrice();
    }

    public String getSymbol() {

        return this.symbol;
    }

    public void setSymbol(final String value) {

        this.symbol = value;
    }

    public String getExchange() {

        return this.exchange;
    }

    public void setExchange(final String value) {

        this.exchange = value;
    }

    public Date getDate() {

        return this.date;
    }

    public void setDate(final Date value) {

        this.date = value;
    }

    /**
     * Returns the adjusted closing price of the underlying
     * @return adjustedStockClosePrice BigDecimal
     */
    public BigDecimal getAdjustedStockClosePrice() {

        return this.adjustedStockClosePrice;
    }

    /**
     * Returns the adjusted closing price of the underlying
     * @param value BigDecimal
     */
    public void setAdjustedStockClosePrice(final BigDecimal value) {

        this.adjustedStockClosePrice = value;
    }

    public String getOptionSymbol() {

        return this.optionSymbol;
    }

    public void setOptionSymbol(final String value) {

        this.optionSymbol = value;
    }

    public Date getExpiration() {

        return this.expiration;
    }

    public void setExpiration(final Date value) {

        this.expiration = value;
    }

    public BigDecimal getStrike() {

        return this.strike;
    }

    public void setStrike(final BigDecimal value) {

        this.strike = value;
    }

    public String getType() {

        return this.type;
    }

    public void setType(final String value) {

        this.type = value;
    }

    public BigDecimal getAsk() {

        return this.ask;
    }

    public void setAsk(final BigDecimal value) {

        this.ask = value;
    }

    public BigDecimal getBid() {

        return this.bid;
    }

    public void setBid(final BigDecimal value) {

        this.bid = value;
    }

    public int getVolume() {

        return this.volume;
    }

    public void setVolume(final int value) {

        this.volume = value;
    }

    public int getOpenIntrest() {

        return this.openIntrest;
    }

    public void setOpenIntrest(final int value) {

        this.openIntrest = value;
    }

    /**
     * Returns the un-adjusted price of the underlying
     * @return unadjustedStockPrice BigDecimal
     */
    public BigDecimal getUnadjustedStockPrice() {

        return this.unadjustedStockPrice;
    }

    /**
     * Returns the un-adjusted price of the underlying
     * @param value BigDecimal
     */
    public void setUnadjustedStockPrice(final BigDecimal value) {

        this.unadjustedStockPrice = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("IVolVO [symbol=");
        builder.append(symbol);
        builder.append(", exchange=");
        builder.append(exchange);
        builder.append(", date=");
        builder.append(date);
        builder.append(", adjustedStockClosePrice=");
        builder.append(adjustedStockClosePrice);
        builder.append(", optionSymbol=");
        builder.append(optionSymbol);
        builder.append(", expiration=");
        builder.append(expiration);
        builder.append(", strike=");
        builder.append(strike);
        builder.append(", type=");
        builder.append(type);
        builder.append(", ask=");
        builder.append(ask);
        builder.append(", bid=");
        builder.append(bid);
        builder.append(", volume=");
        builder.append(volume);
        builder.append(", openIntrest=");
        builder.append(openIntrest);
        builder.append(", unadjustedStockPrice=");
        builder.append(unadjustedStockPrice);
        builder.append("]");

        return builder.toString();
    }

}
