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
 * Contains the SABR definition of a Volatility Smile at a specific expiration defined by the
 * parameter {@code years}
 */
public class SABRSmileVO implements Serializable {

    private static final long serialVersionUID = -8638571149531828705L;

    /**
     * The time-to-expiration represented in {@code years}
     */
    private double years;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setYears = false;

    /**
     * The SABR alpha
     */
    private double alpha;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setAlpha = false;

    /**
     * The SABR rho
     */
    private double rho;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setRho = false;

    /**
     * The SABR volVol
     */
    private double volVol;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setVolVol = false;

    /**
     * The at-the-money Volatility
     */
    private double atmVol;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setAtmVol = false;

    /**
     * Default Constructor
     */
    public SABRSmileVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param yearsIn double
     * @param alphaIn double
     * @param rhoIn double
     * @param volVolIn double
     * @param atmVolIn double
     */
    public SABRSmileVO(final double yearsIn, final double alphaIn, final double rhoIn, final double volVolIn, final double atmVolIn) {

        this.years = yearsIn;
        this.setYears = true;
        this.alpha = alphaIn;
        this.setAlpha = true;
        this.rho = rhoIn;
        this.setRho = true;
        this.volVol = volVolIn;
        this.setVolVol = true;
        this.atmVol = atmVolIn;
        this.setAtmVol = true;
    }

    /**
     * Copies constructor from other SABRSmileVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public SABRSmileVO(final SABRSmileVO otherBean) {

        this.years = otherBean.getYears();
        this.setYears = true;
        this.alpha = otherBean.getAlpha();
        this.setAlpha = true;
        this.rho = otherBean.getRho();
        this.setRho = true;
        this.volVol = otherBean.getVolVol();
        this.setVolVol = true;
        this.atmVol = otherBean.getAtmVol();
        this.setAtmVol = true;
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
     * The SABR alpha
     * @return alpha double
     */
    public double getAlpha() {

        return this.alpha;
    }

    /**
     * The SABR alpha
     * @param value double
     */
    public void setAlpha(final double value) {

        this.alpha = value;
        this.setAlpha = true;
    }

    /**
     * Return true if the primitive attribute alpha is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetAlpha() {

        return this.setAlpha;
    }

    /**
     * The SABR rho
     * @return rho double
     */
    public double getRho() {

        return this.rho;
    }

    /**
     * The SABR rho
     * @param value double
     */
    public void setRho(final double value) {

        this.rho = value;
        this.setRho = true;
    }

    /**
     * Return true if the primitive attribute rho is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetRho() {

        return this.setRho;
    }

    /**
     * The SABR volVol
     * @return volVol double
     */
    public double getVolVol() {

        return this.volVol;
    }

    /**
     * The SABR volVol
     * @param value double
     */
    public void setVolVol(final double value) {

        this.volVol = value;
        this.setVolVol = true;
    }

    /**
     * Return true if the primitive attribute volVol is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetVolVol() {

        return this.setVolVol;
    }

    /**
     * The at-the-money Volatility
     * @return atmVol double
     */
    public double getAtmVol() {

        return this.atmVol;
    }

    /**
     * The at-the-money Volatility
     * @param value double
     */
    public void setAtmVol(final double value) {

        this.atmVol = value;
        this.setAtmVol = true;
    }

    /**
     * Return true if the primitive attribute atmVol is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetAtmVol() {

        return this.setAtmVol;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("SABRSmileVO [years=");
        builder.append(this.years);
        builder.append(", setYears=");
        builder.append(this.setYears);
        builder.append(", alpha=");
        builder.append(this.alpha);
        builder.append(", setAlpha=");
        builder.append(this.setAlpha);
        builder.append(", rho=");
        builder.append(this.rho);
        builder.append(", setRho=");
        builder.append(this.setRho);
        builder.append(", volVol=");
        builder.append(this.volVol);
        builder.append(", setVolVol=");
        builder.append(this.setVolVol);
        builder.append(", atmVol=");
        builder.append(this.atmVol);
        builder.append(", setAtmVol=");
        builder.append(this.setAtmVol);
        builder.append("]");

        return builder.toString();
    }

}
