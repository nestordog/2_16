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

import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.TransactionType;

/**
 * A ValueObject representing a {@link ch.algotrader.entity.Transaction Transaction}. Used for
 * Client display.
 */
public class TransactionVO implements Serializable {

    private static final long serialVersionUID = -602836993018186650L;

    /**
     * The Id of the Transaction.
     */
    private long id;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setId = false;

    /**
     * The dateTime of the Transaction
     */
    private Date dateTime;

    /**
     * The quantity of the Transaction.
     */
    private long quantity;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setQuantity = false;

    /**
     * The {@link TransactionType} of the Transaction
     */
    private TransactionType type;

    /**
     * The Symbol of the associated Security
     */
    private String name;

    /**
     * The name of the Strategy
     */
    private String strategy;

    /**
     * The name of the Account
     */
    private String account;

    /**
     * The {@link Currency} of the Position.
     */
    private Currency currency;

    /**
     * The price of the Transaction.
     */
    private BigDecimal price;

    /**
     * The Commission of the Transaction.
     */
    private BigDecimal totalCharges;

    /**
     * The Net Value of the Transaction.
     */
    private BigDecimal value;

    /**
     * Default Constructor
     */
    public TransactionVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor taking only required properties
     * @param idIn int The Id of the Transaction.
     * @param dateTimeIn Date The dateTime of the Transaction
     * @param quantityIn long The quantity of the Transaction.
     * @param typeIn TransactionType The {@link TransactionType} of the Transaction
     * @param strategyIn String The name of the Strategy
     * @param accountIn String The name of the Account
     * @param currencyIn Currency The {@link Currency} of the Position.
     * @param priceIn BigDecimal The price of the Transaction.
     */
    public TransactionVO(final int idIn, final Date dateTimeIn, final long quantityIn, final TransactionType typeIn, final String strategyIn, final String accountIn, final Currency currencyIn,
            final BigDecimal priceIn) {

        this.id = idIn;
        this.setId = true;
        this.dateTime = dateTimeIn;
        this.quantity = quantityIn;
        this.setQuantity = true;
        this.type = typeIn;
        this.strategy = strategyIn;
        this.account = accountIn;
        this.currency = currencyIn;
        this.price = priceIn;
    }

    /**
     * Constructor with all properties
     * @param idIn int
     * @param dateTimeIn Date
     * @param quantityIn long
     * @param typeIn TransactionType
     * @param nameIn String
     * @param strategyIn String
     * @param accountIn String
     * @param currencyIn Currency
     * @param priceIn BigDecimal
     * @param totalChargesIn BigDecimal
     * @param valueIn BigDecimal
     */
    public TransactionVO(final int idIn, final Date dateTimeIn, final long quantityIn, final TransactionType typeIn, final String nameIn, final String strategyIn, final String accountIn,
            final Currency currencyIn, final BigDecimal priceIn, final BigDecimal totalChargesIn, final BigDecimal valueIn) {

        this.id = idIn;
        this.setId = true;
        this.dateTime = dateTimeIn;
        this.quantity = quantityIn;
        this.setQuantity = true;
        this.type = typeIn;
        this.name = nameIn;
        this.strategy = strategyIn;
        this.account = accountIn;
        this.currency = currencyIn;
        this.price = priceIn;
        this.totalCharges = totalChargesIn;
        this.value = valueIn;
    }

    /**
     * Copies constructor from other TransactionVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public TransactionVO(final TransactionVO otherBean) {

        this.id = otherBean.getId();
        this.setId = true;
        this.dateTime = otherBean.getDateTime();
        this.quantity = otherBean.getQuantity();
        this.setQuantity = true;
        this.type = otherBean.getType();
        this.name = otherBean.getName();
        this.strategy = otherBean.getStrategy();
        this.account = otherBean.getAccount();
        this.currency = otherBean.getCurrency();
        this.price = otherBean.getPrice();
        this.totalCharges = otherBean.getTotalCharges();
        this.value = otherBean.getValue();
    }

    /**
     * The Id of the Transaction.
     * @return id int
     */
    public long getId() {

        return this.id;
    }

