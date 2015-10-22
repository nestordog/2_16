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

import java.io.Serializable;
import java.math.BigDecimal;

import ch.algotrader.enumeration.Currency;

/**
 * Represents an amount in a particular {@link Currency}
 */
public class CurrencyAmountVO implements Serializable {

    private static final long serialVersionUID = -287820727140021514L;

    /**
     * The {@link Currency}
     */
    private Currency currency;

    /**
     * The Money amount
     */
    private BigDecimal amount;

    /**
     * Default Constructor
     */
    public CurrencyAmountVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param currencyIn Currency
     * @param amountIn BigDecimal
     */
    public CurrencyAmountVO(final Currency currencyIn, final BigDecimal amountIn) {

        this.currency = currencyIn;
        this.amount = amountIn;
    }

    /**
     * Copies constructor from other CurrencyAmountVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public CurrencyAmountVO(final CurrencyAmountVO otherBean) {

        this.currency = otherBean.getCurrency();
        this.amount = otherBean.getAmount();
    }

    public Currency getCurrency() {

        return this.currency;
    }

    public void setCurrency(final Currency value) {

        this.currency = value;
    }

    public BigDecimal getAmount() {

        return this.amount;
    }

    public void setAmount(final BigDecimal value) {

        this.amount = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("CurrencyAmountVO [currency=");
        builder.append(this.currency);
        builder.append(", amount=");
        builder.append(this.amount);
        builder.append("]");

        return builder.toString();
    }

}
