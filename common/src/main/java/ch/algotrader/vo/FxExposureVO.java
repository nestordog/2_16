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
package ch.algotrader.vo;

import ch.algotrader.enumeration.Currency;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * A ValueObject representing the different Balances of a particular {@link Currency}. Used for
 * Client display.
 */
public class FxExposureVO implements Serializable {

    private static final long serialVersionUID = -3662517250923293233L;

    /**
     * The {@link Currency} of this BalanceVO
     */
    private Currency currency;

    /**
     * Total cash
     */
    private BigDecimal amount;

    /**
     * Current market value of all positions in Base {@link Currency}.
     */
    private BigDecimal amountBase;

    /**
     * The exchange Rate between the {@link Currency} of this BalanceVO and the Base Currency.
     */
    private double exchangeRate;

    /**
     *  Default Constructor with no properties
     */
    public FxExposureVO() {
        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param currencyIn Currency
     * @param amountIn BigDecimal
     * @param amountBaseIn BigDecimal
     * @param exchangeRateIn double
     */
    public FxExposureVO(final Currency currencyIn, final BigDecimal amountIn, final BigDecimal amountBaseIn, final double exchangeRateIn) {

        this.currency = currencyIn;
        this.amount = amountIn;
        this.amountBase = amountBaseIn;
        this.exchangeRate = exchangeRateIn;
    }

    /**
     * Copies constructor from other FxExposureVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public FxExposureVO(final FxExposureVO otherBean) {

        this.currency = otherBean.getCurrency();
        this.amount = otherBean.getAmount();
        this.amountBase = otherBean.getAmountBase();
        this.exchangeRate = otherBean.getExchangeRate();
    }

    /**
     * The {@link Currency} of this BalanceVO
     * Get the currency Attribute
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

    /**
     * Total cash
     * Get the amount Attribute
     * @return amount BigDecimal
     */
    public BigDecimal getAmount() {
        return this.amount;
    }

    /**
     * Total cash
     * @param value BigDecimal
     */
    public void setAmount(final BigDecimal value) {
        this.amount = value;
    }

    /**
     * Current market value of all positions in Base {@link Currency}.
     * Get the amountBase Attribute
     * @return amountBase BigDecimal
     */
    public BigDecimal getAmountBase() {
        return this.amountBase;
    }

    /**
     * Current market value of all positions in Base {@link Currency}.
     * @param value BigDecimal
     */
    public void setAmountBase(final BigDecimal value) {
        this.amountBase = value;
    }

    /**
     * The exchange Rate between the {@link Currency} of this BalanceVO and the Base Currency.
     * Get the exchangeRate Attribute
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

    /**
     * @return String representation of object
     * @see Object#toString()
     */
    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("TradePerformanceVO [currency=");
        builder.append(this.currency);
        builder.append(", amount=");
        builder.append(this.amount);
        builder.append(", amountBase=");
        builder.append(this.amountBase);
        builder.append(", exchangeRate=");
        builder.append(this.exchangeRate);
        builder.append("]");

        return builder.toString();
    }

}
