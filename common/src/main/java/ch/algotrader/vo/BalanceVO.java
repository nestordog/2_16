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

import ch.algotrader.enumeration.Currency;

/**
 * A ValueObject representing the different Balances of a particular {@link Currency}. Used for
 * Client display.
 */
public class BalanceVO implements Serializable {

    private static final long serialVersionUID = -4789319288921590623L;

    /**
     * The {@link Currency} of this BalanceVO
     */
    private Currency currency;

    /**
     * Total cash
     */
    private BigDecimal cash;

    /**
     * Current market value of all positions.
     */
    private BigDecimal securities;

    /**
     * Current market value of all Assets.
     */
    private BigDecimal netLiqValue;

    /**
     * Current market value of all positions.
     */
    private BigDecimal unrealizedPL;

    /**
     * Total cash in Base {@link Currency}.
     */
    private BigDecimal cashBase;

    /**
     * Current market value of all positions in Base {@link Currency}.
     */
    private BigDecimal securitiesBase;

    /**
     * Current market value of all Assets in Base {@link Currency}.
     */
    private BigDecimal netLiqValueBase;

    /**
     * Current market value of all positions in Base {@link Currency}.
     */
    private BigDecimal unrealizedPLBase;

    /**
     * The exchange Rate between the {@link Currency} of this BalanceVO and the Base Currency.
     */
    private double exchangeRate;

    /**
     * Default Constructor
     */
    public BalanceVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param currencyIn Currency
     * @param cashIn BigDecimal
     * @param securitiesIn BigDecimal
     * @param netLiqValueIn BigDecimal
     * @param unrealizedPLIn BigDecimal
     * @param cashBaseIn BigDecimal
     * @param securitiesBaseIn BigDecimal
     * @param netLiqValueBaseIn BigDecimal
     * @param unrealizedPLBaseIn BigDecimal
     * @param exchangeRateIn double
     */
    public BalanceVO(final Currency currencyIn, final BigDecimal cashIn, final BigDecimal securitiesIn, final BigDecimal netLiqValueIn, final BigDecimal unrealizedPLIn, final BigDecimal cashBaseIn,
            final BigDecimal securitiesBaseIn, final BigDecimal netLiqValueBaseIn, final BigDecimal unrealizedPLBaseIn, final double exchangeRateIn) {

        this.currency = currencyIn;
        this.cash = cashIn;
        this.securities = securitiesIn;
        this.netLiqValue = netLiqValueIn;
        this.unrealizedPL = unrealizedPLIn;
        this.cashBase = cashBaseIn;
        this.securitiesBase = securitiesBaseIn;
        this.netLiqValueBase = netLiqValueBaseIn;
        this.unrealizedPLBase = unrealizedPLBaseIn;
        this.exchangeRate = exchangeRateIn;
    }

    /**
     * Copies constructor from other BalanceVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public BalanceVO(final BalanceVO otherBean) {

        this.currency = otherBean.getCurrency();
        this.cash = otherBean.getCash();
        this.securities = otherBean.getSecurities();
        this.netLiqValue = otherBean.getNetLiqValue();
        this.unrealizedPL = otherBean.getUnrealizedPL();
        this.cashBase = otherBean.getCashBase();
        this.securitiesBase = otherBean.getSecuritiesBase();
        this.netLiqValueBase = otherBean.getNetLiqValueBase();
        this.unrealizedPLBase = otherBean.getUnrealizedPLBase();
        this.exchangeRate = otherBean.getExchangeRate();
    }

    /**
     * The {@link Currency} of this BalanceVO
     * @return currency Currency
     */
    public Currency getCurrency() {

        return this.currency;
    }

    /**
     * The {@link Currency} of this BalanceVO
     * @param value Currency
     */
    public void setCurrency(final Currency value) {

        this.currency = value;
    }

    public BigDecimal getCash() {

        return this.cash;
    }

    public void setCash(final BigDecimal value) {

        this.cash = value;
    }