    /**
     * The Id of the Transaction.
     * @param value int
     */
    public void setId(final long value) {

        this.id = value;
        this.setId = true;
    }

    /**
     * Return true if the primitive attribute id is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetId() {

        return this.setId;
    }

    /**
     * The dateTime of the Transaction
     * @return dateTime Date
     */
    public Date getDateTime() {

        return this.dateTime;
    }

    /**
     * The dateTime of the Transaction
     * @param value Date
     */
    public void setDateTime(final Date value) {

        this.dateTime = value;
    }

    /**
     * The quantity of the Transaction.
     * @return quantity long
     */
    public long getQuantity() {

        return this.quantity;
    }

    /**
     * The quantity of the Transaction.
     * @param value long
     */
    public void setQuantity(final long value) {

        this.quantity = value;
        this.setQuantity = true;
    }

    /**
     * Return true if the primitive attribute quantity is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetQuantity() {

        return this.setQuantity;
    }

    /**
     * The {@link TransactionType} of the Transaction
     * @return type TransactionType
     */
    public TransactionType getType() {

        return this.type;
    }

    /**
     * The {@link TransactionType} of the Transaction
     * @param value TransactionType
     */
    public void setType(final TransactionType value) {

        this.type = value;
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
     * The name of the Strategy
     * @return strategy String
     */
    public String getStrategy() {

        return this.strategy;
    }

    /**
     * The name of the Strategy
     * @param value String
     */
    public void setStrategy(final String value) {

        this.strategy = value;
    }

    /**
     * The name of the Account
     * @return account String
     */
    public String getAccount() {

        return this.account;
    }

    /**
     * The name of the Account
     * @param value String
     */
    public void setAccount(final String value) {

        this.account = value;
    }

    /**
     * The {@link Currency} of the Position.
     * @return currency Currency
     */
    public Currency getCurrency() {

        return this.currency;
    }

    /**
     * The {@link Currency} of the Position.
     * @param value Currency
     */
    public void setCurrency(final Currency value) {

        this.currency = value;
    }

    /**
     * The price of the Transaction.
     * @return price BigDecimal
     */
    public BigDecimal getPrice() {

        return this.price;
    }

    /**
     * The price of the Transaction.
     * @param value BigDecimal
     */
    public void setPrice(final BigDecimal value) {

        this.price = value;
    }

    /**
     * The Commission of the Transaction.
     * @return totalCharges BigDecimal
     */
    public BigDecimal getTotalCharges() {

        return this.totalCharges;
    }

    /**
     * The Commission of the Transaction.
     * @param value BigDecimal
     */
    public void setTotalCharges(final BigDecimal value) {

        this.totalCharges = value;
    }

    /**
     * The Net Value of the Transaction.
     * @return value BigDecimal
     */
    public BigDecimal getValue() {

        return this.value;
    }

    /**
     * The Net Value of the Transaction.
     * @param value BigDecimal
     */
    public void setValue(final BigDecimal value) {

        this.value = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("TransactionVO [id=");
        builder.append(this.id);
        builder.append(", setId=");
        builder.append(this.setId);
        builder.append(", dateTime=");
        builder.append(this.dateTime);
        builder.append(", quantity=");
        builder.append(this.quantity);
        builder.append(", setQuantity=");
        builder.append(this.setQuantity);
        builder.append(", type=");
        builder.append(this.type);
        builder.append(", name=");
        builder.append(this.name);
        builder.append(", strategy=");
        builder.append(this.strategy);
        builder.append(", account=");
        builder.append(this.account);
        builder.append(", currency=");
        builder.append(this.currency);
        builder.append(", price=");
        builder.append(this.price);
        builder.append(", totalCharges=");
        builder.append(this.totalCharges);
        builder.append(", value=");
        builder.append(this.value);
        builder.append("]");

        return builder.toString();
    }

}
