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

/**
 * A ValueObject used add a new {@link ch.algotrader.entity.security.Component Component} to the
 * CombinationWindow
 */
public class InsertComponentEventVO implements Serializable {

    private static final long serialVersionUID = 4539095334154421342L;

    /**
     * The Id of the Component
     */
    private long componentId;

    /**
     * The quantity of the Component
     */
    private long quantity;

    /**
     * The Security Id of the Component.
     */
    private long securityId;

    /**
     * The Security Id of the Combination
     */
    private long combinationId;

    /**
     * The number of components the corresponding Combination consists of.
     */
    private int componentCount;

    /**
     * Default Constructor
     */
    public InsertComponentEventVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param componentIdIn int
     * @param quantityIn long
     * @param securityIdIn int
     * @param combinationIdIn int
     * @param componentCountIn int
     */
    public InsertComponentEventVO(final int componentIdIn, final long quantityIn, final int securityIdIn, final int combinationIdIn, final int componentCountIn) {

        this.componentId = componentIdIn;
        this.quantity = quantityIn;
        this.securityId = securityIdIn;
        this.combinationId = combinationIdIn;
        this.componentCount = componentCountIn;
    }

    /**
     * Copies constructor from other InsertComponentEventVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public InsertComponentEventVO(final InsertComponentEventVO otherBean) {

        this.componentId = otherBean.getComponentId();
        this.quantity = otherBean.getQuantity();
        this.securityId = otherBean.getSecurityId();
        this.combinationId = otherBean.getCombinationId();
        this.componentCount = otherBean.getComponentCount();
    }

    public long getComponentId() {

        return this.componentId;
    }

    public void setComponentId(final long value) {

        this.componentId = value;
    }

    public long getQuantity() {

        return this.quantity;
    }

    public void setQuantity(final long value) {

        this.quantity = value;
    }

    public long getSecurityId() {

        return this.securityId;
    }

    public void setSecurityId(final long value) {

        this.securityId = value;
    }

    public long getCombinationId() {

        return this.combinationId;
    }

    public void setCombinationId(final long value) {

        this.combinationId = value;
    }

    /**
     * Returns the number of components the corresponding Combination consists of.
     * @return componentCount int
     */
    public int getComponentCount() {

        return this.componentCount;
    }

    public void setComponentCount(final int value) {

        this.componentCount = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("InsertComponentEventVO [componentId=");
        builder.append(componentId);
        builder.append(", quantity=");
        builder.append(quantity);
        builder.append(", securityId=");
        builder.append(securityId);
        builder.append(", combinationId=");
        builder.append(combinationId);
        builder.append(", componentCount=");
        builder.append(componentCount);
        builder.append("]");

        return builder.toString();
    }

}
