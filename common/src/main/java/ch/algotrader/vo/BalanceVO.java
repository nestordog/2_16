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
     * The exchange Rate between the {@link Currency} of this BalanceVO and the Base Currency.
     */
    private double exchangeRate;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setExchangeRate = false;

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
     * @param cashBaseIn BigDecimal
     * @param securitiesBaseIn BigDecimal
     * @param netLiqValueBaseIn BigDecimal
     * @param exchangeRateIn double
     */
    public BalanceVO(final Currency currencyIn, final BigDecimal cashIn, final BigDecimal securitiesIn, final BigDecimal netLiqValueIn, final BigDecimal cashBaseIn, final BigDecimal securitiesBaseIn,
            final BigDecimal netLiqValueBaseIn, final double exchangeRateIn) {

        this.currency = currencyIn;
        this.cash = cashIn;
        this.securities = securitiesIn;
        this.netLiqValue = netLiqValueIn;
        this.cashBase = cashBaseIn;
        this.securitiesBase = securitiesBaseIn;
        this.netLiqValueBase = netLiqValueBaseIn;
        this.exchangeRate = exchangeRateIn;
        this.setExchangeRate = true;
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
        this.cashBase = otherBean.getCashBase();
        this.securitiesBase = otherBean.getSecuritiesBase();
        this.netLiqValueBase = otherBean.getNetLiqValueBase();
        this.exchangeRate = otherBean.getExchangeRate();
        this.setExchangeRate = true;
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
        this.setExchangeRate = true;
    }

    /**
     * Return true if the primitive attribute exchangeRate is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetExchangeRate() {

        return this.setExchangeRate;
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
        builder.append(", cashBase=");
        builder.append(this.cashBase);
        builder.append(", securitiesBase=");
        builder.append(this.securitiesBase);
        builder.append(", netLiqValueBase=");
        builder.append(this.netLiqValueBase);
        builder.append(", exchangeRate=");
        builder.append(this.exchangeRate);
        builder.append(", setExchangeRate=");
        builder.append(this.setExchangeRate);
        builder.append("]");

        return builder.toString();
    }

}
