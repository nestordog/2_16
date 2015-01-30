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
    private int componentId;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setComponentId = false;

    /**
     * The quantity of the Component
     */
    private long quantity;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setQuantity = false;

    /**
     * The Security Id of the Component.
     */
    private int securityId;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setSecurityId = false;

    /**
     * The Security Id of the Combination
     */
    private int combinationId;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setCombinationId = false;

    /**
     * The number of components the corresponding Combination consists of.
     */
    private int componentCount;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setComponentCount = false;

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
        this.setComponentId = true;
        this.quantity = quantityIn;
        this.setQuantity = true;
        this.securityId = securityIdIn;
        this.setSecurityId = true;
        this.combinationId = combinationIdIn;
        this.setCombinationId = true;
        this.componentCount = componentCountIn;
        this.setComponentCount = true;
    }

    /**
     * Copies constructor from other InsertComponentEventVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public InsertComponentEventVO(final InsertComponentEventVO otherBean) {

        this.componentId = otherBean.getComponentId();
        this.setComponentId = true;
        this.quantity = otherBean.getQuantity();
        this.setQuantity = true;
        this.securityId = otherBean.getSecurityId();
        this.setSecurityId = true;
        this.combinationId = otherBean.getCombinationId();
        this.setCombinationId = true;
        this.componentCount = otherBean.getComponentCount();
        this.setComponentCount = true;
    }

    public int getComponentId() {

        return this.componentId;
    }

    public void setComponentId(final int value) {

        this.componentId = value;
        this.setComponentId = true;
    }

    /**
     * Return true if the primitive attribute componentId is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetComponentId() {

        return this.setComponentId;
    }

    public long getQuantity() {

        return this.quantity;
    }

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

    public int getSecurityId() {

        return this.securityId;
    }

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

    public int getCombinationId() {

        return this.combinationId;
    }

    public void setCombinationId(final int value) {

        this.combinationId = value;
        this.setCombinationId = true;
    }

    /**
     * Return true if the primitive attribute combinationId is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetCombinationId() {

        return this.setCombinationId;
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
        this.setComponentCount = true;
    }

    /**
     * Return true if the primitive attribute componentCount is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetComponentCount() {

        return this.setComponentCount;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("InsertComponentEventVO [componentId=");
        builder.append(componentId);
        builder.append(", setComponentId=");
        builder.append(setComponentId);
        builder.append(", quantity=");
        builder.append(quantity);
        builder.append(", setQuantity=");
        builder.append(setQuantity);
        builder.append(", securityId=");
        builder.append(securityId);
        builder.append(", setSecurityId=");
        builder.append(setSecurityId);
        builder.append(", combinationId=");
        builder.append(combinationId);
        builder.append(", setCombinationId=");
        builder.append(setCombinationId);
        builder.append(", componentCount=");
        builder.append(componentCount);
        builder.append(", setComponentCount=");
        builder.append(setComponentCount);
        builder.append("]");

        return builder.toString();
    }

}
