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

import ch.algotrader.enumeration.Direction;

/**
 * A ValueObject representing a mutation of a {@link ch.algotrader.entity.Position Position}
 */
public class PositionMutationVO implements Serializable {

    private static final long serialVersionUID = 8785576161302312640L;

    /**
     * The Id of the Position.
     */
    private int id;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setId = false;

    /**
     * The Id of the Security.
     */
    private int securityId;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setSecurityId = false;

    /**
     * The name of the Strategy
     */
    private String strategy;

    /**
     * The current quantity of the Position
     */
    private long quantity;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setQuantity = false;

    /**
     * The direction of the Position
     */
    private Direction direction;

    /**
     * Default Constructor
     */
    public PositionMutationVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param idIn int
     * @param securityIdIn int
     * @param strategyIn String
     * @param quantityIn long
     * @param directionIn Direction
     */
    public PositionMutationVO(final int idIn, final int securityIdIn, final String strategyIn, final long quantityIn, final Direction directionIn) {

        this.id = idIn;
        this.setId = true;
        this.securityId = securityIdIn;
        this.setSecurityId = true;
        this.strategy = strategyIn;
        this.quantity = quantityIn;
        this.setQuantity = true;
        this.direction = directionIn;
    }

    /**
     * Copies constructor from other PositionMutationVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public PositionMutationVO(final PositionMutationVO otherBean) {

        this.id = otherBean.getId();
        this.setId = true;
        this.securityId = otherBean.getSecurityId();
        this.setSecurityId = true;
        this.strategy = otherBean.getStrategy();
        this.quantity = otherBean.getQuantity();
        this.setQuantity = true;
        this.direction = otherBean.getDirection();
    }

    /**
     * The Id of the Position.
     * @return id int
     */
    public int getId() {

        return this.id;
    }

    /**
     * The Id of the Position.
     * @param value int
     */
    public void setId(final int value) {

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
     * The Id of the Security.
     * @return securityId int
     */
    public int getSecurityId() {

        return this.securityId;
    }

    /**
     * The Id of the Security.
     * @param value int
     */
    public void setSecurityId(final int value) {

        this.securityId = value;
        this.setSecurityId = true;
    }

    /**
     * Return true if the primitive attribute securityId is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetSecurityId() {

        return this.setSecurityId;
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
     * The current quantity of the Position
     * @return quantity long
     */
    public long getQuantity() {

        return this.quantity;
    }

    /**
     * The current quantity of the Position
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
     * The direction of the Position
     * @return direction Direction
     */
    public Direction getDirection() {

        return this.direction;
    }

    /**
     * The direction of the Position
     * @param value Direction
     */
    public void setDirection(final Direction value) {

        this.direction = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("PositionMutationVO [id=");
        builder.append(this.id);
        builder.append(", setId=");
        builder.append(this.setId);
        builder.append(", securityId=");
        builder.append(this.securityId);
        builder.append(", setSecurityId=");
        builder.append(this.setSecurityId);
        builder.append(", strategy=");
        builder.append(this.strategy);
        builder.append(", quantity=");
        builder.append(this.quantity);
        builder.append(", setQuantity=");
        builder.append(this.setQuantity);
        builder.append(", direction=");
        builder.append(this.direction);
        builder.append("]");

        return builder.toString();
    }

}