    /**
     * Current market value of all positions.
     * @return securities BigDecimal
     */
    public BigDecimal getSecurities() {

        return this.securities;
    }

    /**
     * Current market value of all positions.
     * @param value BigDecimal
     */
    public void setSecurities(final BigDecimal value) {

        this.securities = value;
    }

    /**
     * Current market value of all Assets.
     * @return netLiqValue BigDecimal
     */
    public BigDecimal getNetLiqValue() {

        return this.netLiqValue;
    }

    /**
     * Current market value of all Assets.
     * @param value BigDecimal
     */
    public void setNetLiqValue(final BigDecimal value) {

        this.netLiqValue = value;
    }

    /**
     * Current market value of all positions.
     * Get the unrealizedPL Attribute
     * @return unrealizedPL BigDecimal
     */
    public BigDecimal getUnrealizedPL() {

        return this.unrealizedPL;
    }

    /**
     * Current market value of all positions.
     * @param value BigDecimal
     */
    public void setUnrealizedPL(final BigDecimal value) {

        this.unrealizedPL = value;
    }

    /**
     * Total cash in Base {@link Currency}.
     * @return cashBase BigDecimal
     */
    public BigDecimal getCashBase() {

        return this.cashBase;
    }

    /**
     * Total cash in Base {@link Currency}.
     * @param value BigDecimal
     */
    public void setCashBase(final BigDecimal value) {

        this.cashBase = value;
    }

    /**
     * Current market value of all positions in Base {@link Currency}.
     * @return securitiesBase BigDecimal
     */
    public BigDecimal getSecuritiesBase() {

        return this.securitiesBase;
    }

    /**
     * Current market value of all positions in Base {@link Currency}.
     * @param value BigDecimal
     */
    public void setSecuritiesBase(final BigDecimal value) {

        this.securitiesBase = value;
    }

    /**
     * Current market value of all Assets in Base {@link Currency}.
     * @return netLiqValueBase BigDecimal
     */
    public BigDecimal getNetLiqValueBase() {

        return this.netLiqValueBase;
    }

    /**
     * Current market value of all Assets in Base {@link Currency}.
     * @param value BigDecimal
     */
    public void setNetLiqValueBase(final BigDecimal value) {

        this.netLiqValueBase = value;
    }


        /**
     * Current market value of all positions in Base {@link Currency}.
     * Get the unrealizedPLBase Attribute
     * @return unrealizedPLBase BigDecimal
     */
    public BigDecimal getUnrealizedPLBase() {

        return this.unrealizedPLBase;
    }

    /**
     * Current market value of all positions in Base {@link Currency}.
     * @param value BigDecimal
     */
    public void setUnrealizedPLBase(final BigDecimal value) {

        this.unrealizedPLBase = value;
    }

    /**
     * The exchange Rate between the {@link Currency} of this BalanceVO and the Base Currency.
     * @return exchangeRate double
     */
    public double getExchangeRate() {

        return this.exchangeRate;
    }

    /**
     * The exchange Rate between the {@link Currency} of this BalanceVO and the Base Currency.
     * @param value double
     */
    public void setExchangeRate(final double value) {

        this.exchangeRate = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("BalanceVO [currency=");
        builder.append(this.currency);
        builder.append(", cash=");
        builder.append(this.cash);
        builder.append(", securities=");
        builder.append(this.securities);
        builder.append(", netLiqValue=");
        builder.append(this.netLiqValue);
        builder.append(", unrealizedPL=");
        builder.append(this.unrealizedPL);
        builder.append(", cashBase=");
        builder.append(this.cashBase);
        builder.append(", securitiesBase=");
        builder.append(this.securitiesBase);
        builder.append(", netLiqValueBase=");
        builder.append(this.netLiqValueBase);
        builder.append(", unrealizedPLBase=");
        builder.append(this.unrealizedPLBase);
        builder.append(", exchangeRate=");
        builder.append(this.exchangeRate);
        builder.append("]");

        return builder.toString();
    }

}
