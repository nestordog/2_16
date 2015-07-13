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
import java.util.Map;

import ch.algotrader.enumeration.Currency;

/**
 * A ValueObject representing a {@link ch.algotrader.entity.Position Position}. Used for Client display.
 */
public class PositionVO implements Serializable {

    private static final long serialVersionUID = 9152785353277989943L;

    /**
     * The Id of the Position.
     */
    private long id;

    /**
     * The Id of the Security.
     */
    private long securityId;

    /**
     * The current quantity of this Position.
     */
    private long quantity;

    /**
     * The Symbol of the associated Security
     */
    private String name;

    /**
     * The name of the Strategy
     */
    private String strategy;

    /**
     * The Currency of the associated Security
     */
    private Currency currency;

    /**
     * Either {@code bid} or {@code ask} depending on the direction of the position.
     */
    private BigDecimal marketPrice;

    /**
     * The value of the position based on either {@code bid} or {@code ask} depending on the
     * direction of the position.
     */
    private BigDecimal marketValue;

    /**
     * The average price of the position based on all relevant opening transactions.
     */
    private BigDecimal averagePrice;

    /**
     * The total cost of the position based on all relevant opening transactions.
     */
    private BigDecimal cost;

    /**
     * The unrealized Profit-and-Loss for this Position.
     */
    private BigDecimal unrealizedPL;

    /**
     * The realized Profit-and-Loss for this Position.
     */
    private BigDecimal realizedPL;

    /**
     * Any {@link ch.algotrader.entity.property.Property Property Properties} associated with the
     * Position
     */
    private Map properties;

    /**
     * Default Constructor
     */
    public PositionVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param idIn int
     * @param securityIdIn int
     * @param quantityIn long
     * @param nameIn String
     * @param strategyIn String
     * @param currencyIn Currency
     * @param marketPriceIn BigDecimal
     * @param marketValueIn BigDecimal
     * @param averagePriceIn BigDecimal
     * @param costIn BigDecimal
     * @param unrealizedPLIn BigDecimal
     * @param realizedPLIn BigDecimal
     * @param propertiesIn Map
     */
    public PositionVO(final int idIn, final int securityIdIn, final long quantityIn, final String nameIn, final String strategyIn, final Currency currencyIn, final BigDecimal marketPriceIn,
            final BigDecimal marketValueIn, final BigDecimal averagePriceIn, final BigDecimal costIn, final BigDecimal unrealizedPLIn, final BigDecimal realizedPLIn, final Map propertiesIn) {

        this.id = idIn;
        this.securityId = securityIdIn;
        this.quantity = quantityIn;
        this.name = nameIn;
        this.strategy = strategyIn;
        this.currency = currencyIn;
        this.marketPrice = marketPriceIn;
        this.marketValue = marketValueIn;
        this.averagePrice = averagePriceIn;
        this.cost = costIn;
        this.unrealizedPL = unrealizedPLIn;
        this.realizedPL = realizedPLIn;
        this.properties = propertiesIn;
    }

    /**
     * Copies constructor from other PositionVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public PositionVO(final PositionVO otherBean) {

        this.id = otherBean.getId();
        this.securityId = otherBean.getSecurityId();
        this.quantity = otherBean.getQuantity();
        this.name = otherBean.getName();
        this.strategy = otherBean.getStrategy();
        this.currency = otherBean.getCurrency();
        this.marketPrice = otherBean.getMarketPrice();
        this.marketValue = otherBean.getMarketValue();
        this.averagePrice = otherBean.getAveragePrice();
        this.cost = otherBean.getCost();
        this.unrealizedPL = otherBean.getUnrealizedPL();
        this.realizedPL = otherBean.getRealizedPL();
        this.properties = otherBean.getProperties();
    }

    /**
     * The Id of the Position.
     * @return id int
     */
    public long getId() {

        return this.id;
    }

    /**
     * The Id of the Position.
     * @param value int
     */
    public void setId(final long value) {

        this.id = value;
    }

    /**
     * The Id of the Security.
     * @return securityId int
     */
    public long getSecurityId() {

        return this.securityId;
    }

    /**
     * The Id of the Security.
     * @param value int
     */
    public void setSecurityId(final long value) {

        this.securityId = value;
    }

