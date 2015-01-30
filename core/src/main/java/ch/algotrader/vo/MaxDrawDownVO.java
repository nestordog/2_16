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
 * A ValueObject representing the Maximum Draw Down during a Simulation Run.
 */
public class MaxDrawDownVO implements Serializable {

    private static final long serialVersionUID = -449935965133287989L;

    /**
     * The amount of the Draw Down in percent.
     */
    private double amount;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setAmount = false;

    /**
     * The length of the Draw Down in milliseconds.
     */
    private long period;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setPeriod = false;

    /**
     * Default Constructor
     */
    public MaxDrawDownVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param amountIn double
     * @param periodIn long
     */
    public MaxDrawDownVO(final double amountIn, final long periodIn) {

        this.amount = amountIn;
        this.setAmount = true;
        this.period = periodIn;
        this.setPeriod = true;
    }

    /**
     * Copies constructor from other MaxDrawDownVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public MaxDrawDownVO(final MaxDrawDownVO otherBean) {

        this.amount = otherBean.getAmount();
        this.setAmount = true;
        this.period = otherBean.getPeriod();
        this.setPeriod = true;
    }

    public double getAmount() {

        return this.amount;
    }

    public void setAmount(final double value) {

        this.amount = value;
        this.setAmount = true;
    }

    /**
     * Return true if the primitive attribute amount is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetAmount() {

        return this.setAmount;
    }

    public long getPeriod() {

        return this.period;
    }

    public void setPeriod(final long value) {

        this.period = value;
        this.setPeriod = true;
    }

    /**
     * Return true if the primitive attribute period is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetPeriod() {

        return this.setPeriod;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("MaxDrawDownVO [amount=");
        builder.append(amount);
        builder.append(", setAmount=");
        builder.append(setAmount);
        builder.append(", period=");
        builder.append(period);
        builder.append(", setPeriod=");
        builder.append(setPeriod);
        builder.append("]");

        return builder.toString();
    }

}
