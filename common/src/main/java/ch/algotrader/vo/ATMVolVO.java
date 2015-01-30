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
 * Contains the Put and Call at-the-money Volatility at a specific expiration defined by the
 * parameter {@code years}.
 */
public class ATMVolVO implements Serializable {

    private static final long serialVersionUID = 8954384794717746508L;

    /**
     * The time-to-expiration represented in {@code years}
     */
    private double years;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setYears = false;

    /**
     * the Call at-the-money Volatility
     */
    private double callVol;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setCallVol = false;

    /**
     * the Put at-the-money Volatility
     */
    private double putVol;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setPutVol = false;

    /**
     * Default Constructor
     */
    public ATMVolVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param yearsIn double
     * @param callVolIn double
     * @param putVolIn double
     */
    public ATMVolVO(final double yearsIn, final double callVolIn, final double putVolIn) {

        this.years = yearsIn;
        this.setYears = true;
        this.callVol = callVolIn;
        this.setCallVol = true;
        this.putVol = putVolIn;
        this.setPutVol = true;
    }

    /**
     * Copies constructor from other ATMVolVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public ATMVolVO(final ATMVolVO otherBean) {

        this.years = otherBean.getYears();
        this.setYears = true;
        this.callVol = otherBean.getCallVol();
        this.setCallVol = true;
        this.putVol = otherBean.getPutVol();
        this.setPutVol = true;
    }

    /**
     * The time-to-expiration represented in {@code years}
     * @return years double
     */
    public double getYears() {

        return this.years;
    }

    /**
     * The time-to-expiration represented in {@code years}
     * @param value double
     */
    public void setYears(final double value) {

        this.years = value;
        this.setYears = true;
    }

    /**
     * Return true if the primitive attribute years is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetYears() {

        return this.setYears;
    }

    /**
     * the Call at-the-money Volatility
     * @return callVol double
     */
    public double getCallVol() {

        return this.callVol;
    }

    /**
     * the Call at-the-money Volatility
     * @param value double
     */
    public void setCallVol(final double value) {

        this.callVol = value;
        this.setCallVol = true;
    }

    /**
     * Return true if the primitive attribute callVol is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetCallVol() {

        return this.setCallVol;
    }

    /**
     * the Put at-the-money Volatility
     * @return putVol double
     */
    public double getPutVol() {

        return this.putVol;
    }

    /**
     * the Put at-the-money Volatility
     * @param value double
     */
    public void setPutVol(final double value) {

        this.putVol = value;
        this.setPutVol = true;
    }

    /**
     * Return true if the primitive attribute putVol is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetPutVol() {

        return this.setPutVol;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("ATMVolVO [years=");
        builder.append(this.years);
        builder.append(", setYears=");
        builder.append(this.setYears);
        builder.append(", callVol=");
        builder.append(this.callVol);
        builder.append(", setCallVol=");
        builder.append(this.setCallVol);
        builder.append(", putVol=");
        builder.append(this.putVol);
        builder.append(", setPutVol=");
        builder.append(this.setPutVol);
        builder.append("]");

        return builder.toString();
    }

}