    /**
     * The current quantity of this Position.
     * @return quantity long
     */
    public long getQuantity() {

        return this.quantity;
    }

    /**
     * The current quantity of this Position.
     * @param value long
     */
    public void setQuantity(final long value) {

        this.quantity = value;
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
     * The Currency of the associated Security
     * @return currency Currency
     */
    public Currency getCurrency() {

        return this.currency;
    }

    /**
     * The Currency of the associated Security
     * @param value Currency
     */
    public void setCurrency(final Currency value) {

        this.currency = value;
    }

    /**
     * Either {@code bid} or {@code ask} depending on the direction of the position.
     * @return marketPrice BigDecimal
     */
    public BigDecimal getMarketPrice() {

        return this.marketPrice;
    }

    /**
     * Either {@code bid} or {@code ask} depending on the direction of the position.
     * @param value BigDecimal
     */
    public void setMarketPrice(final BigDecimal value) {

        this.marketPrice = value;
    }

    /**
     * The value of the position based on either {@code bid} or {@code ask} depending on the
     * direction of the position.
     * @return marketValue BigDecimal
     */
    public BigDecimal getMarketValue() {

        return this.marketValue;
    }

    /**
     * The value of the position based on either {@code bid} or {@code ask} depending on the
     * direction of the position.
     * @param value BigDecimal
     */
    public void setMarketValue(final BigDecimal value) {

        this.marketValue = value;
    }

    /**
     * The average price of the position based on all relevant opening transactions.
     * @return averagePrice BigDecimal
     */
    public BigDecimal getAveragePrice() {

        return this.averagePrice;
    }

    /**
     * The average price of the position based on all relevant opening transactions.
     * @param value BigDecimal
     */
    public void setAveragePrice(final BigDecimal value) {

        this.averagePrice = value;
    }

    /**
     * The total cost of the position based on all relevant opening transactions.
     * @return cost BigDecimal
     */
    public BigDecimal getCost() {

        return this.cost;
    }

    /**
     * The total cost of the position based on all relevant opening transactions.
     * @param value BigDecimal
     */
    public void setCost(final BigDecimal value) {

        this.cost = value;
    }

    /**
     * The unrealized Profit-and-Loss for this Position.
     * @return unrealizedPL BigDecimal
     */
    public BigDecimal getUnrealizedPL() {

        return this.unrealizedPL;
    }

    /**
     * The unrealized Profit-and-Loss for this Position.
     * @param value BigDecimal
     */
    public void setUnrealizedPL(final BigDecimal value) {

        this.unrealizedPL = value;
    }

    /**
     * The realized Profit-and-Loss for this Position.
     * @return realizedPL BigDecimal
     */
    public BigDecimal getRealizedPL() {

        return this.realizedPL;
    }

    /**
     * The realized Profit-and-Loss for this Position.
     * @param value BigDecimal
     */
    public void setRealizedPL(final BigDecimal value) {

        this.realizedPL = value;
    }

    /**
     * Any {@link ch.algotrader.entity.property.Property Property Properties} associated with the Position
     * @return properties Map
     */
    public Map getProperties() {

        return this.properties;
    }

    /**
     * Any {@link ch.algotrader.entity.property.Property Property Properties} associated with the Position
     * @param value Map
     */
    public void setProperties(final Map value) {

        this.properties = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("PositionVO [id=");
        builder.append(this.id);
        builder.append(", securityId=");
        builder.append(this.securityId);
        builder.append(", quantity=");
        builder.append(this.quantity);
        builder.append(", name=");
        builder.append(this.name);
        builder.append(", strategy=");
        builder.append(this.strategy);
        builder.append(", currency=");
        builder.append(this.currency);
        builder.append(", marketPrice=");
        builder.append(this.marketPrice);
        builder.append(", marketValue=");
        builder.append(this.marketValue);
        builder.append(", averagePrice=");
        builder.append(this.averagePrice);
        builder.append(", cost=");
        builder.append(this.cost);
        builder.append(", unrealizedPL=");
        builder.append(this.unrealizedPL);
        builder.append(", realizedPL=");
        builder.append(this.realizedPL);
        builder.append(", properties=");
        builder.append(this.properties);
        builder.append("]");

        return builder.toString();
    }

}
